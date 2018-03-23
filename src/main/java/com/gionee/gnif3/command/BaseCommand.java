package com.gionee.gnif3.command;

import com.gionee.gnif3.config.AppConfig;
import com.gionee.gnif3.context.ContextFactory;
import com.gionee.gnif3.context.IContext;
import com.gionee.gnif3.context.ThreadContext;
import com.gionee.gnif3.context.ThreadContextHolder;
import com.gionee.gnif3.db.DsConfig;
import com.gionee.gnif3.db.SessionType;
import com.gionee.gnif3.exception.GnifRuntimeException;

import java.io.Serializable;

/**
 * Created by doit on 2016/4/18.
 */
public abstract class BaseCommand<T> implements Command<T>, Serializable {

    private Integer userId;

    public abstract void perform(IContext context);

    /**
     * execute with new session which will create from the param dsConfig
     *
     * @param dsConfig dataSource config
     * @return
     */
    @Override
    public T execute(DsConfig dsConfig) {
        IContext context = ContextFactory.getContext();
        ThreadContext threadContext = ThreadContextHolder.getContext();
        try {
            threadContext.requiredSession(dsConfig);
            perform(context);
            threadContext.commitAndCloseSession();
        } catch (Exception e) {
            threadContext.rollbackAndCloseSession();
            e.printStackTrace();
        } finally {
            ThreadContextHolder.clear();
        }
        return (T) this;
    }

    /**
     * execute with new session
     *
     * @param dsConfigName dataSource config name
     * @return
     */
    public T execute(String dsConfigName) {
        return execute(AppConfig.getConfig(dsConfigName).setSessionType(SessionType.RENEW));
    }

    /**
     * execute with last session
     *
     * @return
     */
    public T execute() {
        return execute(AppConfig.getDefaultDsConfig());
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }


}
