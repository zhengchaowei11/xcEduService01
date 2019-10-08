package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    //变成常量了
    public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";

    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    @Value("${xuecheng.mq.queue}")
    //队列的名字
    public String queue_cms_postpage_name;
    //routingKey 即站点Id
    @Value("${xuecheng.mq.routingKey}")
    public String routingKey;
    /**
     * 交换机配置使用direct类型
     * durable 是持久化的意思
     * @return the exchange
     */
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EXCHANGE_TOPICS_INFORM() {
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }
    //声明队列
    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE() {
        Queue queue = new Queue(queue_cms_postpage_name);
        return queue;
    }

    //交换器绑定消息队列
    //@Qualifier  进行消息的绑定
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE) Queue queue,@Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
    }
}
