package com.gionee.gnif3.impl;

import com.gionee.gnif3.api.Repository;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class SimpleRepository<T> implements Repository<T> {
    public T load(int id) {
        return null;
    }

    public void save(T t) {

    }
}
