package com.autotest.sonicclient.utils;

import android.util.Log;

import com.autotest.sonicclient.application.ApplicationImpl;

import org.jetbrains.annotations.NotNull;

import io.minio.BucketExistsArgs;
import io.minio.DownloadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

public class MinioUtil {
    private static final String TAG = "minioUtil";
//    static String ACCESS_KEY = "Q3AM3UQ867SPQQA43P2F";
//    static String SECRET_KEY = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";
//    static String ENDPOINT = "https://play.min.io";
    static String ACCESS_KEY = "SoHeRJJdCXiWsnt4vOAD";
    static String SECRET_KEY = "9FnySk77jfrgvBRohqMNo9HJ5mNznf1zPMBUMlE4";
    static String ENDPOINT = "http://172.16.142.208:9000";

    private MinioUtil() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        String accessKey = ACCESS_KEY;
        String secretKey = SECRET_KEY;
        String endpoint = ENDPOINT;
        @NotNull
        String bucketName = "";

        Builder() {
        }

        public Builder setBucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder setAccessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder setSecretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public MinIoClient build() {
            return new MinIoClient(this);
        }
    }

    public static class MinIoClient {
        private final MinioClient minioClient;
        String accessKey;
        String secretKey;
        String endpoint;
        String bucketName;

        MinIoClient(Builder builder) {
            this.bucketName = builder.bucketName.isEmpty() ? ApplicationImpl.getInstance().getPackageName() : builder.bucketName;
            this.accessKey = builder.accessKey;
            this.secretKey = builder.secretKey;
            this.endpoint = builder.endpoint;
            minioClient =
                    MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();
        }

        public void createBucket() throws Exception{
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket .
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//                LogUtil.i(TAG, "checkBucket: create bucket: " + bucketName);
            } else {
//                LogUtil.i(TAG, "checkBucket: bucket already exists: " + bucketName);
            }
        }

        public String upload(String source, String target) throws MinioException, Exception {
            createBucket();
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(target)
                            .filename(source)
                            .build());
//            LogUtil.d(TAG, String.format("upload: source: %s, target: %s", source, target));
            return String.format("%s/%s/%s", endpoint, bucketName, target);
        }

        public void download(String from, String to) throws Exception{
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(from)
                            .filename(to)
                            .build());
        }
    }
}
