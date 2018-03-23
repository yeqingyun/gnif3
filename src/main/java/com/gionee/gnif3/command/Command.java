package com.gionee.gnif3.command;


import com.gionee.gnif3.db.DsConfig;

/**
 * Created by doit on 2016/4/18.
 */
public interface Command<T> {

    T execute();

    T execute(DsConfig dsConfig);

}
