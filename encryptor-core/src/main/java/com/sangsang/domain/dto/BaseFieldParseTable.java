package com.sangsang.domain.dto;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 解析sql中出现字段的基类
 *
 * @author liutangqi
 * @date 2024/3/4 14:26
 */
public class BaseFieldParseTable {
    /**
     * 层数
     */
    private int layer;
    /**
     * 存储sql中 第layer 层select 中出现字段
     * key： layer层数
     * value:
     * ---key: 表别名(有表别名就是别名，没有别名就是表名)
     * --- value: (注意：这里嵌套子查询时，这里的所有字段不一定属于同一张表)
     * -------出现的字段的 “别名” 和这个字段所属的真实表名
     **/
    private Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap;
    /**
     * 存储sql中 第layer 层查询表 拥有的全部字段
     * key： layer层数
     * value:
     * ---key: 表别名(有表别名就是别名，没有别名就是表名)
     * --- value: (注意：这里嵌套子查询时，这里的所有字段不一定属于同一张表)
     * ------查询的表拥有的全部 “字段原名”和所属的真实表名
     */
    private Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap;


    public BaseFieldParseTable(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        this.layer = layer;
        this.layerSelectTableFieldMap = Optional.ofNullable(layerSelectTableFieldMap).orElse(new HashMap<>());
        this.layerFieldTableMap = Optional.ofNullable(layerFieldTableMap).orElse(new HashMap<>());
    }

    public int getLayer() {
        return layer;
    }

    public Map<String, Map<String, Set<FieldInfoDto>>> getLayerSelectTableFieldMap() {
        return layerSelectTableFieldMap;
    }

    public Map<String, Map<String, Set<FieldInfoDto>>> getLayerFieldTableMap() {
        return layerFieldTableMap;
    }
}
