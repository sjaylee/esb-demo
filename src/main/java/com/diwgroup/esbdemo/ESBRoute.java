package com.diwgroup.esbdemo;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ESBRoute extends RouteBuilder {


    /**
     * 下游应用和MQ的IP地址
     */
    @Value("${ecs2.ip}")
    private String ecs2_IP;

    @Override
    public void configure() {
        // 接收本地请求，转发至ECS2的下游服务
        from("jetty:http://0.0.0.0:8080/esb")
                .log("Received request: ${body}")
                .to("http://" + ecs2_IP + ":8081/downstream?bridgeEndpoint=true")
                .log("Forwarded response: ${body}");
    }
}