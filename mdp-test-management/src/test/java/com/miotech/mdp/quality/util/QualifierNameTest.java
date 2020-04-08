package com.miotech.mdp.quality.util;

import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.util.SQLQueryHelper;
import org.assertj.core.api.Assertions;
import org.jooq.Name;
import org.junit.Test;

/**
 * @author: shanyue.gao
 * @date: 2020/4/7 1:31 PM
 */
public class QualifierNameTest {

    @Test
    public void testQuery() {
        String tableName = "table1";
        String schema = "dm";
        String dbTypeTmp = "athena".toUpperCase();
        DBType dbType = DBType.valueOf(dbTypeTmp);

        Name name = SQLQueryHelper.getQualifiedTableName(tableName, schema, dbType);
        String naming =  name.unquotedName().toString();
        System.out.println(naming);

        Assertions.assertThat(naming)
                .isEqualTo("dm.table1");
    }

    @Test
    public void testQuery1() {
        String tableName = "table1";
        String schema = "dm";
        String dbTypeTmp = "mysql".toUpperCase();
        DBType dbType = DBType.valueOf(dbTypeTmp);
        Name name = SQLQueryHelper.getQualifiedTableName(tableName, schema, dbType);
        String naming =  name.unquotedName().toString();
        System.out.println(naming);

        Assertions.assertThat(naming)
                .isEqualTo(tableName);
    }
}
