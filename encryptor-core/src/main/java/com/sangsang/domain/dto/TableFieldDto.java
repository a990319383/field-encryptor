package com.sangsang.domain.dto;

import com.sangsang.domain.annos.encryptor.FieldEncryptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author liutangqi
 * @date 2024/5/17 11:12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableFieldDto {
    /**
     * 字段名(小写)
     */
    private String fieldName;

    /**
     * 字段上拥有的@FieldEncryptor 注解
     */
    private FieldEncryptor fieldEncryptor;
}
