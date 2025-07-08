# field-encryptor使用文档

## 技术支持

- 990319383@qq.com
- bug反馈可通过邮件或者QQ的方式进行联系，QQ的话请备注来意

## 版本迭代记录

| 版本号 |                           主要内容                           |  日期  |
| :----: | :----------------------------------------------------------: | :----: |
|  1.0   |                    支持db模式的自动加解密                    | 2024/5 |
| 2.0.0  |            增加pojo模式，pojo同时支持多种算法共存            | 2024/9 |
| 2.1.0  |       将jsqlparser打入项目的jar中，重命名避免版本冲突        | 2025/2 |
| 3.0.0  | 优化项目结构，减少visitor的重复代码，<br>db模式兼容insert(select)密文存储不同的场景 | 2025/3 |
| 3.1.0  |        jsqlparser升级到4.9版本<br />增加脱敏功能支持         | 2025/4 |
| 3.2.0  | 增加 transformation语法自动转换功能<br />（目前支持mysql转达梦） | 2025/6 |
| 3.3.0  |             增加数据隔离支持（maven仓库未发布）              | 2025/7 |
| 3.5.0  |                整体配置方式重构，使用方法变化                | 2025/7 |

## 简介

> 基于mybatis拦截器 + jsqlparser ，动态改写sql。减少重复性工作，使客户专注业务开发
>
> 仅需表结构级别的标注，快捷改造各种业务
>
> 此文档针对3.5.0以后的版本

## 核心功能

> - 数据库字段自动加解密
>   - 仅需要进行实体类标注，无需每个接口标注，简单快捷，项目入侵性基本为0
> - 数据库字段查询脱敏
>   - 相较于一般的序列化脱敏方法，本方案可拿到整个响应对象，可做针对性的业务脱敏
> - sql语法自动切换（栗如：mysql的语法自动转换为达梦数据库语法）
>   - 无需改造项目sql，即可实现项目数据库种类切换
> - 业务数据自动隔离
>   - 业务表级别的数据隔离，无需每个接口标注，简单快捷
>   - 告别新人接手项目搞不清楚数据隔离或忘了加数据隔离字段，因为根本不需要加

## 引入依赖

- 一般情况，仅需引入下面依赖即可

  ```xml
  <dependency>
      <groupId>io.gitee.tired-of-the-water</groupId>
      <artifactId>encryptor-core</artifactId>
      <version>对应版本</version>
  </dependency>
  ```

- 当实体类模块没有mybatis依赖时，可以在实体类模块单独引入注解模块，用于使用框架注解

  ```xml
  <dependency>
      <groupId>io.gitee.tired-of-the-water</groupId>
      <artifactId>encryptor-annos</artifactId>
      <version>对应版本</version>
  </dependency>
  ```

## 快速接入

### 1.数据库字段自动加解密

#### 模式简介

##### db模式

- 基本实现原理
  - 通过直接改写原sql，利用**数据库本身的加解密库函数**对字段进行加解密
- 优点
  - 加解密场景适应性强，支持列运算的字符加解密
  - 历史数据清洗方便
  - 支持模糊查询
- 缺点
  - 数据库本身支持的加解密算法较少，加密算法扩展性弱，低版本缺失加解密算法（可通过udf解决）
  - 增加额外数据库计算开销

##### pojo模式

- 基本实现
  - 通过java库在拦截器层对sql的入参，响应进行加解密
- 优点
  - 加解密算法可选择性强，java能实现的算法都支持
- 缺点
  - 加解密场景适应性弱，仅对sql入参响应进行加解密，无法针对列运算做处理
  - 历史数据清洗麻烦
  - 一般情况下不支持模糊查询

#### 快速使用

> 进行了“基本配置” 和“字段标注” 即可使用

##### 基本配置

```
#配置@TableName标注的实体类路径
field.scanEntityPackage[0]=com.sangsang.es.entity
#确定想要使用的模式是哪种，目前支持 db  pojo 两种模式配置
field.encryptor.patternType=db
#自定义秘钥(这里配置秘钥后，默认的加密算法会使用这个秘钥)
field.encryptor.secretKey=TIREDTHEWATER
```

##### 字段标注

> 在标注了@TableName的实体类中，找到自己需要密文存储的字段使用@FieldEncryptor标注

