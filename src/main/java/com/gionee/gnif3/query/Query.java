package com.gionee.gnif3.query;

import java.util.List;

/**
 * Created by doit on 2016/4/18.
 */
public interface Query<T> {

    List<T> list();

    T single();

    List<T> list(Page page);

    long count();

}
