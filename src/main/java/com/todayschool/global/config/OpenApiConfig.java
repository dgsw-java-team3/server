package com.todayschool.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todaySchoolOpenApi() {
        return new OpenAPI().info(new Info()
                .title("오늘의 학교(TodaySchool) API")
                .description("급식 만족도, 실습실 예약, 분실물 게시판 REST API")
                .version("v1"));
    }
}
