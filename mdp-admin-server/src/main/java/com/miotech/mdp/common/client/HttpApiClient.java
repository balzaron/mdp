package com.miotech.mdp.common.client;

import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

public abstract class HttpApiClient {

    private RestTemplate restClient = initRestClient();

    public abstract String getBase();

    private RestTemplate initRestClient() {
        RestTemplate restClient = new RestTemplate();
        restClient.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restClient;
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        return headers;
    }

    protected ResponseEntity<String> post(String url, String message) {
        String payload = null;
        if (message!=null) {
            payload = message;
        } else {
            payload = "{}";
        }

        return request(url,
                HttpMethod.POST,
                new HttpEntity<>(payload, createHeaders()));
    }

    protected ResponseEntity<String> put(String url, String message){
        return request(url,
                HttpMethod.PUT,
                new HttpEntity<>(message, createHeaders()));
    }

    protected ResponseEntity<String> get(String url) {
        return request(url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()));
    }

    protected ResponseEntity<String> delete(String url) {
        return request(url,
                HttpMethod.DELETE,
                new HttpEntity<>(createHeaders()));
    }

    protected ResponseEntity<String> head(String url) {
        return request(url,
                HttpMethod.HEAD,
                new HttpEntity<>(createHeaders()));
    }

    protected ResponseEntity<String> request(String url,
                                           HttpMethod method,
                                           HttpEntity<String> entity) {
        ResponseEntity<String> responseEntity = restClient.exchange(url,
                method,
                entity,
                String.class);
        if (responseEntity.getStatusCodeValue() < 200 ||
                responseEntity.getStatusCodeValue() >= 400 ) {
            throw new RuntimeException("Http API ERROR: " + responseEntity.getBody());
        }
        return responseEntity;
    }

    protected String buildUrl(String api) {
        String baseUrl = this.getBase();
        if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl;
        }
        return baseUrl + api;
    }

    protected String getBody(ResponseEntity<String> response) {
        return response.getBody();
    }
}
