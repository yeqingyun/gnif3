package com.gionee.gnif3.api;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public interface Repository<T> {

    T load(int id);

    void save(T t);

}
