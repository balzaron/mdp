package com.miotech.mdp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiotechMDPServer.class, TestConfiguration.class})
@ActiveProfiles("test")
public abstract class ServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ServiceTest.class);

    @Autowired
    private DataSource dataSource;

    private static List<String> tablesOfDataSource = null;

    @After
    public void tearDown() throws Exception {
        truncateAllTables();
    }

    private void truncateAllTables() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        for (String t : getAllTables(dataSource)) {
            template.execute(String.format("TRUNCATE %s;", t));
        }
    }

    private List<String> getAllTables(DataSource dataSource) {
        if (tablesOfDataSource != null) {
            return tablesOfDataSource;
        }

        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM pg_catalog.pg_tables" +
                            " WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'");
            while (rs.next()) {
                String tableName = rs.getString("tablename");

                // ignore 'flyway' tables
                if (tableName.startsWith("flyway")) {
                    continue;
                }

                tables.add(tableName);
            }
            tablesOfDataSource = ImmutableList.copyOf(tables);
            return tablesOfDataSource;
        } catch (SQLException e) {
            log.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }
}
