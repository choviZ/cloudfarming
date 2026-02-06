package com.vv.cloudfarming.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 权限认证配置
 */
@Configuration
public class SaTokenConfigure {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
            // 拦截地址
            .addInclude("/**")
            // 开放地址
            .addExclude("/favicon.ico", "/doc.html", "/webjars/**", "/swagger-resources/**")
            // 鉴权方法：每次请求进入 Controller 之前，该方法都会被调用
            .setAuth(obj -> {
                // 手动放行 Swagger 相关请求，避免 PatternParser 报错
                String path = SaHolder.getRequest().getRequestPath();
                if (path.contains("/v3/api-docs")) {
                    return;
                }

                // 登录校验 -- 拦截所有路由，并排除开放接口
                SaRouter.match("/**")
                        .notMatch(
                            "/api/user/login",
                            "/api/user/register",
                            "/api/spu/**", // 商品详情/列表
                            "/api/category/**" // 分类
                        )
                        .check(r -> StpUtil.checkLogin());
            })
            // 异常处理方法：每次 setAuth 函数出现异常时进入
            .setError(e -> {
                return SaResult.error(e.getMessage());
            });
    }
}
