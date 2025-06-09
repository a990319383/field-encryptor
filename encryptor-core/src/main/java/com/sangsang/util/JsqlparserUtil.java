package com.sangsang.util;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.annos.FieldEncryptor;
import com.sangsang.domain.constants.FieldConstant;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.visitor.dbencrtptor.DBDecryptExpressionVisitor;
import com.sangsang.visitor.pojoencrtptor.PlaceholderExpressionVisitor;
import com.sangsang.visitor.transformation.TransformationExpressionVisitor;
import com.sangsang.visitor.transformation.wrap.ExpressionWrapper;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 解析sql语句相关的工具类
 *
 * @author liutangqi
 * @date 2024/2/1 14:14
 */
public class JsqlparserUtil {


    /**
     * 将 column 变成function转换为查询项
     * 备注：暂未使用此方法，此方法只为记录转换拼接的语法
     *
     * @author liutangqi
     * @date 2024/3/11 9:07
     * @Param [column]
     **/
    public static SelectItem functColumn(Column column, Alias alias) {
        String columnName = column.getColumnName();

        Function aesEncryptFunction = new Function();
        aesEncryptFunction.setName("AES_ENCRYPT");
        aesEncryptFunction.setParameters(new ExpressionList(new Column(columnName), new StringValue("encryptionKey")));

        Function toBase64Function = new Function();
        toBase64Function.setName("TO_BASE64");
        toBase64Function.setParameters(new ExpressionList(aesEncryptFunction));

        alias = alias == null ? new Alias(columnName) : alias;
        return SelectItem.from(toBase64Function, alias);
    }

    /**
     * 补齐 select * 的所有字段
     * 备注：只补齐有实体类的表，并且这个实体类存在需要加密字段
     *
     * @author liutangqi
     * @date 2024/2/19 17:20
     * @Param [selectItem, layerFieldTableMap 当前层的表拥有的全部字段 ]
     **/
    public static List<SelectItem> perfectAllColumns(SelectItem selectItem, Map<String, Set<FieldInfoDto>> layerFieldTableMap) {
        //只有查询的这张表有需要加密的字段才将* 转换为每个字段，避免不必要的性能损耗
        if (layerFieldTableMap.values().stream().flatMap(Collection::stream)
                .filter(f -> TableCache.getFieldEncryptTable().contains(f.getSourceTableName())).count() == 0) {
            return Arrays.asList(selectItem);
        }
        Expression expression = selectItem.getExpression();

        //select 别名.*  （注意：AllColumns AllTableColumns 有继承关系，这里判断顺序不能改）
        if (expression instanceof AllTableColumns) {
            String tableName = ((AllTableColumns) expression).getTable().getName().toLowerCase();
            return Optional.ofNullable(CollectionUtils.getValueIgnoreFloat(layerFieldTableMap, tableName))
                    .orElse(new HashSet<>())
                    .stream()
                    .map(m -> {
                        Column column = new Column(m.getColumnName());
                        column.setTable(new Table(tableName));
                        return SelectItem.from(column);
                    }).collect(Collectors.toList());
        }

        // select *  未指定别名，表示当前层只有一张表，直接将当前层的全部字段作为结果集返回
        if (expression instanceof AllColumns) {
            return layerFieldTableMap
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(m -> SelectItem.from(new Column(m.getColumnName())))
                    .collect(Collectors.toList());
        }

        //不包含*
        return Arrays.asList(selectItem);
    }


