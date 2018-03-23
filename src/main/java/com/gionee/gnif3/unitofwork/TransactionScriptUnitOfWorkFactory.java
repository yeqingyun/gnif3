package com.gionee.gnif3.unitofwork;

import com.gionee.gnif3.context.Context;
import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.db.DbSqlSessionFactory;

/**
 * Created by Leon.Yu on 2016/10/13.
 */
public class TransactionScriptUnitOfWorkFactory implements UnitOfWorkFactory {
    @Override
    public TransactionScriptUnitOfWork createUnitOfWork() {
        DbSqlSessionFactory sqlSessionFactory = Context.getSqlSessionFactory();
        DbSqlSession session = sqlSessionFactory.openSession();
        return new TransactionScriptUnitOfWork(session);
    }
}
