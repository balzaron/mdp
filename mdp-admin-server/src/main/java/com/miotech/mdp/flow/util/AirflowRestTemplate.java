package com.miotech.mdp.flow.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AirflowRestTemplate {

    @Value("${airflow.host}")
    private String HOST;

    private String TEST_URL = "/api/experimental/test";

    private String REFRESH_DAG_URL = "/admin/airflow/refresh?dag_id=";

    private String DELETE_DAG_URL = "/api/experimental/dags/";

    @Autowired
    private RestTemplate restTemplate;

    public void test() {
        ResponseEntity entity = restTemplate.getForEntity(HOST + TEST_URL, String.class);
        entity.getStatusCode();
    }

    public void refreshDAG(String dagId) {
        restTemplate.getForObject(HOST + REFRESH_DAG_URL + dagId, String.class);
    }

    public void deleteDAG(String dagId) {
        restTemplate.delete(HOST + DELETE_DAG_URL + dagId);
    }
}
