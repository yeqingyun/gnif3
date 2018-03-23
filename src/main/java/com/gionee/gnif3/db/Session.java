package com.gionee.gnif3.db;

import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.query.Page;

import java.util.List;

/**
 * Created by doit on 2016/4/18.
 */
public interface Session {
    // basic db operation
    void flush();

    void commit();

    void rollback();

    void close();

    void save(Entity entity);

    int update(String statement, Object parameter);

    void update(Entity entity);

    int delete(String statement, Object parameter);

    Object selectOne(String statement, Object parameter);

    List selectList(String statement, Object parameter);

    List selectList(String statement, Object parameter, Page page);
}
