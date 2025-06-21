package com.sangsang.transformation.mysql2dm.expression;

import com.sangsang.transformation.ExpressionTransformation;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TranscodingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * convert函数的转换，由于达梦没有可替代的语法，所以这里只做警告日志输出，由用户自己从业务上替换
 *
 * @author liutangqi
 * @date 2025/6/3 14:41
 */
public class ConvertExpressionM2DTf extends ExpressionTransformation {
    private static final Logger log = LoggerFactory.getLogger(ConvertExpressionM2DTf.class);

    @Override
    public boolean needTransformation(Expression expression) {
        //所有的convert函数都需要转换
        return expression instanceof TranscodingFunction;
    }

    @Override
    public Expression doTransformation(Expression expression) {
        log.warn("【db-transformation】达梦数据库不支持convert函数，且没有替换方案，请手动调整");
        return expression;
    }
}
