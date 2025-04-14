# 数据库自动加解密使用文档

## 技术支持

- 990319383@qq.com
- bug反馈可通过邮件或者QQ的方式进行联系，QQ的话请备注来意

## 版本迭代记录

| 版本号 |                主要内容                |  日期  |
| :----: | :------------------------------------: | :----: |
|  1.0   |         支持db模式的自动加解密         | 2024/5 |
| 2.0.0 | 增加pojo模式，pojo同时支持多种算法共存 | 2024/9 |
| 2.1.0 | 将jsqlparser打入项目的jar中，重命名避免版本冲突 | 2025/2 |
| 3.0.0 | 优化项目结构，减少visitor的重复代码，<br>db模式兼容insert(select)密文存储不同的场景 | 2025/3 |
| 3.1.0 | jsqlparser升级到4.9版本<br />增加脱敏功能支持 | 2025/4 |

## 使用场景

> 1.解决等保，数据安全等场景下，数据库需要进行加密存储
>
> 2.数据响应有脱敏需求

## 简介

> 基于mybatis拦截器 + jsqlparser ，解析执行sql，动态替换加解密字段，实现字段自动加解密
>
> 仅需标注实体类密文字段，无需每个接口进行标注，实现快速改造，业务代码0侵入

## 详细介绍

### 1.加解密模式

- db模式
  - 基本实现原理
    - 通过直接改写原sql，利用数据库本身的加解密库函数对字段进行加解密
  - 多算法支持（数据库中不同字段存储算法不一致）
    - 项目仅支持一种加解密算法
    - 多算法支持，后续版本可能会考虑
  - 优点
    - 加解密场景适配性强，支持列运算的字符加解密
    - 历史数据清洗方便
    - 支持模糊查询
  - 缺点
    - 数据库本身支持的加解密算法较少，加密算法扩展性弱，低版本缺失加解密算法（可通过udf解决）
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
    - 一般情况下不支持模糊查询

### 2.大致处理逻辑

> - 项目启动时，将指定包路径下面的所有@TableName标注的实体类及其字段加载到本地缓存中
> - sql执行的时候，通过jsqlparser 解析语法树，得到每个字段所属的表关系，从而知道这个字段是否需要进行加解密
> - 通过拦截器，动态替换sql或者修改入参响应，达到不修改业务代码，实现加解密的效果

### 3.效果示例

```sql
-- 建表语句 其中phone字段存的是密文
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户名',
  `login_name` varchar(100) DEFAULT NULL COMMENT '登录名',
  `login_pwd` varchar(100) DEFAULT NULL COMMENT '登录密码',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话号码',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
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

- 一般情况，仅需引入下面依赖即可

  ```
  <dependency>
      <groupId>io.gitee.tired-of-the-water</groupId>
      <artifactId>encryptor-core</artifactId>
      <version>对应版本</version>
  </dependency>
  ```

- 当实体类模块没有mybatis依赖时，可以在实体类模块单独引入注解模块，用于使用框架注解

  ```
  <dependency>
      <groupId>io.gitee.tired-of-the-water</groupId>
      <artifactId>encryptor-annos</artifactId>
      <version>对应版本</version>
  </dependency>
  ```

### 2.增加配置

```
#自定义秘钥，当不自定义加解密算法时，这个值建议自定义
field.encryptor.secretKey=7uq?q8g3@q
#加密模式，目前支持pojo/db两种模式
field.encryptor.patternType=db
#指定实体类包路径（项目启动时会扫描指定路径下的实体类，加载到本地缓存中）
field.encryptor.scanEntityPackage[0]=com.sinoiov.model
#开启脱敏支持（如果不需要，则不需要加这个配置）
field.encryptor.fieldDesensitize=true
```

### 3.数据库加密存储使用

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

### 4.响应脱敏使用

- 自定义脱敏算法
  - 注意：相比于常规的序列化时进行脱敏，这里可以根据当前对象的不同情况进行不同的脱敏

```
public class CustomerDesensitize implements DesensitizeInterface {
    /**
    * cleartext :原字符串
    * obj：当前整个对象
    */
    @Override
    public String desensitize(String cleartext, Object obj) {
       return "xxx";
    }
}
```

- 在sql的响应处标注哪些需要脱敏

  - 响应是实体类：在具体字段上面标注@FieldDesensitize 并指定算法

    ```
        @FieldDesensitize(CustomerDesensitize.class)
        private String name;
    ```

  - 响应是String：在mapper上标注@MapperDesensitize 并指定算法

    ```
       @MapperDesensitize(@FieldDesensitize(CustomerDesensitize.class))
        List<String> getListResult(String name);
    ```

  - 响应是Map：在mapper上标注@MapperDesensitize 并指定算法，字段名

    ```java
       @MapperDesensitize({@FieldDesensitize(value = CustomerDesensitize.class, fieldName = "name"),
                @FieldDesensitize(value = CustomerDesensitize.class, fieldName = "remark")})
        Map getResultMap(String name);
    ```

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

### 3.简化分表写法

- 当项目中进行了分表时，默认需要在每个分表的表名上面标注上述注解，如果分了100张表需要重复写100次

- 想要简化配置的话，只需要在原表名的实体类上面标注@ShardingTableEncryptor，在其后实现主表表名后分表后表名的规则

  ```
  //原表实体类
  @ShardingTableEncryptor(value = ShardingTableByDay.class)
  @TableName("stat_vehicle_alarm")
  public class StatVehicleAlarmEntity {}
  
  //分表表名规则
  public class ShardingTableByDay implements ShardingTableInterface {
      private static final LocalDate BASE_DATE = LocalDate.parse("2000-01-01");
      private static final DateTimeFormatter DATE_FMT = DatePattern.createFormatter("MMdd");
      private static final List<String> SUFFIX;
      static {
          SUFFIX = new ArrayList<>();
          for (int i = 0; i < 366; i++) {
              SUFFIX.add(LocalDateTimeUtil.format(BASE_DATE.plusDays(i), DATE_FMT));
          }
      }
  
      @Override
      public List<String> getShardingTableName(String prefix) {
          return SUFFIX.stream().map(suffix -> prefix + suffix).collect(Collectors.toList());
      }
  }
  ```

  ### 4.pojo模式，select结果有列运算等不支持的语法，但是存在解密的需求

  将下列注解标注在sql响应的类上面

  ```
  @PoJoResultEncryptor
  ```


## 不兼容场景

### 1.全模式均不兼容场景

- 项目基于jsqlparse解析，其版本不支持的sql语法，此框架无法兼容
- INSERT INTO 表名 VALUES (所有字段);   表名后面没有跟具体字段的，无法兼容

### 2.db模式不兼容场景

### 3.pojo模式不兼容场景

- mybatis-plus  service层自带的saveBatch()方法不支持
- 列运算的结果集和sql的入参响应需要做对应的
  - 例如： select  concat(phone,"---")  as ph from tb_user;  无法将ph变量做自动的解密映射
- 同一个#{}入参，sql中对应不同的字段，想要拥有不同的值

## 其它扩展

建议使用db模式，支持大部分场景，如果数据库不支持此加解密算法，可以自己扩展mysql的 udf

下面链接是使用rust扩展sm4 加解密的一个栗子

[encryptor-udf: rust扩展mysql的udf ，本项目简单扩展了sm4的加解密支持](https://gitee.com/tired-of-the-water/encryptor-udf)