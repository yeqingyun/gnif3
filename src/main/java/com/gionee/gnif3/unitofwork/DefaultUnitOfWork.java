package com.gionee.gnif3.unitofwork;

import com.gionee.gnif3.action.Action;

import java.util.concurrent.Callable;

/**
 * Created by Leon.Yu on 2016/9/29.
 */
public class DefaultUnitOfWork implements UnitOfWork {
    @Override
    public void begin() {

    }

    @Override
    public <R> R execute(Callable<R> callable) {
        return null;
    }

    @Override
    public void addAction(Action action) {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
