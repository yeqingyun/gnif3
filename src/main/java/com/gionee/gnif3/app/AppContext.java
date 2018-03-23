package com.gionee.gnif3.app;


import com.gionee.gnif3.context.Context;
import com.gionee.gnif3.context.ThreadContextHolder;
import com.gionee.gnif3.db.DatabaseIdGenerator;
import com.gionee.gnif3.db.IdGenerator;

/**
 * Created by doit on 2016/4/19.
 */
public class AppContext {


    /**
     * 获取当前上下文的ID生成器
     *
     * @return 当前上下文的ID生成器
     */
    public static IdGenerator getIdGenerator() {
        return ThreadContextHolder.getContext().getSession().getDbSqlSessionFactory().getIdGenerator();
    }

    /**
     * 获取指定数据源的ID生成器
     *
     * @param dsConfigName 数据源名称
     * @return 指定数据源的ID生成器
     */
    public static IdGenerator getIdGenerator(String dsConfigName) {
        if (Context.sqlSessionFactoryMap.containsKey(dsConfigName)) {
            return Context.sqlSessionFactoryMap.get(dsConfigName).getIdGenerator();
        }

        return new DatabaseIdGenerator(dsConfigName);
    }

    public static Integer getUserId() {
        return null;
    }


}
