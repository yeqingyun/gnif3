package com.gionee.gnif3.db;

import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.query.Page;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.util.List;

/**
 * Created by Administrator on 2016/4/21.
 */
public class VirtualSession implements Session {

    private DbSqlSessionFactory sqlSessionFactory;
    private DbSqlSession realSession;
    private DsConfig dsConfig;

    public VirtualSession(DbSqlSessionFactory sqlSessionFactory, DsConfig dsConfig) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.dsConfig = dsConfig;
    }

    public DbSqlSession openSession() {
        if (realSession == null) {
            if (dsConfig.getTransactionIsolation() != null) {
                this.realSession = new DbSqlSession(sqlSessionFactory, dsConfig.getTransactionIsolation());
            } else {
                this.realSession = new DbSqlSession(sqlSessionFactory);
            }
        }

        return this.realSession;
    }

    public Session openSession(TransactionIsolationLevel level) {
        if (realSession == null) {
            return new DbSqlSession(sqlSessionFactory, level);
        }
        return this.realSession;
    }

    public DsConfig getDsConfig() {
        return this.dsConfig;
    }

    @Override
    public void flush() {
        // 如果realSession未初始化，说明此次会话没有产生数据库连接
        if (this.realSession != null) {
            this.realSession.flush();
        }
    }

    @Override
    public void commit() {
        if (this.realSession != null) {
            this.realSession.commit();
        }
    }

    @Override
    public void rollback() {
        if (this.realSession != null) {
            this.realSession.rollback();
        }
    }

    @Override
    public void close() {
        if (this.realSession != null) {
            this.realSession.close();
        }
    }

    @Override
    public void save(Entity entity) {
        throw new GnifRuntimeException();
    }

    @Override
    public int update(String statement, Object parameter) {
        throw new GnifRuntimeException();
    }

    @Override
    public void update(Entity entity) {

    }

    @Override
    public int delete(String statement, Object parameter) {
        throw new GnifRuntimeException();
    }

    @Override
    public Object selectOne(String statement, Object parameter) {
        throw new GnifRuntimeException();
    }

    @Override
    public List selectList(String statement, Object parameter) {
        throw new GnifRuntimeException();
    }

    @Override
    public List selectList(String statement, Object parameter, Page page) {
        throw new GnifRuntimeException();
    }

}
