package com.gionee.gnif3.job;

import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.utils.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;

/**
 * 定时任务管理类
 * Created by Leon on 2016/5/6.
 */
public class ScheduleManager {

    private static Scheduler scheduler = null;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            throw new GnifRuntimeException("初始化定时任务执行器失败", e);
        }
    }

    public static void addSchedule(String jobName, Class<? extends Job> jobClass, String cronExpression) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName).build();
            Trigger trigger = TriggerBuilder.newTrigger().forJob(jobName).withIdentity(jobName + "Trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            throw new GnifRuntimeException("添加定时任务时出现错误", e);
        }
    }

    public static void updateSchedule(String jobName, String cronExpression) {
        if (!StringUtils.hasText(cronExpression)) {
            return;
        }

        try {
            TriggerKey triggerKey = new TriggerKey(jobName + "Trigger");
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (!cronExpression.equals(trigger.getCronExpression())) {
                CronTriggerImpl cronTrigger = (CronTriggerImpl) trigger;
                cronTrigger.setCronExpression(cronExpression);
                scheduler.resumeTrigger(triggerKey);
            }
        } catch (SchedulerException e) {
            throw new GnifRuntimeException("修改定时任务 " + jobName + "时出现错误", e);
        } catch (ParseException e) {
            throw new GnifRuntimeException("修改定时任务" + jobName + " 时间表达式时出现解析错误", e);
        }
    }

    public static void removeSchedule(String jobName) {
        try {
            JobKey jobKey = new JobKey(jobName);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new GnifRuntimeException("移除定时任务" + jobName + " 时出现错误", e);
        }
    }

    public static void shutDownAll(boolean waitForJobsToComplete) {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown(waitForJobsToComplete);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }


}
