package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHitRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    XcTaskHitRepository xcTaskHitRepository;

    public List<XcTask> findTaskList(Date updateTime,int size){
        Pageable pageable = PageRequest.of(0,size);
        Page<XcTask> repositoryByUpdateTimeBefore = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return repositoryByUpdateTimeBefore.getContent();
    }

   @Transactional
   public void publish(String exchange,String routingKey,XcTask xcTask){
       Optional<XcTask> optionalXcTask = xcTaskRepository.findById(xcTask.getId());
       if (optionalXcTask.isPresent()){
           rabbitTemplate.convertAndSend(exchange,routingKey,xcTask);
           XcTask one = optionalXcTask.get();
           one.setUpdateTime(new Date());
           xcTaskRepository.save(one);

       }
   }

   @Transactional
   public int getTask(int version,String id){
       int i = xcTaskRepository.updateTaskVersion(version, id);
       return i;
   }

   @Transactional
    public void finishTask(String taskId){
       Optional<XcTask> taskRepositoryById = xcTaskRepository.findById(taskId);
       if (taskRepositoryById.isPresent()){
           XcTask xcTask = taskRepositoryById.get();
           XcTaskHis xcTaskHis = new XcTaskHis();
           BeanUtils.copyProperties(xcTask,xcTaskHis);
           xcTaskHitRepository.save(xcTaskHis);
           xcTaskRepository.delete(xcTask);
       }
   }
}
