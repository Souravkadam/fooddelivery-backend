package in.souravkadam.foodiesapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Forces LocalDateTime to serialize as ISO-8601 string ("2025-06-01T14:30:00")
 * instead of the default array format ([2025,6,1,14,30,0]).
 * This is critical for the admin dashboard charts to parse dates correctly.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * This customizer integrates with Spring Boot's auto-configured ObjectMapper
     * so it applies to ALL Jackson serialization (REST responses, etc.)
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
