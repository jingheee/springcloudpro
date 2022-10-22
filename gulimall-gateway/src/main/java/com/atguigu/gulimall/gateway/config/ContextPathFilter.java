/*
 *    Copyright [2022] [lazyd0g]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.atguigu.gulimall.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * @Auther: lazyd0g
 * @Date: 2022/10/22
 */
@Configuration
public class ContextPathFilter {
    private String contextpath;

    @Value("${gateway.contextpath:}")
    private void initContextpath(String path) {
        this.contextpath = cleanContextPath(path);
        checkContextPath(path);
    }


    private String cleanContextPath(String contextPath) {
        String candidate = null;
        if (StringUtils.hasLength(contextPath)) {
            candidate = contextPath.strip();
        }
        if (StringUtils.hasText(candidate) && candidate.endsWith("/")) {
            return candidate.substring(0, candidate.length() - 1);
        }
        return candidate;
    }

    private void checkContextPath(String contextPath) {
        Assert.notNull(contextPath, "ContextPath must not be null");
        if (!contextPath.isEmpty()) {
            if ("/".equals(contextPath)) {
                throw new IllegalArgumentException("Root ContextPath must be specified using an empty string");
            }
            if (!contextPath.startsWith("/") || contextPath.endsWith("/")) {
                throw new IllegalArgumentException("ContextPath must start with '/' and not end with '/'");
            }
        }
    }

    @Bean
    @Order(-1)
    public WebFilter apiPrefixFilter() {
        return (exchange, chain) -> {
            if (StringUtils.hasText(contextpath)) {
                ServerHttpRequest request = exchange.getRequest();
                String rawPath = request.getURI().getRawPath();
                if (!rawPath.startsWith(contextpath)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ContextPath does not match the request"));
                }
                String newPath = rawPath.substring(contextpath.length());
                ServerHttpRequest newRequest = request.mutate().path(newPath).build();
                return chain.filter(exchange.mutate().request(newRequest).build());
            } else {
                return chain.filter(exchange);
            }
        };
    }

}
