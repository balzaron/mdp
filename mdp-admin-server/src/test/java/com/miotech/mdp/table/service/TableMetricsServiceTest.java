package com.miotech.mdp.table.service;

import com.miotech.mdp.ServiceTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class TableMetricsServiceTest extends ServiceTest {
    @Autowired
    TableService tableService;

    @Autowired
    TableMetricsService tableMetricsService;

    @Test
    public void testGetRecordsCount() {
        Integer count = tableMetricsService.getRecordsCount(
                tableService.find("3057810208194560") // cn_company_data
        );
        assertEquals(true, Objects.nonNull(count));
        Assertions.assertThat(count).isGreaterThanOrEqualTo(0);
        System.out.println("Count for cn_company_data: " + count.toString());
    }
}
