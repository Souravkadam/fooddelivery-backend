package in.souravkadam.foodiesapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class AWSConfig {

    @Value("${aws.access.key:dummy}")
    private String accessKey;

    @Value("${aws.secret.key:dummy}")
    private String secretKey;

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Value("${storage.local:true}")
    private boolean useLocalStorage;

    @Bean
    public S3Client s3Client() {
        if (useLocalStorage) {
            // Local storage mode — build a client that never makes real network calls.
            // FoodServiceImpl checks useLocalStorage before calling any S3 method,
            // so this bean is injected but never actually used.
            return S3Client.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create("http://localhost:9999"))
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("dummy", "dummy")
                        )
                    )
                    .build();
        }

        // Real S3 — only used when storage.local=false
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                    )
                )
                .build();
    }
}
