package com.gionee.gnif3.action;

import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.entity.HasRevision;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.exception.OptimisticLockingException;

/**
 * Created by Leon.Yu on 2016/10/14.
 */
public class DeleteAction extends DbAction{
    public DeleteAction(Entity entity) {
        super(entity);
    }

    public DeleteAction(String statementName, Object parameter) {
        super(statementName, parameter);
    }

    public DeleteAction(String statementName, Object parameter, DbAction.ActionType actionType) {
        super(statementName, parameter, actionType);
    }

    @Override
    protected void doActionWithStatement(DbSqlSession session) {

    }

    @Override
    protected void doActionWithEntity(DbSqlSession session) {
        String deleteStatement = session.getDbSqlSessionFactory().getDeleteStatement(entity.getClass());
        if (deleteStatement == null) {
            throw new GnifRuntimeException("no delete statement for " + entity.getClass() + " in the ibatis mapping files");
        }

        // It only makes sense to check for optimistic locking exceptions
        // for objects that actually have a revision
        if (entity instanceof HasRevision) {
            int nrOfRowsDeleted = session.delete(deleteStatement, entity);
            if (nrOfRowsDeleted == 0) {
                throw new OptimisticLockingException(entity + " was updated by another transaction concurrently");
            }
        } else {
            session.delete(deleteStatement, entity);
        }
    }

}
