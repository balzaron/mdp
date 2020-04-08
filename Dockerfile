FROM hub.miotech.com/library/openjdk:8u181-jdk
ARG sentry_dsn
ARG sentry_env
ARG sentry_release

COPY mdp-admin-server/target/mdp-admin-server-1.0.0.jar /server/target/
WORKDIR /server/target
EXPOSE 9999
CMD SENTRY_DSN=${sentry_dsn} SENTRY_ENVIRONMENT=${env} SENTRY_RELEASE=${tag} java ${JAVA_OPTS} -jar /server/target/mdp-admin-server-1.0.0.jar -Dapp=mdp-server -Dtag=${tag} -Denv=${env}
