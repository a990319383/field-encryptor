package com.sangsang.util;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.constants.SymbolConstant;

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
            sql = sql.replaceFirst(SymbolConstant.ESC_QUESTION_MARK, DecryptConstant.PLACEHOLDER + i);
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
        int wordCount = StringUtils.wordCount(sql, DecryptConstant.PLACEHOLDER);
        for (int i = 0; i < wordCount; i++) {
            sql = sql.replaceFirst(DecryptConstant.PLACEHOLDER + i, SymbolConstant.ESC_QUESTION_MARK);
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
     * 判断sql中是否一定不存在加解密的字段
     *
     * @return true:一定不存在 false: 可能存在
     * @author liutangqi
     * @date 2024/9/18 22:02
     * @Param [sql]
     **/
    public static boolean notExistEncryptor(String sql) {
        if (StringUtils.isBlank(sql)) {
            return true;
        }
        //sql转小写
        String lowerCaseSql = sql.toLowerCase();

        //获取当前需要加解密的表
        Set<String> fieldEncryptTable = TableCache.getFieldEncryptTable();
        for (String table : fieldEncryptTable) {
            if (lowerCaseSql.contains(table)) {
                return false;
            }
        }

        //都不含需要加解密的表，则当前sql一定不需要加解密
        return true;
    }

    /**
     * 将字符串中的换行符替换为空格
     *
     * @author liutangqi
     * @date 2025/4/8 15:17
     * @Param [str]
     **/
    public static String replaceLineBreak(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != '\n' && c != '\r') {
                sb.append(c);
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
