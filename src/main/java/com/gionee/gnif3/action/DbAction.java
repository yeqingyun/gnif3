package com.gionee.gnif3.action;

import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.db.Session;
import com.gionee.gnif3.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by Leon.Yu on 2016/10/14.
 */
public abstract class DbAction implements Action {

    protected static Logger logger = LoggerFactory.getLogger(DbAction.class);
    protected Entity entity;
    protected String statementName;
    protected Object parameter;
    protected ActionType actionType;

    public DbAction(Entity entity) {
        this.entity = entity;
        this.actionType = ActionType.MyBatis;
    }

    public DbAction(String statementName, Object parameter) {
        this(statementName, parameter, ActionType.MyBatis);
    }

    public DbAction(String statementName, Object parameter, ActionType actionType) {
        this.statementName = statementName;
        this.parameter = parameter;
        this.actionType = ActionType.MyBatis;
    }

    @Override
    public void execute(Session session) {
        DbSqlSession dbSqlSession = (DbSqlSession) session;
        if (entity != null) {
            doActionWithEntity(dbSqlSession);
        } else {
            doActionWithStatement(dbSqlSession);
        }
    }

    protected abstract void doActionWithStatement(DbSqlSession session);

    protected abstract void doActionWithEntity(DbSqlSession session);

    protected void closeResource(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void closeResource(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public enum ActionType {
        NativeSQL, MyBatis, BatchWithNativeSQL, BatchWithMyBatis
    }
}
