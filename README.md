# field-encryptor

#### 介绍
​	基于mybatis-plus 的数据库自动加解密插件

#### 软件架构
​	 基于jsqlparse解析执行sql,利用mysql自己的加解密库函数对执行的sql中需要加解密的字段进行自动加解密

​	由mybatis拦截器，替换原sql，达到无侵入的效果


#### 安装教程

1. 使用

   - 引入pom依赖
   - 需要加解密的表的实体类的对应字段上面标注 @FieldEncryptor

2. 个性化配置

   - 自定义秘钥  

     配置文件中加入 field.encryptor.secretKey = 自己的秘钥

     没有配置的话，采用插件默认的秘钥

   - 自定义加解密算法

     默认加密算法是AES加密

     自定义的话，实现FieldEncryptorPattern 接口，实现其中的加解密方法，并加上@Component即可

#### 使用说明

1. 此插件是对原字段进行加解密处理，所以支持 like 模糊匹配

2. 由于有列运算，会造成索引失效，当加密字段有作为索引的需求的话，建议在其他条件比如时间上加上索引

3. 如果数据很大，必须采用此字段作为索引的话，建议做出如下改造

   栗子：

   - 表结构

   ```sql
   CREATE TABLE `tb_user` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `user_name` varchar(100) DEFAULT NULL COMMENT '用户名',
     `phone` varchar(50) DEFAULT NULL COMMENT '电话号码',
     `create_time` datetime DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     PRIMARY KEY (`id`),
     KEY `tb_user_phone_IDX` (`phone`) USING BTREE
   ) COMMENT='用户'
   ```

   - 需求

     上述表结构下，需要对phone进行加解密，但是业务有对phone很强的索引需求

   - 建议改造

     这种情况下，建议对原有sql进行改造，先利用覆盖索引，让phone索引生效，再根据id去检索这条数据

   - 具体sql

     - 原sql

       select * from tb_user where phone like "18%"

     - 改造后

       select id from tb_user where phone like "18%"

       select * from tb_user where id in (上面sql的结果集)

#### 参与贡献

gitee地址：

​	[field-encryptor: 基于mybatis-plus 的数据库自动加解密插件 (gitee.com)](https://gitee.com/tired_of_the_water/field-encryptor)