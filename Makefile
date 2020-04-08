.PHONY:  build doc-dev

build:
		mvn -DskipTests clean package

deploy-dev: build
		scp ./mdp-admin-server/target/mdp-admin-server-1.0.0.jar miotech@192.168.1.211:/home/miotech/Docker/mdp/
		scp ./startup.sh miotech@192.168.1.211:/home/miotech/Docker/mdp/
		scp ./stop.sh miotech@192.168.1.211:/home/miotech/Docker/mdp/
		ssh miotech@192.168.1.211 'cd /home/miotech/Docker/mdp/; bash stop.sh ; bash startup.sh;'


remote=ec2-user@54.248.3.105
deploy-prod:
		scp ./mdp-admin-server/target/mdp-admin-server-1.0.0.jar ${remote}:/home/ec2-user/mdp/
		ssh ${remote} 'cd /home/ec2-user/mdp/; bash stop.sh ;MDP_ENV=prod bash startup.sh;'

doc-dev:
		curl "http://localhost:9999/v2/api-docs" -o swagger.json
		scp swagger.json ec2-user@10.0.1.254:/home/ec2-user/static/swagger-ui/dist/mdp/swagger.json
