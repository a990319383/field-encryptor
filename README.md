# 数据库自动加解密使用文档

## 技术支持

- 990319383@qq.com

## 版本迭代记录

| 版本号 |                主要内容                |  日期  |
| :----: | :------------------------------------: | :----: |
|  1.0   |         支持db模式的自动加解密         | 2024/5 |
| 1.0  | 增加pojo模式，pojo同时支持多种算法共存 | 2024/9 |

## 使用场景

> 解决等保，数据安全等场景下，数据库需要进行加密存储的问题

## 简介

> 本组件基于mybatis拦截器 + jsqlparser ，解析执行sql，动态替换加解密字段，实现字段自动加解密
>
> 仅需标注实体类密文字段，无需每个接口进行标注，实现业务代码0侵入

## 详细介绍

### 1.加解密模式

- db模式
  - 基本实现原理
    - 通过直接改写原sql，利用数据库本身的加解密库函数对字段进行加解密
  - 多算法支持
    - 项目仅支持一种加解密算法
  - 优点
    - 加解密场景适配性强，支持列运算的字符加解密
    - 历史数据清洗方便
    - 支持模糊查询
  - 缺点
    - 数据库本身支持的加解密算法较少，加密算法扩展性弱，低版本缺失加解密算法
    - 增加额外数据库计算开销
- pojo模式
  - 基本实现
    - 通过java库在拦截器层对sql的入参，响应进行加解密
  - 多算法支持
    - 项目针对不同字段可配置多种加解密算法
  - 优点
    - 加解密算法可选择性强，java能实现的算法都支持
  - 缺点
    - 加解密场景适配弱，仅对sql入参响应进行加解密，无法针对列运算做处理
    - 历史数据清洗麻烦
    - 一般情况下不支持模糊查询，如果以牺牲存储空间的方式选择特定算法，可以支持模糊搜索

### 2.大致处理逻辑

> - 项目启动时，将指定包路径下面的所有@TableName标注的实体类及其字段加载到本地缓存中
> - sql执行的时候，通过jsqlparser 解析语法树，得到每个字段所属的表关系，从而知道这个字段是否需要进行加解密
> - 通过拦截器，动态替换sql或者修改入参响应，达到不修改业务代码，实现加解密的效果

### 3.效果示例

```sql
-- 建表语句 其中phone字段存的是密文
CREATE TABLE `tb_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户名',
  `login_name` varchar(100) DEFAULT NULL COMMENT '登录名',
  `login_pwd` varchar(100) DEFAULT NULL COMMENT '登录密码',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话号码',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色id',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='用户'
-- 查询语句
select phone,user_name from tb_user where phone = ?
```

- db模式

  拦截器会自动把sql替换成下面的

  ```sql
  -- select查询的会进行解密转换  where条件的会进行加密转换
  SELECT 
  CAST(AES_DECRYPT(FROM_BASE64(phone), '7uq?q8g3@q') AS CHAR) AS phone,
  user_name 
  FROM tb_user 
  WHERE phone = TO_BASE64(AES_ENCRYPT(?, '7uq?q8g3@q'))
  ```

- pojo模式

  原sql不做变更，在拦截器这层，对入参进行加密，拿到sql结果集后，对响应进行解密

## 快速接入

### 1.引入依赖

- 实体类抽离成单独的模块，业务sql的mapper在单独的模块

  - 实体类模块

    ```
    <dependency>
        <groupId>com.sinoiov</groupId>
        <artifactId>encryptor-annos</artifactId>
        <version>对应版本</version>
    </dependency>
    ```

  - 业务sql模块   

    ```
    <dependency>
        <groupId>com.sinoiov</groupId>
        <artifactId>encryptor-core</artifactId>
        <version>对应版本</version>
    </dependency>
    ```

- 实体类和业务sql都在一起（<font color='orange'>大多数场景 </font>）

  ```
  <dependency>
      <groupId>com.sinoiov</groupId>
      <artifactId>encryptor-core</artifactId>
      <version>对应版本</version>
  </dependency>
  ```

### 2.增加配置

```
#自定义秘钥，当不自定义加解密算法时，这个值建议自定义
field.encryptor.secretKey=7uq?q8g3@q
#加密模式，目前支持pojo/db两种模式
field.encryptor.patternType=pojo
#指定实体类包路径（项目启动时会扫描指定路径下的实体类，加载到本地缓存中）
field.encryptor.scanEntityPackage[0]=com.sinoiov.model
```

### 3.标注需要加解密的字段

找到实体类(<font color='red'>@TableName标注的</font>)，在其中需要加解密的字段上面标注<font color='red'>@FieldEncryptor</font>

```java
@TableName("ts_basic_ticket")
public class BasicTicketEntity extends SupperEntity {
    /**
     * 物料名称
     */
    @FieldEncryptor
    private String materialName;
}
```

<font color='red'>**注意**</font>

​	有些项目没有使用mybatis-plus，使用的是mybatis的老项目，项目中可能没有“实体类”这个概念

​	这种情况下可以用第三方工具，把项目数据库中的表都导出标注好@TableName的实体类，扔到项目的一个指定包下面即可

## 个性化配置

### 1.自定义秘钥

配置文件中增加下面参数，默认的加解密算法会使用此秘钥进行加解密处理

```
#自定义秘钥，当不自定义加解密算法时，这个值建议自定义
field.encryptor.secretKey=7uq?q8g3@q
```

- db模式

  默认采用AES加密

- pojo模式

  默认采用DES加密

### 2.自定义加解密函数

当默认的加解密算法不能适配项目场景，可以选择下面的自定义加解密函数

- db模式
  - 实现DBFieldEncryptorPattern接口，实现其中的加解密方法
  - 将自定义实现的方法交给spring容器管理（下面两种方法均可）
    - 类上标注@Component
    - 使用@Bean的方式
  - 栗子

```java
参考 com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern 默认实现类
```

- pojo模式
  - 实现PoJoFieldEncryptorPattern接口，实现其中的加解密方法，和额外的枚举PoJoAlgorithmEnum
  - 可定义多个加解密算法
    - 每种算法的encryptorAlgorithm()方法中，返回的枚举PoJoAlgorithmEnum必须不同
    - 要求其中必须有 PoJoAlgorithmEnum.ALGORITHM_DEFAULT 
  - 将自定义实现的方法交给spring管理（方法同db模式，这里略）
  - 实体类上面标注字段时可以自己选择加解密算法
    - @FieldEncryptor(pojoAlgorithm = PoJoAlgorithmEnum.ALGORITHM_1)
    - @FieldEncryptor 不填的，默认是PoJoAlgorithmEnum.ALGORITHM_DEFAULT 对应的算法

## 不兼容场景



## 版本后续规划

- 规划1

  出于性能考虑，从数据库到数据库的对比，修改，插入，默认都是密文，所以对此类场景是没有做加解密处理的，后续迭代中会针对此场景做出优化，当两者一个加密一个不加密时，会选择合适的字段进行加解密

- 规划2

  当前jsqlparser 使用版本为4.4，还存在部分语法不兼容的问题（特别是ck），后续考虑出高版本的分支	


