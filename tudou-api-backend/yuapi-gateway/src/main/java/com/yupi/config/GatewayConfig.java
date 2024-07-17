package com.yupi.config;

import com.yupi.yuapigateway.InterfaceInvokeFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder, InterfaceInvokeFilter filter) {
            //用路由前缀区分路由来源是前端还是接口管理平台
            return builder.routes()
                    .route(r ->
                            r.path("/api/interface/**")
                                    .filters(f -> f.filter(filter))
                                    .uri("http://localhost:8123")
                    )
                    .build();
        }

    }