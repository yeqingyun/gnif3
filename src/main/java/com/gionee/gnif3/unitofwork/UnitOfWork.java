package com.gionee.gnif3.unitofwork;

import com.gionee.gnif3.action.Action;

import java.util.concurrent.Callable;

/**
 * Created by Leon.Yu on 2016/9/29.
 */
public interface UnitOfWork {

    void begin();

    <R> R execute(Callable<R> callable);

    void addAction(Action action);

    void commit();

    void rollback();
}
