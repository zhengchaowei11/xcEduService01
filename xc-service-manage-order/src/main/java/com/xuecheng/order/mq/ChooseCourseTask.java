package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {
    @Autowired
    TaskService taskService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);


    //

    @Scheduled(cron = "0/3 * * * * * ")
    public void  sendChoosecourseTask(){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();


        List<XcTask> taskList = taskService.findTaskList(time, 10);
        System.out.println(taskList);

        for (XcTask xcTask : taskList){
            if (taskService.getTask(xcTask.getVersion(),xcTask.getId()) > 0){
                taskService.publish(xcTask.getMqExchange(),xcTask.getMqRoutingkey(),xcTask);
            }
        }

    }



    //@Scheduled(cron = "0/3 * * * * * ") //秒，分，时，月中的天 ，月，周中的天
   //@Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次任务
    public void task1(){
        LOGGER.info("测试定时任务1开始---------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("测试定时任务1结束----------------");
    }
    //@Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次任务
    public void task2(){
        LOGGER.info("测试定时任务2开始---------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("测试定时任务2结束----------------");
    }

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void  receiveFinishChoosecourseTask(XcTask xcTask){
        taskService.finishTask(xcTask.getId());
    }
}