    /**
     * 解析当前字段所属表的信息
     *
     * @author liutangqi
     * @date 2024/3/6 14:52
     * @Param [column, layer, layerFieldTableMap 每一层的表拥有的全部字段的map]
     **/
    public static ColumnTableDto parseColumn(Column column, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        //字段名
        String columName = column.getColumnName();
        //字段所属表 （只有select 别名.字段名 时这个才有值，其它的为null）
        Table table = column.getTable();

        //字段所属的表的别名(from 后面接的表的别名)
        AtomicReference<String> tableAliasName = new AtomicReference<>();
        //字段所属表的真实表的名字
        AtomicReference<String> sourceTableName = new AtomicReference<>();
        //字段所属真实字段名
        AtomicReference<String> sourceColumn = new AtomicReference<>();
        //字段所属表的真实名字 from 后面的表的名字 （tableAliasName的真实名字）
        AtomicBoolean fromSourceTable = new AtomicBoolean(false);


        //1.没有指定表名时，从当前层的表的所有字段里面找到这个名字的表( select 字段)
        if (table == null) {
            layerFieldTableMap.get(String.valueOf(layer)).entrySet()
                    .forEach(f -> {
                        List<FieldInfoDto> matchFields = f.getValue().stream().filter(fi -> StringUtils.equalIgnoreFieldSymbol(fi.getColumnName(), columName)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(matchFields)) {
                            //当前层的所有字段里面叫这个的，正确sql语法中只会有一个，所以get(0)
                            FieldInfoDto matchField = matchFields.get(0);
                            sourceTableName.set(matchField.getSourceTableName());
                            sourceColumn.set(matchField.getSourceColumn());
                            fromSourceTable.set(matchField.isFromSourceTable());
                            tableAliasName.set(f.getKey());
                        }
                    });
        }

        //2.有指定表名时，从当前层的这张表的所有字段里面这个字段的信息 （select 别名.字段）
        if (table != null) {
            String columnTableName = table.getName().toLowerCase();
            List<FieldInfoDto> matchFields = Optional.ofNullable(CollectionUtils.getValueIgnoreFloat(layerFieldTableMap.get(String.valueOf(layer)), columnTableName))
                    .orElse(new HashSet<>())
                    .stream().filter(f -> StringUtils.equalIgnoreFieldSymbol(f.getColumnName(), columName))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(matchFields)) {
                //当前层的所有字段里面叫这个的，正确sql语法中只会有一个，所以get(0)
                FieldInfoDto matchField = matchFields.get(0);
                sourceTableName.set(matchField.getSourceTableName());
                sourceColumn.set(matchField.getSourceColumn());
                fromSourceTable.set(matchField.isFromSourceTable());
                tableAliasName.set(columnTableName);
            }
        }

