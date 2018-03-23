package com.gionee.gnif3.action;

import com.gionee.gnif3.app.AppContext;
import com.gionee.gnif3.constant.StatusConst;
import com.gionee.gnif3.context.Context;
import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.entity.BaseEntity;
import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.entity.HasRevision;
import com.gionee.gnif3.exception.GnifRuntimeException;

import java.util.Date;

/**
 * Created by Leon.Yu on 2016/10/14.
 */
public class InsertAction extends DbAction {


    public InsertAction(Entity entity) {
        super(entity);
        handle(entity);
    }

    public InsertAction(String statementName, Object parameter) {
        super(statementName, parameter);
    }

    public InsertAction(String statementName, Object parameter, ActionType actionType) {
        super(statementName, parameter, actionType);
    }

    @Override
    protected void doActionWithStatement(DbSqlSession session) {
    }

    @Override
    protected void doActionWithEntity(DbSqlSession session) {
        String insertStatement = Context.getSqlSessionFactory().getInsertStatement(entity);
        if (insertStatement == null) {
            throw new GnifRuntimeException("no insert statement for " + entity.getClass() + " in the ibatis mapping files");
        }

        session.save(entity);
    }


    private void handle(Entity entity) {
        if (entity.getId() == null) {
            String idKey = entity.getClass().getSimpleName() + ".id";
            entity.setId((int) Context.getSqlSessionFactory().getIdGenerator().getNextId(idKey));
        }
        if (entity instanceof HasRevision) {
            BaseEntity revisionObject = (BaseEntity) entity;
            if (revisionObject.getRevision() == null) {
                revisionObject.setRevision(0);
            }
        }
        if (entity instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) entity;
            baseEntity.setCreateBy(AppContext.getUserId());
            baseEntity.setUpdateBy(AppContext.getUserId());
            Date currentTime = new Date();
            baseEntity.setCreateTime(currentTime);
            baseEntity.setUpdateTime(currentTime);
            if (baseEntity.getStatus() == null) {
                baseEntity.setStatus(StatusConst.ENABLE);
            }
        }
    }

}
