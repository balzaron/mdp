# MDP(Data management system) Spec

## Common
module name : mdp开头，短横杠（-）作为分隔符 <br>
java package : com.miotech.mdp.xxx <br>
sql : sql文件放在src/main/sql（参考mdp-table-management）

## Common Module
在开发过程中与业务无关的代码应集中抽象至mdp-common <br>
  - common pojo
  - common utils
  - common config
  - common annotation

## Development Flow
1. 确保本地master代码与远程一致
    - git checkout master
    - git pull
2. 创建对应SP号的新分支
    - git checkout -b ER-SP-XXXX/jiechen
3. 在新分支上提交自己的代码
    - git add .
    - git commit -m "ER: SP-XXXX some messsage"
4. 如果有多次commit则需要合并成一个commit再push
    - git rebase -i master
    - git push
    
## Rest API Doc

使用swagger2生成API文档 <br>
目前swagger已改造成支持multi-package扫描，将需要扫描的package添加至mdp-admin-server -> com.miotech.mdp.config.Swagger2Config

publish docs during dev

```sh 
make doc-dev
```

add docs host to your hosts
```sh 
echo "54.248.3.105 docs.miotech.com" >> /etc/hosts

```

then go to url [docs.miotech.com](http://docs.miotech.com)

## Persistent
使用Spring data jpa