```java
@Data
@TableName(value = "tb_user")
public class UserEntity extends BaseEntity {

    @TableField(value = "phone")
    @FieldEncryptor //标识这个字段是加密的字段
    private String phone;
}
```

#### 进阶使用

> 根据项目实际情况，可以使用下面的进阶的一些配置

##### 自定义加密算法

- db模式

  > 注意1: 所有引入的jsqlparser的包是com.shade.net.sf.jsqlparser 开头的，使用本框架打的包，不要使用自己引的net.sf.jsqlparser包下的
  >
  > 注意2：如果当前项目想要配置多种算法的话，需要在默认的算法的类上面标注@DefaultStrategy

```java
package xxx.strategy;

import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.annos.DefaultStrategy;
import com.sangsang.domain.strategy.encryptor.FieldEncryptorStrategy;
import com.shade.net.sf.jsqlparser.expression.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liutangqi
 * @date 2025/7/4 14:02
 */
@Component
@DefaultStrategy
public class DBFieldEncryptorStrategrDefault implements FieldEncryptorStrategy<Expression> {
  
    @Autowired
    private FieldProperties fieldProperties;
	// 关于如何使用jsqlparser 拼凑出数据库的加解密函数
    //参考com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern
    
    @Override
    public Expression encryption(Expression oldExpression) {
          //todo 加密的逻辑 秘钥最好使用fieldProperties配置的秘钥
        return null;
    }

    @Override
    public Expression decryption(Expression oldExpression) {
         //todo 解密的逻辑 秘钥最好使用fieldProperties配置的秘钥
        return null;
    }
}

```

- pojo模式

```java
package xxx.strategy;

import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.strategy.encryptor.FieldEncryptorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liutangqi
 * @date 2025/7/8 11:17
 */
@Component
public class PojoFieldEncryptorStrategy implements FieldEncryptorStrategy<String> {
    @Autowired
    private FieldProperties fieldProperties;
    @Override
    public String encryption(String s) {
        //todo 加密的逻辑 秘钥最好使用fieldProperties配置的秘钥
        return null;
    }

    @Override
    public String decryption(String s) {
        //todo 解密的逻辑 秘钥最好使用fieldProperties配置的秘钥
        return null;
    }
}

```

##### 项目同时存在多种加密算法

参考上面的“自定义加密算法”，项目中实现多种算法，并放在spring容器中

- 当项目spring容器中只有一个算法，则这个算法就是默认算法
- 当spring容器中存在多个算法，需要使用@DefaultStrategy标注在算法类上作为默认算法
- 当字段使用默认算法时@FieldEncryptor 后面不用指定，使用其它算法时，需要这个注解后面指定算法类

##### 分库分表项目集成

- @FieldEncryptor 字段标注是记录了哪些表的哪些字段需要进行密文存储，当不进行任何配置时，需要将每张分表都创建好实体类，并在上面的每个字段都标注好
- 简化配置
  - 栗子："stat_vehicle_alarm"这张表是分表前的表名。
    - 实际数据库中不存在这张表
    - 数据库中有的表名是"stat_vehicle_alarm_0101","stat_vehicle_alarm_0102"等根据天进行了分表的
  - 实现ShardingTableStrategy接口，在里面配置根据原表名获取到分表的表名的规则
  - 在原表的实体类上标注@ShardingTableEncryptor(value = ShardingTableByDay.class)

```java
//原表实体类
@ShardingTableEncryptor(value = ShardingTableByDay.class)
@TableName("stat_vehicle_alarm")
public class StatVehicleAlarmEntity {
    //xxx字段
}

//分表表名规则
public class ShardingTableByDay implements ShardingTableStrategy {
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

##### 配置sql解析LRU缓存容量（默认值是100）

​	所有sql都会过一遍jsqlparser解析，大sql这部分操作比较耗时（大约15到40ms），所以这部分有层LRU缓存

```
#sql解析LRU缓存容量（默认值100）
field.lruCapacity=100
```

##### pojo模式强制指定响应解密

> 当pojo模式遇到不兼容的场景，但是还是想要在查询结果进行解密处理时

```
# 在sql的响应类的字段上面标注这个注解
# 注意:这里是响应类，不是实体类！！！
@PoJoResultEncryptor
```

#### 不兼容场景

##### db,pojo模式均不兼容的场景

- 项目基于jsqlparse解析，其版本不支持的sql语法，此框架无法兼容
- INSERT INTO 表名 VALUES (所有字段);   表名后面没有跟具体字段的，无法兼容

##### pojo模式不兼容的场景

- mybatis-plus  service层自带的saveBatch()方法不支持
- 列运算的结果集和sql的入参响应需要做对应的
  - 例如： select  concat(phone,"---")  as ph from tb_user;  无法将ph变量做自动的解密映射
- 同一个#{}入参，sql中对应不同的字段，想要拥有不同的值

#### 模式示例

- 栗子

```mysql
# 其中 phone 字段是密文存储的
select  phone,user_name FROM tb_user WHERE phone = ?
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





