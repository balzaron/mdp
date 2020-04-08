package com.miotech.mdp.common.client.metabase;

import com.miotech.mdp.common.models.protobuf.metabase.*;

import java.lang.annotation.Native;
import java.util.Map;

public class MetabaseCardRequestFactory {

    public static MetabaseCardRequest createRequest(
            String name,
            String description,
            int collectionId,
            int databaseId,
            String query) {
        return createRequest(name,
                description,
                collectionId,
                databaseId,
                query,
                null,
                "native",
                DisplayType.table, null);
    }

    public static MetabaseCardRequest createRequest(
            String name,
            String description,
            int collectionId,
            int databaseId,
            String query,
            Map<String, TemplateTag> templateTagMap,
            String queryType) {
        return createRequest(name,
                description,
                collectionId,
                databaseId,
                query,
                templateTagMap,
                queryType,
                DisplayType.table, null);
    }

    public static MetabaseCardRequest createRequest(
            String name,
            String description,
            int collectionId,
            int databaseId,
            String query,
            Map<String, TemplateTag> templateTagMap,
            String queryType,
            DisplayType displayType) {
        return createRequest(name,
                description,
                collectionId,
                databaseId,
                query,
                templateTagMap,
                queryType,
                displayType, null);
    }

    public static MetabaseCardRequest createRequest(
            String name,
            String description,
            int collectionId,
            int databaseId,
            String query,
            Map<String, TemplateTag> templateTagMap,
            String queryType,
            DisplayType displayType,
            VisualizationSettings vzSettings
            ) {
        DatasetQuery datasetQuery = createDatasetQuery(databaseId, query, templateTagMap,null, queryType);

        if (vzSettings == null) {
            vzSettings = VisualizationSettings.getDefaultInstance();
        }

        return MetabaseCardRequest.newBuilder()
                .setName(name)
                .setCollectionId(collectionId)
                .setDisplay(displayType)
                .setDescription(description)
                .setDatasetQuery(
                     datasetQuery
                )
                .setVisualizationSettings(vzSettings)
                .build();
    }

    public static DatasetQuery createDatasetQuery(Integer databaseId,
                                                  String query,
                                                  Map<String, TemplateTag> templateTagMap) {
        return createDatasetQuery(databaseId, query, templateTagMap, null, null);
    }

    public static DatasetQuery createDatasetQuery(Integer databaseId,
                                                  String query,
                                                  Map<String, TemplateTag> templateTagMap,
                                                  String collection,
                                                  String queryType) {
        if (queryType == null) {
            queryType = "native";
        }
        DatasetQuery.Builder datasetBuilder = DatasetQuery.newBuilder()
                .setDatabase(databaseId)
                .setType(queryType);
        if (queryType.equals("native")) {
            datasetBuilder.setNative(createNativeQuery(query, templateTagMap, collection));
        } else {
            datasetBuilder.setNative(NativeQuery.getDefaultInstance());
        }
        return datasetBuilder.build();
    }

    public static NativeQuery createNativeQuery(String query,
                                                Map<String, TemplateTag> templateTagMap) {
        return createNativeQuery(query, templateTagMap, null);
    }

    public static NativeQuery createNativeQuery(String query,
                                                Map<String, TemplateTag> templateTagMap,
                                                String collection) {
        NativeQuery.Builder nativeBuilder= NativeQuery.newBuilder()
                .setQuery(query);
        if (templateTagMap != null ){
            nativeBuilder.putAllTemplateTags(templateTagMap);
        }
        if (collection != null ) {
            nativeBuilder.setCollection(collection);
        }
        return nativeBuilder.build();
    }

    /**
     * Create template tag as parameter to sql
     * @param name
     * @param display
     * @param typeName
     * @param isReuqired
     * @return
     */
    public static TemplateTag createTemplateTag(String name,
                                         String display,
                                         String typeName,
                                         Boolean isReuqired){
        return TemplateTag.newBuilder()
                .setRequired(isReuqired)
                .setDisplayName(display)
                .setType(typeName)
                .setName(name)
                .build();
    }
}
