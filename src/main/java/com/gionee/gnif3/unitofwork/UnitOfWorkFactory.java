package com.gionee.gnif3.unitofwork;

/**
 * Created by Leon.Yu on 2016/9/29.
 */
public interface UnitOfWorkFactory {

    TransactionScriptUnitOfWork createUnitOfWork();
}
