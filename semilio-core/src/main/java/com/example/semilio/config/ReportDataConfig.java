package com.example.semilio.config;

import com.example.semilio.report.response.ReportCategoryResponse;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class ReportDataConfig {

    @Component
    @ConfigurationProperties(prefix = "report")
    @PropertySource(value = "classpath:message/report-categories_pl.yml", factory = YamlPropertySourceFactory.class)
    @Data
    public static class PolishCategories {
        public List<ReportCategoryResponse> categories;
    }

//    @Component
//    @ConfigurationProperties(prefix = "report")
//    @PropertySource(value = "classpath:message/categories_en.yml", factory = YamlPropertySourceFactory.class)
//    @Data
//    public static class EnglishCategories {
//        private List<ReportCategoryResponse> categories;
//    }
}
