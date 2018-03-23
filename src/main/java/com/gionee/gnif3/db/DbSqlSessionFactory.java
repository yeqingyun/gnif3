package com.gionee.gnif3.db;

import com.gionee.gnif3.entity.Entity;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/21.
 */
public class DbSqlSessionFactory implements SessionFactory {
    private static Logger logger = LoggerFactory.getLogger(DbSqlSessionFactory.class);

    private SqlSessionFactory sqlSessionFactory;
    private DsConfig dsConfig;
    private IdGenerator idGenerator;


    private Map<Class<?>, String> insertStatements = new HashMap<>();
    private Map<Class<?>, String> updateStatements = new HashMap<>();
    private Map<Class<?>, String> deleteStatements = new HashMap<>();
    private Map<Class<?>, String> selectStatements = new HashMap<>();

    public DbSqlSessionFactory(SqlSessionFactory sqlSessionFactory, DsConfig dsConfig) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.dsConfig = dsConfig;
        this.idGenerator = new DatabaseIdGenerator(dsConfig);
    }

    public DsConfig getDsConfig() {
        return this.dsConfig;
    }

    @Override
    public DbSqlSession openSession() {
        return new DbSqlSession(this);
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    // insert, update and delete statements
    public String getInsertStatement(Entity object) {
        return getStatement(object.getClass(), insertStatements, "insert");
    }

    public String getUpdateStatement(Entity object) {
        return getStatement(object.getClass(), updateStatements, "update");
    }

    public String getDeleteStatement(Class<?> persistentObjectClass) {
        return getStatement(persistentObjectClass, deleteStatements, "delete");
    }

    public String getSelectStatement(Class<?> persistentObjectClass) {
        return getStatement(persistentObjectClass, selectStatements, "select");
    }

    private String getStatement(Class<?> persistentObjectClass, Map<Class<?>, String> cachedStatements, String prefix) {
        String statement = cachedStatements.get(persistentObjectClass);
        if (statement != null) {
            return statement;
        }
        statement = prefix + persistentObjectClass.getSimpleName();
        // persistent object should be ends with Entity.
        statement = statement.substring(0, statement.length() - 6);
        cachedStatements.put(persistentObjectClass, statement);
        return statement;
    }


}
