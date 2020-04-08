package com.miotech.mdp.flow.service;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {
    @Autowired
    private AmazonS3 s3Client;

    public void copyFolders(String bucketName, String srcFolder, String targetBucket, String targetFolder) {
        s3Client.listObjects(bucketName, srcFolder)
                .getObjectSummaries()
                .forEach( f -> {
                    String fileName;
                    String fileKey = f.getKey();
                    if (fileKey.equals(srcFolder)) {
                        fileName = fileKey.substring(fileKey.lastIndexOf("/") + 1);
                    } else {
                        fileName = fileKey.replace(srcFolder, "");
                    }
                    String targetFile = buildPrefix(targetFolder, fileName);
                    s3Client.copyObject(bucketName, f.getKey(), targetBucket, targetFile);
                });
    }

    private String buildPrefix(String... path) {
        List<String> pieces = Arrays.stream(path)
                .map(x -> x
                        .replaceAll("/$", "")
                        .replaceAll("^/", "")
                )
                .collect(Collectors.toList());
        return String.join("/", pieces);
    }
}
