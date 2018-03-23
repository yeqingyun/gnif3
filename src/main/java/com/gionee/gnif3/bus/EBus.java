package com.gionee.gnif3.bus;

import com.gionee.gnif3.action.DbAction;
import com.gionee.gnif3.action.DeleteAction;
import com.gionee.gnif3.action.InsertAction;
import com.gionee.gnif3.action.UpdateAction;
import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.unitofwork.CurrentUnitOfWork;
import com.gionee.gnif3.unitofwork.UnitOfWork;

import java.util.List;

/**
 * Event Bus
 * Created by Leon.Yu on 2016/10/14.
 */
public class EBus {

    private static UnitOfWork getCurrentUnitOfWork() {
        return CurrentUnitOfWork.get();
    }

    public static void flush() {
        getCurrentUnitOfWork().commit();
    }

//    public static void insert(String statement, Object parameter) {
//        getCurrentUnitOfWork().addAction(new InsertAction(statement, parameter));
//    }

    public static void update(String statement, Object parameter) {
        getCurrentUnitOfWork().addAction(new UpdateAction(statement, parameter));
    }

    public static void delete(String statement, Object parameter) {
        getCurrentUnitOfWork().addAction(new DeleteAction(statement, parameter));
    }

    public static void insert(Entity entity) {
        getCurrentUnitOfWork().addAction(new InsertAction(entity));
    }

    public static void update(Entity entity) {
        getCurrentUnitOfWork().addAction(new UpdateAction(entity));
    }

    public static void delete(Entity entity) {
        getCurrentUnitOfWork().addAction(new DeleteAction(entity));
    }

    public void batchInsert(List<? extends Entity> entityList) {
        getCurrentUnitOfWork().addAction(new InsertAction(null, entityList, DbAction.ActionType.BatchWithMyBatis));
    }

    public void batchUpdate(List<? extends Entity> entityList) {
        getCurrentUnitOfWork().addAction(new UpdateAction(null, entityList, DbAction.ActionType.BatchWithMyBatis));
    }

    public void batchUpdate(String statement, List<? extends Entity> entityList) {
        getCurrentUnitOfWork().addAction(new UpdateAction(statement, entityList, DbAction.ActionType.BatchWithMyBatis));
    }

    public void batchInsertWithNativeSQL(String statement, List<Object[]> paramList) {
        getCurrentUnitOfWork().addAction(new InsertAction(statement, paramList, DbAction.ActionType.BatchWithNativeSQL));
    }

    public void batchUpdateWithNativeSQL(String statement, List<Object[]> paramList) {
        getCurrentUnitOfWork().addAction(new UpdateAction(statement, paramList, DbAction.ActionType.BatchWithNativeSQL));
    }
}
