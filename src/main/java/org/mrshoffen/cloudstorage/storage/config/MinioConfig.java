package org.mrshoffen.cloudstorage.storage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.user}")
    private String user;

    @Value("${minio.password}")
    private String password;

    @Value("${minio.bucket-name}")
    private String bucket;

    @Bean
    @SneakyThrows
    public MinioClient minioClient() {
        MinioClient client = MinioClient
                .builder()
                .endpoint(endpoint)
                .credentials(user, password)
                .build();

        boolean bucketExists = client.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build());

        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }

        return client;
    }
}
