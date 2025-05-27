package com.diwgroup.esbservice;


import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class ESBRoute {


    // 定义一个简单的路由，从收到外部HTTP请求并发送到下游应用,当下游应用接受并返回消息之后，ESB 将消息异步存于MQ中
    @Bean
    public RouteBuilder httpToJmsRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {


                // 全局异常处理
                onException(Exception.class)
                        .handled(true)
                        .logStackTrace(true)
                        .logExhausted(true)
                        .log(LoggingLevel.ERROR, "处理消息时发生异常: ${exception.message}")
                        .setBody().constant("处理失败，请稍后重试")
                        .setHeader("ResponseCode").constant(500);



                from("jetty:http://0.0.0.0:8080/esb")
//                from("servlet://esb")
                        .setBody(body().convertToString()) // 强制读取原始文本
                        .log("ESB 收到请求: ${body}")
                        .to("http://{{downstream.url}}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                        .choice()
                        .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                        .log("发送下游应用成功: ${body}")

                        // 异步发送到MQ（不阻塞主线程）
                        .wireTap("direct:async-mq-writer")
                        .executorService("asyncThreadPool")
                        .end()

                        .otherwise()
                        .log("发送下游应用失败: ${exception.message}")
                        .end();


                // 异步写入MQ的路由
                from("direct:async-mq-writer")
                        .routeId("async-mq-writer-route")
                        .errorHandler(defaultErrorHandler()
                                .maximumRedeliveries(3) // 失败重试策略
                                .redeliveryDelay(1000)
                                .retryAttemptedLogLevel(LoggingLevel.WARN)
                                .logExhausted(true)
                                .logStackTrace(true)
                        )
                        .threads().executorService("asyncThreadPool")
                        .log(LoggingLevel.INFO, "开始异步写入MQ")
                        .setBody(exchange -> exchange.getProperty("originalMessage"))

                        // send to MQ, topic is 'ESB_MESSAGES'
                        .to("activemq:queue:ESB_MESSAGES?" +
                                "deliveryPersistent=true&" +
                                "username=${spring.activemq.user}&" +
                                "password=${spring.activemq.password}")

                        .log(LoggingLevel.INFO, "消息已存入MQ: ${body}");


            }
        };
    }



}