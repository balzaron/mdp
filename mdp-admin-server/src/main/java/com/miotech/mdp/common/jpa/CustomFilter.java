package com.miotech.mdp.common.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import java.util.List;

public class CustomFilter<T> {
    private static final String UDF_ARRAY_CONTAINS = "array_contains";
    private static final String UDF_ARRAY_OVERLAP = "array_overlap";
    private static final String UDF_TS_TEXT_MATCH = "ts_text_match";
    private static final String UDF_TS_QUERY_MATCH = "ts_query_match";

    public Expression<Boolean> udfArrayContains(CriteriaBuilder cb,
                                                Expression<T> expression,
                                                List<String> array) {
        // TODO: find using array, hibernate do not support postgres array [],
        //  cast as text alternatively
        return cb.function(UDF_ARRAY_CONTAINS,
                Boolean.class,
                expression,
                cb.literal(String.join(",", array))
        );
    }

    public Expression<Boolean> udfArrayOverlap(CriteriaBuilder cb,
                                                Expression<T> expression,
                                                List<String> array) {
        return cb.function(UDF_ARRAY_OVERLAP,
                Boolean.class,
                expression,
                cb.literal(String.join(",", array))
        );
    }

    public Expression<Boolean> udfTsFilter(CriteriaBuilder cb,
                                           Expression<T> expression,
                                           String searchQuery) {
        return cb.function(UDF_TS_TEXT_MATCH,
                Boolean.class,
                expression,
                cb.literal(searchQuery)
        );
    }
}