### 2.数据库自动查询脱敏

#### 快速使用

##### 基本配置

```
#开启脱敏支持
field.desensitize.enable=true
```

##### 自定义脱敏策略

```java
import com.sangsang.domain.strategy.desensitize.DesensitizeStrategy;

/**
 * @author liutangqi
 * @date 2025/7/8 13:45
 */
public class TestDesensitizeStrategy implements DesensitizeStrategy {
    //注意： s可能为null，自己根据业务进行不同处理
    @Override
    public String desensitize(String s, Object o) {
        //s 是待脱敏的字符串
        //o 是一整个返回对象，可以根据o来做一些业务差异化脱敏
        return "脱敏后：" + s;
    }
}
```

##### 字段标注

>  注意：这个是标注在sql的返回对象上，不是实体类，区别于其它功能

- 响应是实体类：在具体字段上面标注@FieldDesensitize 并指定算法

  ```
  public class UserVo{
  	private Long id;
      /**
       * 用户名
       */
      @FieldDesensitize(TestDesensitizeStrategy.class)
      private String userName;    
  }
  ```

- 响应是String：在mapper上标注@MapperDesensitize 并指定算法

  ```
     @MapperDesensitize(@FieldDesensitize(TestDesensitizeStrategy.class))
      List<String> getListResult(String name);
  ```

- 响应是Map：在mapper上标注@MapperDesensitize 并指定算法，字段名

  - 注意：fieldName指定的是sql中结果集的变量名(as 后面是什么就是什么，没有as 的话就是表达式或字段名)

  ```java
     @MapperDesensitize({@FieldDesensitize(value = TestDesensitizeStrategy.class, fieldName = "user_name"),
              @FieldDesensitize(value = TestDesensitizeStrategy.class, fieldName = "xxx")})
      Map getResultMap(String name);
  ```





### 3.sql语法转换

#### 快速使用

> 仅需基本配置完成即可完成语法的自动切换
>
> 完整切换到其它数据库的话，还需要改项目的数据库驱动和连接地址

##### 基本配置

```
#配置@TableName标注的实体类路径
field.scanEntityPackage[0]=com.sangsang.es.entity
#语法转换模式指定，目前仅支持mysql转达梦
field.transformation.patternType=mysql2dm
```

#### 进阶使用

##### 配置sql解析LRU缓存容量（默认值是100）

​	所有sql都会过一遍jsqlparser解析，大sql这部分操作比较耗时（大约15到40ms），所以这部分有层LRU缓存

```
#sql解析LRU缓存容量（默认值100）
field.lruCapacity=100
```





### 4.业务数据自动隔离

#### 快速使用

##### 基本配置

```
#配置@TableName标注的实体类路径
field.scanEntityPackage[0]=com.sangsang.es.entity
#开启数据隔离
field.isolation.enable=true
```

##### 自定义数据隔离策略

> 注意1：和加解密类似，整个spring容器中如果只有一个DataIsolationStrategy子类，则这个就是默认策略
>
> 注意2：项目spring容器中存在多个DataIsolationStrategy子类，@DefaultStrategy标注那个是默认策略

