package com.sangsang.domain.dto;

import com.sangsang.domain.annos.encryptor.FieldEncryptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从数据库查到的表结构字段信息
 *
 * @author liutangqi
 * @date 2024/8/27 10:26
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableFieldMsgDto {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 字段类型
     */
    private String dataType;
    /**
     * 字段索引
     */
    private String columnKey;
    /**
     * 字段大概长度
     */
    private Integer fieldLength;

    /**
     * 字段的备注
     */
    private String columnComment;

    /**
     * 字段上面标注的注解
     */
    private FieldEncryptor fieldEncryptor;
}
