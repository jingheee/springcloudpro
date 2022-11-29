///*
// *    Copyright [2022] [lazyd0g]
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package com.atguigu.gulimall.product.config;
//
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
//import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.core.task.support.TaskExecutorAdapter;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadFactory;
//
//@Configuration
//public class VirtualThreadConfig {
//    private static final ThreadFactory tomcat = new ThreadFactoryBuilder()
//            .setNameFormat("vt-pool-%d")
//            .build();
//
//    private static final ThreadFactory async = new ThreadFactoryBuilder()
//            .setNameFormat("vt-pool-%d")
//            .build();
//
//    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
//    public AsyncTaskExecutor asyncTaskExecutor() {
//        ExecutorService executorService = Executors.newThreadPerTaskExecutor(async);
//        return new TaskExecutorAdapter(executorService);
//    }
//
//    @Bean
//    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
//        ExecutorService executorService = Executors.newThreadPerTaskExecutor(tomcat);
//        return protocolHandler -> protocolHandler.setExecutor(executorService);
//    }
//}
