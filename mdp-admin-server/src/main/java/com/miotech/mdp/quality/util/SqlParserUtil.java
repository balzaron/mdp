package com.miotech.mdp.quality.util;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 10:30 AM
 */
@Slf4j
public class SqlParserUtil {

    public static String[] getColumns(String sql) {
        String[] ret = {};
        try {
            SchemaStatVisitor visitor = getVisitor(sql);
            Collection<TableStat.Column> collection = visitor.getColumns();
            ret = collection.stream().map(
                    TableStat.Column::getFullName
            ).toArray(size -> new String[collection.size()]);
        }catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return ret;
    }

    public static SchemaStatVisitor getVisitor(String sql) {
        SQLStatementParser parser = new SQLStatementParser(sql);
        SQLStatement statement = parser.parseStatement();
        SchemaStatVisitor visitor = new SchemaStatVisitor();
        statement.accept(visitor);
        return visitor;
    }
}