        return ColumnTableDto.builder()
                .tableAliasName(tableAliasName.get())
                .sourceTableName(sourceTableName.get())
                .sourceColumn(sourceColumn.get())
                .fromSourceTable(fromSourceTable.get())
                .build();
    }


    /**
     * 判断当前column 是否是表字段还是常量
     * 主要用于语法转换时 ` " ‘ 符号的取舍
     * 注意：来自虚拟表的字段，哪怕这个字段在原虚拟表中属于常量，这个也是表字段
     *
     * @author liutangqi
     * @date 2025/5/27 13:08
     * @Param [column, layer, layerFieldTableMap]
     **/
    public static boolean isTableFiled(Column column, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        //字段名
        String columName = column.getColumnName();
        //字段所属表 （只有select 别名.字段名 时这个才有值，其它的为null）
        Table table = column.getTable();

        //1.没有指定表名时，从当前层的表的所有字段里面找到这个名字的表( select 字段)
        if (table == null) {
            for (Map.Entry<String, Set<FieldInfoDto>> entry : layerFieldTableMap.getOrDefault(String.valueOf(layer), new HashMap<>()).entrySet()) {
                //任意的表有一个字段符合，则返回true，表示是个表字段
                if (entry.getValue().stream()
                        .anyMatch(m -> StringUtils.equalIgnoreFieldSymbol(m.getColumnName(), columName))) {
                    return true;
                }
            }
        }

        //2.有指定表名时，说明肯定来自某张表，哪怕是虚拟表
        return table != null;
    }

    /**
     * 判断当前column 是否需要加解密
     *
     * @author liutangqi
     * @date 2024/3/11 15:28
     * @Param [column, layer, layerFieldTableMap]
     **/
    public static boolean needEncrypt(Column column, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        //1.匹配所属表信息
        ColumnTableDto columnTableDto = parseColumn(column, layer, layerFieldTableMap);

        //2.当前字段不需要解密直接返回 (实体类上面没有标注@FieldEncryptor注解 或者字段不是来源自真实表)
        return columnTableDto.isFromSourceTable()
                && Optional.ofNullable(TableCache.getTableFieldEncryptInfo())
                .map(m -> CollectionUtils.getValueIgnoreFloat(m, columnTableDto.getSourceTableName()))
                .map(m -> CollectionUtils.getValueIgnoreFloat(m, columnTableDto.getSourceColumn()))
                .orElse(null) != null;
    }


    /**
     * 将 dto 存放到对应的layerTableMap 中
     *
     * @param layer
     * @param tableName
     * @param dto
     * @param layerTableMap key: layer  value( key: tableName value: dto)
     * @author liutangqi
     * @date 2024/3/18 14:17
     **/
    public static void putFieldInfo(Map<String, Map<String, Set<FieldInfoDto>>> layerTableMap, int layer, String tableName, FieldInfoDto dto) {
        Map<String, Set<FieldInfoDto>> layerFieldMap = Optional.ofNullable(layerTableMap.get(String.valueOf(layer))).orElse(new HashMap<>());
        Set<FieldInfoDto> fieldInfoDtos = Optional.ofNullable(CollectionUtils.getValueIgnoreFloat(layerFieldMap, tableName)).orElse(new HashSet<>());

        fieldInfoDtos.add(dto);
        layerFieldMap.put(tableName, fieldInfoDtos);
        layerTableMap.put(String.valueOf(layer), layerFieldMap);
    }

    /**
     * 将 dto 存放到对应的layerTableMap 中
     *
     * @param layer
     * @param tableName
     * @param dtos
     * @param layerTableMap key: layer  value( key: tableName value: dtos)
     * @author liutangqi
     * @date 2024/3/18 14:17
     **/
    public static void putFieldInfo(Map<String, Map<String, Set<FieldInfoDto>>> layerTableMap, int layer, String tableName, Set<FieldInfoDto> dtos) {
        Map<String, Set<FieldInfoDto>> layerFieldMap = Optional.ofNullable(layerTableMap.get(String.valueOf(layer))).orElse(new HashMap<>());
        Set<FieldInfoDto> fieldInfoDtos = Optional.ofNullable(CollectionUtils.getValueIgnoreFloat(layerFieldMap, tableName)).orElse(new HashSet<>());

        fieldInfoDtos.addAll(dtos);
        layerFieldMap.put(tableName, fieldInfoDtos);
        layerTableMap.put(String.valueOf(layer), layerFieldMap);
    }


    /**
     * 解析出newMap 比 oldMap 多的元素
     *
     * @author liutangqi
     * @date 2024/3/20 13:54
     * @Param [oldMap, newMap]
     **/
    public static Map<String, Set<FieldInfoDto>> parseNewlyIncreased(Map<String, Set<FieldInfoDto>> oldMap, Map<String, Set<FieldInfoDto>> newMap) {
        Map<String, Set<FieldInfoDto>> result = new HashMap<>();
        for (Map.Entry<String, Set<FieldInfoDto>> newMapEntry : newMap.entrySet()) {
            //key 旧的没有，直接整个都是新增的
            if (!oldMap.containsKey(newMapEntry.getKey())) {
                result.put(newMapEntry.getKey(), newMapEntry.getValue());
                //key有，筛选出Set中新增的
            } else {
                Set<FieldInfoDto> newlyIncreasedSet = newMapEntry.getValue().stream().filter(f -> !oldMap.get(newMapEntry.getKey()).contains(f)).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(newlyIncreasedSet)) {
                    result.put(newMapEntry.getKey(), newlyIncreasedSet);
                }
            }
        }
        return result;
    }

    /**
     * 根据当前sql的查询字段和表拥有字段的集合，判断当前sql是否存在需要加密的表
     *
     * @author liutangqi
     * @date 2024/3/20 15:08
     * @Param [layerSelectTableFieldMap, layerFieldTableMap]
     **/
    public static boolean needEncrypt(Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        Set<FieldInfoDto> selectFieldInfoDtos = layerSelectTableFieldMap
                .values()
                .stream()
                .flatMap(f -> f.values().stream())
                .flatMap(Collection::stream)
                .filter(f -> TableCache.getFieldEncryptTable().contains(f.getSourceTableName()))
                .collect(Collectors.toSet());
        Set<FieldInfoDto> fieldInfoDtos = layerFieldTableMap
                .values()
                .stream()
                .flatMap(f -> f.values().stream())
                .flatMap(Collection::stream)
                .filter(f -> TableCache.getFieldEncryptTable().contains(f.getSourceTableName()))
                .collect(Collectors.toSet());
        return CollectionUtils.isNotEmpty(selectFieldInfoDtos) || CollectionUtils.isNotEmpty(fieldInfoDtos);
    }

    /**
     * 根据表和字段信息，从缓存中找到对应字段上面标注的注解
     *
     * @author liutangqi
     * @date 2024/7/24 15:24
     * @Param [dto]
     **/
    public static FieldEncryptor parseFieldEncryptor(ColumnTableDto dto) {
        Map<String, FieldEncryptor> stringFieldEncryptorMap = Optional.ofNullable(CollectionUtils.getValueIgnoreFloat(TableCache.getTableFieldEncryptInfo(), dto.getSourceTableName()))
                .orElse(new HashMap<>());
        return CollectionUtils.getValueIgnoreFloat(stringFieldEncryptorMap, dto.getSourceColumn());
    }

    /**
     * 如果表达式的一边是Column 一边是我们的特殊表达式，则将他们的对应关系维护到placeholderColumnTableMap 中
     *
     * @author liutangqi
     * @date 2024/7/11 11:24
     * @Param [layer 当前层数, layerFieldTableMap 当前层所有的字段信息,expression 表达式, placeholderColumnTableMap 存放结果集的map]
     **/
    public static void parseWhereColumTable(int layer,
                                            Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap,
                                            BinaryExpression expression,
                                            Map<String, ColumnTableDto> placeholderColumnTableMap) {
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();

        parseWhereColumTable(layer, layerFieldTableMap, leftExpression, rightExpression, placeholderColumnTableMap);
    }


    /**
     * 如果表达式的一边是Column 一边是我们的特殊表达式，则将他们的对应关系维护到placeholderColumnTableMap 中
     *
     * @author liutangqi
     * @date 2024/7/11 11:24
     * @Param [layer 当前层数, layerFieldTableMap 当前层所有的字段信息,leftExpression 左表达式,rightExpression右表达式, placeholderColumnTableMap 存放结果集的map]
     **/
    public static void parseWhereColumTable(int layer,
                                            Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap,
                                            Expression leftExpression,
                                            Expression rightExpression,
                                            Map<String, ColumnTableDto> placeholderColumnTableMap) {
        //左边是列，右边是我们的占位符
        if (leftExpression instanceof Column
                && rightExpression != null
                && rightExpression.toString().contains(FieldConstant.PLACEHOLDER)) {
            ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn((Column) leftExpression, layer, layerFieldTableMap);
            placeholderColumnTableMap.put(rightExpression.toString(), columnTableDto);
        }

        //左边是我们的占位符 右边是列
        if (rightExpression instanceof Column
                && leftExpression != null
                && leftExpression.toString().contains(FieldConstant.PLACEHOLDER)) {
            ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn((Column) rightExpression, layer, layerFieldTableMap);
            placeholderColumnTableMap.put(leftExpression.toString(), columnTableDto);
        }
    }


    /**
     * 针对BinaryExpression进行语法解析
     * 备注：只有InExpression 同时拥有左表达式和右表达式，但是不属于BinaryExpression 没有走这个解析
     *
     * @author liutangqi
     * @date 2025/6/6 16:01
     * @Param [tfExpressionVisitor, expression]
     **/
    public static void visitTfBinaryExpression(TransformationExpressionVisitor tfExpressionVisitor, BinaryExpression expression) {
        //解析左表达式
        Expression leftExpression = expression.getLeftExpression();
        //使用包装类进行转转，额外对整个Expression进行语法转换一次
        Expression tfExpL = ExpressionWrapper.wrap(leftExpression).accept(tfExpressionVisitor);
        Optional.ofNullable(tfExpL).ifPresent(p -> expression.setLeftExpression(p));

        //解析右表达式
        Expression rightExpression = expression.getRightExpression();
        //使用包装类进行转转，额外对整个Expression进行语法转换一次
        Expression tfExpR = ExpressionWrapper.wrap(rightExpression).accept(tfExpressionVisitor);
        Optional.ofNullable(tfExpR).ifPresent(p -> expression.setRightExpression(p));
    }


    /**
     * 针对BinaryExpression进行db模式的解密
     *
     * @author liutangqi
     * @date 2025/6/6 16:20
     * @Param [dbExpressionVisitor, expression]
     **/
    public static void visitDbBinaryExpression(DBDecryptExpressionVisitor dbExpressionVisitor, BinaryExpression expression) {
        //解析左表达式
        Expression leftExpression = expression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(dbExpressionVisitor);
        leftExpression.accept(leftExpressionVisitor);
        expression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        //解析右表达式
        Expression rightExpression = expression.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(dbExpressionVisitor);
        rightExpression.accept(rightExpressionVisitor);
        expression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }


    /**
     * 针对BinaryExpression进行pojo模式的占位符的解析
     *
     * @author liutangqi
     * @date 2025/6/6 16:58
     * @Param [phExpressionVisitor, expression]
     **/
    public static void visitPojoBinaryExpression(PlaceholderExpressionVisitor phExpressionVisitor, BinaryExpression expression) {
        //visitor中上游的表达式不为空的话，则这个visitor不能复用
        PlaceholderExpressionVisitor phVisitor = phExpressionVisitor;
        if (phExpressionVisitor.getUpstreamExpression() != null) {
            phVisitor = PlaceholderExpressionVisitor.newInstanceCurLayer(phExpressionVisitor);
        }

        //开始解析左右表达式
        Expression leftExpression = expression.getLeftExpression();
        leftExpression.accept(phVisitor);

        Expression rightExpression = expression.getRightExpression();
        rightExpression.accept(phVisitor);
    }

}
