package com.sangsang.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author liutangqi
 * @date 2024/5/17 10:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableInfoDto {
    /**
     * 表名（小写）
     */
    private String tableName;

    /**
     * 该表拥有的全部字段（小写）
     */
    private Set<TableFieldDto> tableFields;
}
