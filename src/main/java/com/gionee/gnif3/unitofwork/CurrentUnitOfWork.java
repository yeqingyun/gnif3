package com.gionee.gnif3.unitofwork;

import com.gionee.gnif3.exception.GnifRuntimeException;

import java.util.Stack;

/**
 * Created by Leon.Yu on 2016/9/29.
 */
public class CurrentUnitOfWork {

    private static ThreadLocal<Stack<UnitOfWork>> unitOfWorkThreadLocal = new ThreadLocal<>();

    public static <T> UnitOfWork get() {
        Stack<UnitOfWork> unitOfWorkStack = unitOfWorkThreadLocal.get();
        if (unitOfWorkStack.empty()) {
            throw new GnifRuntimeException("Current Unit of Work is Empty");
        }

        return unitOfWorkStack.peek();
    }

    public static void set(UnitOfWork unitOfWork) {
        Stack<UnitOfWork> unitOfWorkStack = unitOfWorkThreadLocal.get();
        if (unitOfWorkStack == null) {
            unitOfWorkStack = new Stack<>();
            unitOfWorkThreadLocal.set(unitOfWorkStack);
        }

        unitOfWorkStack.push(unitOfWork);
    }

    public static void unset(UnitOfWork unitOfWork) {
        Stack<UnitOfWork> unitOfWorks = unitOfWorkThreadLocal.get();
        if (unitOfWorks.peek() == unitOfWork) {
            unitOfWorks.pop();
        }
    }

    public static void clear() {
        unitOfWorkThreadLocal.remove();
    }

}
