package com.datahub.config;

import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

/**
 * Quartz调度器配置
 *
 * @author DataHub Team
 */
@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 配置Job Factory
     */
    @Bean
    public JobFactory jobFactory() {
        return new AutowiringSpringBeanJobFactory(applicationContext);
    }

    /**
     * 配置Scheduler Factory
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        return factory;
    }

    /**
     * 获取Scheduler
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws Exception {
        return factory.getScheduler();
    }

    /**
     * Quartz配置属性
     */
    private Properties quartzProperties() {
        Properties properties = new Properties();
        
        // Scheduler配置
        properties.setProperty("org.quartz.scheduler.instanceName", "DataHubScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        
        // ThreadPool配置
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        
        // JobStore配置(使用内存存储)
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        
        return properties;
    }
}
