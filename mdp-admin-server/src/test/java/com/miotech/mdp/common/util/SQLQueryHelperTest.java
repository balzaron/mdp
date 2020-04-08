package com.miotech.mdp.common.util;

import com.miotech.mdp.common.models.protobuf.schema.DBType;
import org.junit.Test;

import static com.miotech.mdp.common.util.SQLQueryHelper.getCountTableRecordsSQL;
import static org.junit.Assert.*;

public class SQLQueryHelperTest {
    @Test
    public void testGetCountTableRecordsQueryString() {
        assertEquals(
                "select count(*) from `foo`",
                getCountTableRecordsSQL("foo", "", DBType.MYSQL)
        );
        assertEquals(
                "select count(*) from \"dm\".\"cn_narrative_dev_20191030\"",
                getCountTableRecordsSQL("cn_narrative_dev_20191030", "dm", DBType.ATHENA)
        );
        assertEquals(
                "select count(*) from \"public\".\"foo\"",
                getCountTableRecordsSQL("foo", "public", DBType.POSTGRES)
        );
        assertEquals(
                "select count(*) from \"public\".\"bar\"",
                getCountTableRecordsSQL("bar", "", DBType.POSTGRES)
        );
        assertEquals(
                "select count(*) from \"example\"",
                getCountTableRecordsSQL("example", null, DBType.RDBMS)
        );
        assertEquals(
                "select count(*) from [example]",
                getCountTableRecordsSQL("example", null, DBType.SQLSERVER)
        );
        assertEquals(
                "select count(*) from [t].[example]",
                getCountTableRecordsSQL("example", "t", DBType.SQLSERVER)
        );
    }
}
