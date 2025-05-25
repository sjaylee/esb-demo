package com.diwgroup.downstreamservice;


import jakarta.jms.ConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;


@Component
public class DownstreamRoute extends RouteBuilder {

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


        // 处理同步请求并异步写MQ
        from("jetty:http://localhost:8081/downstream")
                .routeId("main-processing-route")
                .log(LoggingLevel.INFO, "接收到ESB请求: ${body}")
                .process(exchange -> {
                    // 业务处理逻辑
                    String body = exchange.getIn().getBody(String.class);
                    String processedMsg = "[Processed] " + body;
                    exchange.getMessage().setBody(processedMsg);

                    // 将原始消息存入Header供异步使用
                    exchange.setProperty("originalMessage", body);
                })
                .log(LoggingLevel.INFO, "业务处理完成，响应内容: ${body}")

//                // 异步发送到MQ（不阻塞主线程）
//                .wireTap("direct:async-mq-writer")
//                    .executorService("asyncThreadPool")
//                .end()

                // 同步返回响应给ESB
                .log("Response Sent to ESB");

//        // 异步写入MQ的路由
//        from("direct:async-mq-writer")
//                .routeId("async-mq-writer-route")
//                .errorHandler(defaultErrorHandler()
//                        .maximumRedeliveries(3)
//                        .redeliveryDelay(1000)
//                        .retryAttemptedLogLevel(LoggingLevel.WARN)
//                        .logExhausted(true)
//                        .logStackTrace(true)
//                )
//                .threads().executorService("asyncThreadPool")
//                .log(LoggingLevel.INFO, "开始异步写入MQ")
//                .setBody(exchange -> exchange.getProperty("originalMessage"))
//
//                .to("activemq:queue:ESB_MESSAGES?" +
//                        "deliveryPersistent=true&" +
//                        "username=${spring.activemq.user}&" +
//                        "password=${spring.activemq.password}")
//
//                .log(LoggingLevel.INFO, "消息已存入MQ: ${body}");

    }
}