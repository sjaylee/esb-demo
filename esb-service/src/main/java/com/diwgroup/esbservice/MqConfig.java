package com.diwgroup.esbservice;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.spi.ThreadPoolProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class MqConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    /**
     * 下游应用和MQ的IP地址
     */
    @Value("${threadPoolProfile.poolSize}")
    private int poolSize;

    @Value("${threadPoolProfile.maxPoolSize}")
    private int maxPoolSize;

    @Value("${threadPoolProfile.keepAliveTime}")
    private long keepAliveTime;

    // 创建ActiveMQ连接工厂
    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);
        return factory;
    }

    // 创建缓存连接工厂
    @Primary
    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(activeMQConnectionFactory());
        cachingFactory.setSessionCacheSize(10);
        return cachingFactory;
    }

    // 配置Camel线程池
    @Bean
    public ThreadPoolProfile asyncThreadPoolProfile() {
        ThreadPoolProfile profile = new ThreadPoolProfile();
        profile.setId("asyncThreadPool");// 线程池 ID，需与路由中的引用一致
        profile.setPoolSize(poolSize);// 核心线程数
        profile.setMaxPoolSize(maxPoolSize);// 最大线程数
        profile.setKeepAliveTime(keepAliveTime);// 空闲线程存活时间（秒）
        profile.setTimeUnit(TimeUnit.SECONDS); // 时间单位
        return profile;
    }
}

