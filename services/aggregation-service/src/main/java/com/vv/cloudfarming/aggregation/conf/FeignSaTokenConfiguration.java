package com.vv.cloudfarming.aggregation.conf;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignSaTokenConfiguration {

    /**
     * feign远程调用丢失上下文的解决方案，新增拦截器
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate requestTemplate) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String cookie = request.getHeader("Cookie");
                requestTemplate.header("Cookie", cookie);
            }
        };
    }
}
