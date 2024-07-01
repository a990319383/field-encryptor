# field-encryptor

#### 介绍
​	基于mybatis-plus 的数据库自动加解密插件

#### 软件架构
​	 基于jsqlparse解析执行sql,利用mysql自己的加解密库函数对执行的sql中需要加解密的字段进行自动加解密

​	由mybatis拦截器，替换原sql，达到无侵入的效果

​	操作简单，只用在实体类指定需要加解密的字段，对业务代码0侵入


#### 安装教程

1. 使用

   - 引入pom依赖

     - 如果数据库实体类和引入mybatis-plus 的不在一个模块的话，实体类的模块只引入encryptor-annos

       之前有mybatis-plus依赖的业务模块再引入encryptor-core

     - 如果实体类和引入mybatis-plus的业务模块在一起的话，直接引入encryptor-core即可

   - 需要加解密的表的**实体类**的对应字段上面标注 @FieldEncryptor

     - 注意：只需要在@TableName的实体类上标注加密字段，不需要每个接口请求响应去标注

   - 将项目表结构信息加载入缓存

     - 多模块项目，所有的实体类不在同一个JVM的时候需要手动指定扫描实体类的路径

       ```
       field.encryptor.scanEntityPackage[0]=com..*.model
       ```

     - 所有实体类都在一个JVM时，不需要额外配置，默认加载当前模块mybatis-plus加载的实体类

   - 自定义加密秘钥

     ```
     field.encryptor.secretKey=xxxx自定义秘钥
     ```

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

#### 不兼容语法

-  不支持 convert() 函数的解析（jsqlparse最新版本仍不支持此语法的解析）

#### 参与贡献

gitee地址：

​	[field-encryptor: 基于mybatis-plus 的数据库自动加解密插件 (gitee.com)](https://gitee.com/tired_of_the_water/field-encryptor)