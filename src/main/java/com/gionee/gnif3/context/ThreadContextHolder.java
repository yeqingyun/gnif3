package com.gionee.gnif3.context;


import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.db.DsConfig;

/**
 * Created by doit on 2016/4/19.
 */
public class ThreadContextHolder {

    public static ThreadLocal<ThreadContext> threadConfig = new ThreadLocal<>();

    public static ThreadContext getContext() {
        ThreadContext threadContext = threadConfig.get();
        if (threadContext == null) {
            threadContext = new ThreadContext();
            threadConfig.set(threadContext);
        }

        return threadContext;
    }

    public static void clear() {
        if (threadConfig.get() != null && threadConfig.get().isSessionEmpty()) {
            threadConfig.remove();
        }
    }

    /**
     * 调用此方法生成的session需要手动关闭
     *
     * @param dsConfig
     * @return
     */
    public static DbSqlSession newSession(DsConfig dsConfig) {
        ThreadContext threadContext = ThreadContextHolder.getContext();
        threadContext.requiredSession(dsConfig);
        return threadContext.getSession();
    }


}
