package com.gionee.gnif3.context;

/**
 * Created by Leon on 2016/5/5.
 */
public interface IContext {
    <T> T getManager(Class<T> managerType);

    <T> T getManager(Class<T> managerType, String managerName);
}
