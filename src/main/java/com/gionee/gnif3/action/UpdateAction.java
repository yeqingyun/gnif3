package com.gionee.gnif3.action;

import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.entity.BaseEntity;
import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.entity.HasRevision;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.exception.OptimisticLockingException;

/**
 * Created by Leon.Yu on 2016/10/14.
 */
public class UpdateAction extends DbAction {
    public UpdateAction(Entity entity) {
        super(entity);
    }

    public UpdateAction(String statementName, Object parameter) {
        super(statementName, parameter);
    }

    public UpdateAction(String statementName, Object parameter, ActionType actionType) {
        super(statementName, parameter, actionType);
    }

    @Override
    protected void doActionWithStatement(DbSqlSession session) {

    }

    @Override
    protected void doActionWithEntity(DbSqlSession session) {
        session.update(entity);
    }

}
