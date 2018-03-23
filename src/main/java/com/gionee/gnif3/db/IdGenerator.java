package com.gionee.gnif3.db;

/**
 * Created by Leon on 2016/4/25.
 */
public interface IdGenerator {

    long getNextId(String key);
}