```java
package xxx.strategy;

import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.strategy.isolation.DataIsolationStrategy;
import org.springframework.stereotype.Component;

/**
 * @author liutangqi
 * @date 2025/7/4 17:12
 */
@Component
public class TIsolationBeanStrategy implements DataIsolationStrategy<Long> {
    //返回需要进行数据隔离的表字段名字 
    //一般项目会将登录用户存threadlocal中，这里可以取出来，根据不同的登录用户选择不同的字段隔离
    @Override
    public String getIsolationField() {
        return "org_id";
    }

    //目前支持 "="  "in"  "like 'xxx%'" 三种模式
    @Override
    public IsolationRelationEnum getIsolationRelation() {
        return IsolationRelationEnum.EQUALS;
    }

    //从当前登录用户的theadlocal中获取到用于数据隔离的字段即可，比如当前登录用户组织id之类的
    @Override
    public Long getIsolationData() {
        return 777777777L;
    }
}
```

##### 标注

> 标注在实体类上，一个实体类表示一种抽象数据，一种数据的权限所属一般情况是固定的

```java
/**
 * @author liutangqi
 * @date 2025/7/2 14:45
 */
@TableName("tb_order")
@Data
//使用上面的默认策略，根据org_id进行数据隔离，只能看到同组织的单据
@DataIsolation
public class OrderEntity extends BaseEntity {

    @TableField("org_id")
    private Long orgId;

}
```

#### 进阶使用

##### 禁用数据隔离

> 当某些场景下，不想系统默认加上数据隔离，就想看到全部数据时
>
>  使用@ForbidIsolation进行标注

- 单个sql不想要数据隔离，标注在mapper上

```
@ForbidIsolation
List<OrderEntity> getPage();
```

- 整个service的方法都不想要数据隔离,标注在service上

```
 @ForbidIsolation
 public void test(){
     //各种sql 各种业务都不想要数据隔离
 }
```

##### 配置sql解析LRU缓存容量（默认值是100）

​	所有sql都会过一遍jsqlparser解析，大sql这部分操作比较耗时（大约15到40ms），所以这部分有层LRU缓存

```
#sql解析LRU缓存容量（默认值100）
field.lruCapacity=100
```

##### 复杂场景的业务隔离

- 模拟系统简介
  -  PC端系统中存在 一级组织 --> 二级组织---> 三级组织 
    - 每一级只能看到自己和下级的单据
    - 此时通过org_seq进行隔离
  - H5端或者App端 有司机
    - 每个司机登录只能看到自己的单据
- 系统简单设计
  - 权限系统
    - org_seq 这个字段存储  上级组织路径-自己组织id
    - 一级组织： 一级组织id
    - 二级组织： 一级组织id-二级组织id
    - 三级组织： 一级组织id-二级组织id-三级组织id
  - 登录系统
    - 用户登录，将当前用户的类型（是司机还是一，二，三级组织）存到threadlocal
    - 是一二三级组织的话，存一份org_seq 司机的话存一份司机id
- 自定义策略实现

```
@Component
public class TIsolationBeanStrategy implements DataIsolationStrategy<String> {
   
    @Override
    public String getIsolationField() {
    	//从threadLocal中获取用户类型，是司机的话就返回 司机id字段(driver_id)否则返回 org_seq
        return null;
    }

    //目前支持 "="  "in"  "like 'xxx%'" 三种模式
    @Override
    public IsolationRelationEnum getIsolationRelation() {
    //根据当前登录用户类型，司机的话就是  "="  组织的话就  "like 'xxx%'"
        return null;
    }
   
    @Override
    public String getIsolationData() {
    //根据当前登录用户类型，返回登录用户的driverId或者orgSeq即可
        return null;
    }
}
```

## 附录

### 测试用例中的表结构

```mysql
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
)COMMENT='测试表-用户';
		
CREATE TABLE `tb_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(100) DEFAULT NULL COMMENT '角色名字',
  `role_desc` varchar(100) DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)COMMENT='测试表-角色';
		
CREATE TABLE `tb_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(100) DEFAULT NULL COMMENT '菜单名字',
  `path` varchar(100) DEFAULT NULL COMMENT '路径',
  `parent_id` bigint DEFAULT NULL COMMENT '父级id,第一级是0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)COMMENT='测试表-菜单';
```

### 其它扩展

建议使用db模式，支持大部分场景，如果数据库不支持此加解密算法，可以自己扩展mysql的 udf

下面链接是使用rust扩展sm4 加解密的一个栗子

[encryptor-udf: rust扩展mysql的udf ，本项目简单扩展了sm4的加解密支持](https://gitee.com/tired-of-the-water/encryptor-udf)