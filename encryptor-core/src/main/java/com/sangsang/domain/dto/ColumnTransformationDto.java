package com.sangsang.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.schema.Column;

/**
 * @author liutangqi
 * @date 2025/5/23 9:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnTransformationDto {
    /**
     * 当前具体的字段
     */
    private Column column;

    /**
     * 是否是表字段
     * true: 当前Column属于表字段
     * false: 当前Column不属于表字段，属于常量
     */
    private boolean tableFiled;
}
