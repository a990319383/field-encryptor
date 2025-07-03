package com.sangsang.util;

import cn.hutool.crypto.digest.DigestUtil;
import com.sangsang.domain.constants.FieldConstant;
import com.sangsang.domain.constants.SymbolConstant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * 避免引入多余的包，这里将commons.lang3的工具类给拷贝过来
 *
 * @copy org.apache.commons.lang3
 * @date 2024/3/29 15:25
 */
public class StringUtils {

    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     * not empty and not null and not whitespace only
     * @since 2.0
     * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }


    /**
     * 获取字符串中出现指定字符串的次数
     *
     * @author liutangqi
     * @date 2024/7/10 18:10
     * @Param [superstring 母串, substring 子串]
     **/
    public static int wordCount(String superstring, String substring) {
        int count = 0;
        if (StringUtils.isBlank(superstring) || StringUtils.isBlank(substring)) {
            return count;
        }

        int index = 0;
        while ((index = superstring.indexOf(substring, index)) != -1) {
            count++;
            // 移动到找到的子串之后
            index += substring.length();
        }
        return count;
    }


    /**
     * 将sql中的 ？ 替换为 DecryptConstant.PLACEHOLDER + 自增序号，从0开始
     *
     * @author liutangqi
     * @date 2024/7/10 18:05
     * @Param [sql]
     **/
    public static String question2Placeholder(String sql) {
        //找出原sql中的 ？个数
        int wordCount = StringUtils.wordCount(sql, SymbolConstant.QUESTION_MARK);
        for (int i = 0; i < wordCount; i++) {
            sql = sql.replaceFirst(SymbolConstant.ESC_QUESTION_MARK, FieldConstant.PLACEHOLDER + i);
        }
        return sql;
    }


    /**
     * 将sql中的 DecryptConstant.PLACEHOLDER + 自增序号，从0开始 替换为 ？
     *
     * @author liutangqi
     * @date 2024/7/10 18:06
     * @Param
     **/
    public static String placeholder2Question(String sql) {
        //找出原sql中的 DecryptConstant.PLACEHOLDER个数
        int wordCount = StringUtils.wordCount(sql, FieldConstant.PLACEHOLDER);
        for (int i = 0; i < wordCount; i++) {
            sql = sql.replaceFirst(FieldConstant.PLACEHOLDER + i, SymbolConstant.ESC_QUESTION_MARK);
        }
        return sql;
    }

    /**
     * 忽略大小写，判断两个字符串是否相等
     *
     * @author liutangqi
     * @date 2024/8/27 11:24
     * @Param [a, b]
     **/
    public static boolean equalCaseInsensitive(String a, String b) {
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return false;
        }
        return a.toLowerCase().equals(b.toLowerCase());
    }

    /**
     * 忽略大小写，忽略开头结尾的 `  " 判断两个字段是否相等
     *
     * @author liutangqi
     * @date 2025/5/27 13:11
     * @Param [a, b]
     **/
    public static boolean equalIgnoreFieldSymbol(String a, String b) {
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return false;
        }
        //去掉首尾的 ` 、 "
        String clearA = trim(trim(a, SymbolConstant.FLOAT), SymbolConstant.DOUBLE_QUOTES);
        String clearB = trim(trim(b, SymbolConstant.FLOAT), SymbolConstant.DOUBLE_QUOTES);
        return equalCaseInsensitive(clearA, clearB);
    }

    /**
     * 判断sql中是否一定不存在 lowerTableNames 中涉及的表
     *
     * @return true:一定不存在  false: 可能存在
     * @author liutangqi
     * @date 2025/7/3 10:40
     * @Param [sql, lowerTableNames]
     **/
    public static boolean notExist(String sql, Set<String> lowerTableNames) {
        //1.表名为空，或者sql为空 肯定不存在
        if (StringUtils.isBlank(sql) || CollectionUtils.isEmpty(lowerTableNames)) {
            return true;
        }

        //2.sql转小写
        String lowerCaseSql = sql.toLowerCase();

        //3.依次判断sql中是否包含执行表名
        for (String table : lowerTableNames) {
            if (lowerCaseSql.contains(table)) {
                return false;
            }
        }

        //4.都不包含
        return true;
    }

    /**
     * 将字符串中的整行的空白去除
     *
     * @author liutangqi
     * @date 2025/4/8 15:17
     * @Param [str]
     **/
    public static String replaceLineBreak(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        StringBuilder lineBuilder = new StringBuilder();
        boolean lineIsBlank = true;
        boolean hasContent = false; // 标记是否已有非空白内容
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // 检查换行符（支持 \n、\r、\r\n）
            if (c == '\n' || c == '\r') {
                // 处理 \r\n 组合的情况
                if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n') {
                    i++; // 跳过 \n
                }
                // 若非空白行则保留
                if (!lineIsBlank) {
                    result.append(lineBuilder);
                    result.append(c);
                }
                // 重置行状态
                lineBuilder.setLength(0);
                lineIsBlank = true;
                hasContent = true;
                continue;
            }
            // 检查非空白字符
            if (!Character.isWhitespace(c)) {
                lineIsBlank = false;
            }
            // 添加到当前行
            lineBuilder.append(c);
        }
        // 处理最后一行无换行符的情况
        if (!lineIsBlank) {
            result.append(lineBuilder);
        }
        // 处理纯空白输入的情况
        else if (!hasContent && lineBuilder.length() > 0) {
            result.append(lineBuilder);
        }
        return result.toString();
    }

    /**
     * 获取字符串的md5值
     *
     * @author liutangqi
     * @date 2025/5/21 14:54
     * @Param [input]
     **/
    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取sha256值
     *
     * @author liutangqi
     * @date 2025/5/27 11:03
     * @Param [input]
     **/
    public static String getSha256(String input) {
        return DigestUtil.sha256Hex(input);
    }


    /**
     * 去除字符串str的首尾的 c
     *
     * @author liutangqi
     * @date 2025/5/27 18:00
     * @Param [str, c]
     **/
    public static String trim(String str, String c) {
        if (str == null || c == null || c.isEmpty() || str.isEmpty()) {
            return str;
        }

        int str1Len = str.length();
        int str2Len = c.length();

        // 如果 str2 比 str1 长，不可能匹配
        if (str2Len > str1Len) {
            return str;
        }

        int start = 0;
        int end = str1Len;

        // 处理开头的 str2 重复匹配
        while (start <= end - str2Len && str.startsWith(c, start)) {
            start += str2Len;
        }

        // 处理结尾的 str2 重复匹配
        while (end >= start + str2Len && str.startsWith(c, end - str2Len)) {
            end -= str2Len;
        }

        return (start > 0 || end < str1Len) ? str.substring(start, end) : str;
    }


    /**
     * 获取sql可以标识唯一的串
     * 原sql长度_sha256
     *
     * @author liutangqi
     * @date 2025/5/29 14:22
     * @Param [sql]
     **/
    public static String getSqlUniqueKey(String sql) {
        return sql.length() + SymbolConstant.UNDERLINE + getSha256(sql);
    }
}
