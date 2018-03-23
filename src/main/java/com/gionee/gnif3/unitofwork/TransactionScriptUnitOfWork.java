package com.gionee.gnif3.unitofwork;

import com.gionee.gnif3.action.Action;
import com.gionee.gnif3.action.DbAction;
import com.gionee.gnif3.db.Session;
import com.gionee.gnif3.exception.GnifRuntimeException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Leon.Yu on 2016/10/11.
 */
public class TransactionScriptUnitOfWork implements UnitOfWork {
    private static Logger logger = Logger.getLogger(TransactionScriptUnitOfWork.class);

    private List<DbAction> dbActionList = new ArrayList<>();
    private Session session;

    public TransactionScriptUnitOfWork(Session session) {
        this.session = session;
    }

    @Override
    public void begin() {
        logger.debug("Transaction Script UnitOfWork is beginning...");
    }

    @Override
    public <R> R execute(Callable<R> callable) {
        begin();
        R result = null;
        try {
            result = callable.call();
            commit();
        } catch (Exception e) {
            rollback();
            e.printStackTrace();
            logger.error(e);
        }

        return result;
    }

    @Override
    public void addAction(Action action) {
        dbActionList.add((DbAction) action);
    }

    @Override
    public void commit() {
        logger.debug("Transaction Script UnitOfWork is committing...");
        dbActionList.forEach((dbAction) -> dbAction.execute(session));
        session.commit();
        unsetUnitOfWork();
    }

    private void unsetUnitOfWork() {
        CurrentUnitOfWork.unset(this);
    }

    @Override
    public void rollback() {
        logger.debug("Transaction Script UnitOfWork rollback...");
        dbActionList.clear();
        session.rollback();
        unsetUnitOfWork();
    }


}
