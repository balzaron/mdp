package com.miotech.mdp.common.util;

import com.miotech.mdp.common.models.protobuf.schema.DBType;
import org.jooq.Name;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class SQLQueryHelper {
    public static SQLDialect getDialect(DBType dbType) {
        switch (dbType) {
            case MYSQL:
                return SQLDialect.MYSQL;
            case POSTGRES:
                return SQLDialect.POSTGRES;
            default:
                return SQLDialect.DEFAULT;
        }
    }

    public static Name getQualifiedTableName(String tableName, String schema, DBType dbType) {
        /**
         * Note:
         * 'foo' is an SQL string
         * "foo" is an SQL identifier (column/table/etc)
         * [foo] is an identifier in MS SQL
         * `foo` is an identifier in MySQL
         */
        switch (dbType) {
            case MYSQL:
                return DSL.table(DSL.name(tableName)).getQualifiedName();
            case POSTGRES:
                if (StringUtil.isNullOrEmpty(schema)) {
                    // set schema to "public" as default
                    return DSL.table(DSL.name("public", tableName)).getQualifiedName();
                } else {
                    return DSL.table(DSL.name(schema, tableName)).getQualifiedName();
                }
            case SQLSERVER:
                if (StringUtil.isNullOrEmpty(schema)) {
                    return DSL.name(String.format("[%s]", tableName)).unquotedName();
                } else {
                    return DSL.name(String.format("[%s].[%s]", schema, tableName)).unquotedName();
                }
            default:
                return DSL.table(DSL.name(schema, tableName)).getQualifiedName();
        }
    }

    public static String getCountTableRecordsSQL(String tableName, String schema, DBType dbType) {
        SQLDialect dialect = getDialect(dbType);
        Name qualifiedName = getQualifiedTableName(tableName, schema, dbType);

        return DSL.using(dialect)
                .select(DSL.count(DSL.asterisk()))
                .from(qualifiedName)
                .getSQL();
    }
}
