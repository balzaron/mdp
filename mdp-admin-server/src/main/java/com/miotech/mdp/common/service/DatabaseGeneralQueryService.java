package com.miotech.mdp.common.service;


import com.miotech.mdp.common.client.metabase.MetabaseCardRequestFactory;
import com.miotech.mdp.common.client.metabase.MetabaseClient;
import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.models.protobuf.metabase.DatasetQuery;
import com.miotech.mdp.common.models.protobuf.metabase.QueryData;
import com.miotech.mdp.common.models.protobuf.metabase.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DatabaseGeneralQueryService {

    @Autowired @Lazy
    private MetabaseClient metabaseClient;

    public QueryData queryData(Integer dbId, String sql) throws InvalidQueryException {
        DatasetQuery query = MetabaseCardRequestFactory.createDatasetQuery(
                dbId, sql, new HashMap<>()
        );
        QueryResult result = metabaseClient.queryDataset(query);
        if (result.getStatus().equals("failed")) {
            throw new InvalidQueryException("");
        }
        return result.getData();
    }
}