package com.gionee.gnif3.db;

import com.gionee.gnif3.app.AppContext;
import com.gionee.gnif3.config.AppConfig;
import com.gionee.gnif3.constant.StatusConst;
import com.gionee.gnif3.db.handler.HandlerRegister;
import com.gionee.gnif3.db.handler.ParamHandler;
import com.gionee.gnif3.entity.BaseEntity;
import com.gionee.gnif3.entity.Entity;
import com.gionee.gnif3.entity.HasRevision;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.exception.OptimisticLockingException;
import com.gionee.gnif3.query.Page;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/21.
 */
public class DbSqlSession implements Session {

    private static Logger log = LoggerFactory.getLogger(DbSqlSession.class);

    private SqlSession sqlSession;
    private DbSqlSessionFactory dbSqlSessionFactory;
    private List<PersistentAction> persistentActions = new ArrayList<>();

    private boolean jdbcExecuted = false;

    public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
        this.dbSqlSessionFactory = dbSqlSessionFactory;
        this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession(getDsConfig().isAutoCommit());
    }

    public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, TransactionIsolationLevel level) {
        this.dbSqlSessionFactory = dbSqlSessionFactory;
        this.sqlSession = dbSqlSessionFactory.getSqlSessionFactory().openSession(level);
    }

    public DsConfig getDsConfig() {
        return this.dbSqlSessionFactory.getDsConfig();
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public DbSqlSessionFactory getDbSqlSessionFactory() {
        return this.dbSqlSessionFactory;
    }

    public List selectList(String statement) {
        return sqlSession.selectList(statement);
    }

    public List selectList(String statement, Object parameter) {
        return sqlSession.selectList(statement, parameter);
    }

    public List selectList(String statement, Object parameter, Page page) {
        if (parameter instanceof Map) {
            setPageIntoParamMap((Map) parameter, page);
            return selectList(statement, parameter);
        } else {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("parameter", parameter);
            setPageIntoParamMap(paramMap, page);
            return selectList(statement, paramMap);
        }
    }

    Object selectOne(String statement) {
        return sqlSession.selectOne(statement);
    }

    public Object selectOne(String statement, Object parameter) {
        return sqlSession.selectOne(statement, parameter);
    }


    public void save(Entity entity) {
        String insertStatement = dbSqlSessionFactory.getInsertStatement(entity);
        if (insertStatement == null) {
            throw new GnifRuntimeException("no insert statement for " + entity.getClass() + " in the ibatis mapping files");
        }

        sqlSession.insert(insertStatement, entity);
    }

    public int update(String statement, Object parameter) {
        return sqlSession.update(statement, parameter);
    }

    public void update(Entity entity) {
        String updateStatement = getDbSqlSessionFactory().getUpdateStatement(entity);
        if (updateStatement == null) {
            throw new GnifRuntimeException("no update statement for " + entity.getClass() + " in the ibatis mapping files");
        }

        if (log.isDebugEnabled()) {
            log.debug("updating: {}", entity);
        }
        int updatedRecords = sqlSession.update(updateStatement, entity);
        if (updatedRecords != 1) {
            throw new OptimisticLockingException(entity + " was updated by another transaction concurrently");
        }

        if (entity instanceof HasRevision) {
            ((BaseEntity) entity).setRevision(((BaseEntity) entity).getRevisionNext());
        }
    }

    public int delete(String statement, Object parameter) {
        return sqlSession.delete(statement, parameter);
    }

    private void handleAction(PersistentAction persistentAction) {
        if (AppConfig.FLUSH_IMMEDIATELY) {
            persistentAction.execute();
        } else {
            persistentActions.add(persistentAction);
        }
    }

    @Override
    public void flush() {
        if (log.isDebugEnabled()) {
            log.debug("execute {} persistent actions.", persistentActions.size());
        }
        try {
            for (PersistentAction persistentAction : persistentActions) {
                persistentAction.execute();
            }
        } finally {
            persistentActions.clear();
        }
    }

    public void commit(boolean force) {
        sqlSession.commit(force);
        jdbcExecuted = false;
    }

    public void rollback(boolean force) {
        sqlSession.rollback(force);
        jdbcExecuted = false;
    }

    @Override
    public void commit() {
        commit(isCommitOrRollbackRequired(false));
    }

    @Override
    public void rollback() {
        rollback(isCommitOrRollbackRequired(false));
    }

    @Override
    public void close() {
        sqlSession.close();
        jdbcExecuted = false;
    }

    private String getEntityName(Entity entity) {
        String entitySimpleName = entity.getClass().getSimpleName();
        return String.valueOf(entitySimpleName.charAt(0)).toUpperCase()
                + entitySimpleName.substring(1, entitySimpleName.length() - 6);
    }

    public void execute(String sql, Object... args) {
        String lowerCaseSql = sql.toLowerCase();
        if (lowerCaseSql.contains("update ")) {
            handleAction(new UpdateAction(sql, Arrays.asList(args), ActionType.NativeSQL));
        } else if (lowerCaseSql.contains("delete ")) {
            handleAction(new DeleteAction(sql, Arrays.asList(args), ActionType.NativeSQL));
        } else if (lowerCaseSql.contains("insert ")) {
            handleAction(new InsertAction(sql, Arrays.asList(args), ActionType.NativeSQL));
        }
    }


    /**
     * 批量更新
     *
     * @param entityList Entity实体集合
     */
    public void batchUpdate(List<? extends Entity> entityList) {
        handleAction(new UpdateAction(null, entityList, ActionType.BatchWithMyBatis));
    }

    /**
     * 批量更新
     *
     * @param statementName myBatis的sql Id
     * @param entityList    Entity实体集合
     */
    public void batchUpdate(String statementName, List<? extends Entity> entityList) {
        handleAction(new UpdateAction(statementName, entityList, ActionType.BatchWithMyBatis));
    }

    /**
     * 使用原生SQL语句进行批量更新
     *
     * @param sql       SQL语句
     * @param paramList 需要更新的参数数组集合
     */
    public void batchUpdateWithNativeSQL(String sql, List<Object[]> paramList) {
        handleAction(new UpdateAction(sql, paramList, ActionType.BatchWithNativeSQL));
    }

    /**
     * 批量插入
     *
     * @param entityList Entity实体集合
     */
    public void batchInsert(List<? extends Entity> entityList) {
        handleAction(new InsertAction(null, entityList, ActionType.BatchWithMyBatis));
    }

    /**
     * 使用原生SQL语句进行批量插入
     *
     * @param sql       SQL语句
     * @param paramList 需要插入的参数数组集合
     */
    public void batchInsertWithNativeSQL(String sql, List<Object[]> paramList) {
        handleAction(new InsertAction(sql, paramList, ActionType.NativeSQL));
    }

    /**
     * 查询集合
     *
     * @param mapper 返回值中泛型对象的转换方法
     * @param sql    SQL语句
     * @param args   参数数组
     * @param <T>    需要返回的泛型
     * @return 查询结果集
     */
    public <T> List<T> queryForList(ObjectMapper<T> mapper, String sql, Object... args) {
        List<T> resultList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement(sql, args);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T result = mapper.handleRow(resultSet);
                if (result != null) {
                    resultList.add(result);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Execute query with jdbc: " + sql);
                log.debug("Parameters: " + Arrays.toString(args));
                log.debug("Result list size: " + resultList.size());
            }
        } catch (SQLException e) {
            throw new GnifRuntimeException("Error cause when execute sql", e);
        } finally {
            closeResource(preparedStatement, resultSet);
        }
        return resultList;
    }


    public <T> T querySingle(ObjectMapper<T> mapper, String sql, Object... args) {
        List<T> resultList = queryForList(mapper, sql, args);
        if (resultList.size() == 0) {
            throw new GnifRuntimeException("except one result, but no result return");
        }

        return resultList.get(0);
    }

    public <T> T queryFirst(ObjectMapper<T> mapper, String sql, Object... args) {
        List<T> resultList = queryForList(mapper, sql, args);
        if (resultList.size() > 0) {
            return resultList.get(0);
        }

        return null;
    }

    public abstract class PersistentAction {
        protected Entity entity;
        protected String statement;
        protected Object parameter;
        protected ActionType actionType;

        public PersistentAction(Entity entity) {
            this.entity = entity;
            actionType = ActionType.MyBatis;
        }

        public PersistentAction(String statement, Object parameter, ActionType actionType) {
            this.statement = statement;
            this.parameter = parameter;
            this.actionType = actionType;
        }

        public void execute() {
            if (entity != null) {
                doActionForEntity();
            } else {
                doActionForBulk();
            }
        }


        protected abstract void doActionForBulk();

        protected abstract void doActionForEntity();
    }

    public class InsertAction extends PersistentAction {

        public InsertAction(Entity entity) {
            super(entity);
            handle(entity);
        }

        public InsertAction(String sql, Object param, ActionType actionType) {
            super(sql, param, actionType);
        }

        private void handle(Entity entity) {
            if (entity.getId() == null) {
                String idKey = entity.getClass().getSimpleName() + ".id";
                entity.setId((int) dbSqlSessionFactory.getIdGenerator().getNextId(idKey));
            }
            if (entity instanceof HasRevision) {
                BaseEntity revisionObject = (BaseEntity) entity;
                if (revisionObject.getRevision() == null) {
                    revisionObject.setRevision(0);
                }
            }
            if (entity instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) entity;
                baseEntity.setCreateBy(AppContext.getUserId());
                baseEntity.setUpdateBy(AppContext.getUserId());
                Date currentTime = new Date();
                baseEntity.setCreateTime(currentTime);
                baseEntity.setUpdateTime(currentTime);
                if (baseEntity.getStatus() == null) {
                    baseEntity.setStatus(StatusConst.ENABLE);
                }
            }
        }

        @Override
        protected void doActionForBulk() {
            if (actionType == ActionType.NativeSQL) {
                doExecuteWithNativeSQL(statement, (List<Object>) parameter);
            } else if (actionType == ActionType.BatchWithNativeSQL) {
                doBatchInsertWithNativeSQL(statement, (List<Object[]>) parameter);
            } else if (actionType == ActionType.BatchWithMyBatis) {
                doBatchInsert((List<? extends Object>) parameter);
            }
        }

        @Override
        protected void doActionForEntity() {
            String insertStatement = dbSqlSessionFactory.getInsertStatement(entity);
            if (insertStatement == null) {
                throw new GnifRuntimeException("no insert statement for " + entity.getClass() + " in the ibatis mapping files");
            }

            sqlSession.insert(insertStatement, entity);
        }
    }

    public class UpdateAction extends PersistentAction {

        public UpdateAction(Entity entity) {
            super(entity);
            handle(entity);
        }

        public UpdateAction(String statement, Object parameter, ActionType actionType) {
            super(statement, parameter, actionType);
        }

        private void handle(Entity entity) {
            if (entity instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) entity;
                baseEntity.setUpdateBy(AppContext.getUserId());
                Date currentTime = new Date();
                baseEntity.setUpdateTime(currentTime);
            }
        }

        @Override
        protected void doActionForBulk() {
            if (actionType == ActionType.MyBatis) {
                int updatedRecords = getSqlSession().update(statement, parameter);
                if (parameter instanceof HasRevision && updatedRecords != 1) {
                    throw new OptimisticLockingException(parameter + " was updated by another transaction concurrently");
                }
            } else if (actionType == ActionType.BatchWithMyBatis) {
                doBatchUpdate((List<? extends Entity>) parameter);
            } else if (actionType == ActionType.NativeSQL) {
                doExecuteWithNativeSQL(statement, (List<Object>) parameter);
            } else if (actionType == ActionType.BatchWithNativeSQL) {
                doBatchUpdateWithNativeSQL(statement, (List<Object[]>) parameter);
            }
        }

        @Override
        protected void doActionForEntity() {
            String updateStatement = dbSqlSessionFactory.getUpdateStatement(entity);
            if (updateStatement == null) {
                throw new GnifRuntimeException("no update statement for " + entity.getClass() + " in the ibatis mapping files");
            }

            if (log.isDebugEnabled()) {
                log.debug("updating: {}", entity);
            }
            int updatedRecords = sqlSession.update(updateStatement, entity);
            if (updatedRecords != 1) {
                throw new OptimisticLockingException(entity + " was updated by another transaction concurrently");
            }

            if (entity instanceof HasRevision) {
                ((BaseEntity) entity).setRevision(((BaseEntity) entity).getRevisionNext());
            }
        }
    }

    public class DeleteAction extends PersistentAction {

        public DeleteAction(Entity entity) {
            super(entity);
        }

        public DeleteAction(String statement, Object parameter, ActionType actionType) {
            super(statement, parameter, actionType);
        }

        public void doActionForBulk() {
            if (actionType == ActionType.MyBatis) {
                sqlSession.delete(statement, parameter);
            } else if (actionType == ActionType.NativeSQL) {
                doExecuteWithNativeSQL(statement, (List<Object>) parameter);
            }
        }

        public void doActionForEntity() {
            String deleteStatement = dbSqlSessionFactory.getDeleteStatement(entity.getClass());
            if (deleteStatement == null) {
                throw new GnifRuntimeException("no delete statement for " + entity.getClass() + " in the ibatis mapping files");
            }

            // It only makes sense to check for optimistic locking exceptions
            // for objects that actually have a revision
            if (entity instanceof HasRevision) {
                int nrOfRowsDeleted = sqlSession.delete(deleteStatement, entity);
                if (nrOfRowsDeleted == 0) {
                    throw new OptimisticLockingException(entity + " was updated by another transaction concurrently");
                }
            } else {
                sqlSession.delete(deleteStatement, entity);
            }
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Object[] args) throws SQLException {
        PreparedStatement preparedStatement = sqlSession.getConnection().prepareStatement(sql);
        int index = 1;
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (null == arg) {
                sb.append(",null");
                preparedStatement.setNull(index++, Types.NULL);
            } else {
                sb.append(arg.toString() + "(" + arg.getClass().getSimpleName() + ")");
                ParamHandler handler = HandlerRegister.getHandler(arg.getClass());
                handler.setParam(preparedStatement, index++, arg);
            }
        }
        return preparedStatement;
    }

    private void closeResource(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void doBatchInsert(List<? extends Object> entityList) {
        if (entityList.size() == 0) {
            throw new GnifRuntimeException("no entity in list, can't batch insert");
        } else if (entityList.size() == 1) {
            save((Entity) entityList.get(0));
        } else {
            String statementName = "insert" + getEntityName((Entity) entityList.get(0));
            doBatch(statementName, entityList);
        }
    }

    private void doBatchUpdate(List<? extends Entity> entityList) {
        if (entityList.size() == 0) {
            throw new GnifRuntimeException("no entity in list, can't batch update");
        }

        String statementName = "update" + getEntityName(entityList.get(0));
        doBatchUpdate(statementName, Arrays.asList(entityList.toArray(new Object[]{})));
    }

    private void doBatchUpdate(String statementName, List<? extends Object> paramList) {
        if (paramList.size() == 0) {
            throw new GnifRuntimeException("no entity in list, can't batch update");
        } else if (paramList.size() == 1) {
            update(statementName, paramList.get(0));
        } else {
            doBatch(statementName, paramList);
        }
    }

    private void doBatchUpdateWithNativeSQL(String sql, List<Object[]> paramList) {
        if (paramList.size() == 0) {
            throw new GnifRuntimeException("no param in list, can't batch update");
        } else if (paramList.size() == 1) {
            execute(sql, paramList.get(0));
        } else {
            doBatchWithNativeSQL(sql, paramList);
        }
    }

    private void doBatchInsertWithNativeSQL(String sql, List<Object[]> paramList) {
        if (paramList.size() == 0) {
            throw new GnifRuntimeException("no param in list, can't batch insert");
        } else if (paramList.size() == 1) {
            execute(sql, paramList.get(0));
        } else {
            doBatchWithNativeSQL(sql, paramList);
        }
    }

    private void doBatchWithNativeSQL(String sql, List<Object[]> paramList) {
        jdbcExecuted = true;
        Connection connection = sqlSession.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            beginTransaction(connection);
            preparedStatement = getPreparedStatement(sql, paramList.get(0));
            for (Object[] args : paramList) {
                int index = 0;
                for (Object arg : args) {
                    if (null == arg) {
                        preparedStatement.setNull(index++, Types.NULL);
                    } else {
                        ParamHandler handler = HandlerRegister.getHandler(arg.getClass());
                        handler.setParam(preparedStatement, index++, arg);
                    }
                }
                preparedStatement.addBatch();
            }

            int[] executeBatchCount = preparedStatement.executeBatch();

            if (log.isDebugEnabled()) {
                log.debug("executed " + executeBatchCount.length + " batch with jdbc, sql: " + sql + ", using JDBC Connection [" + connection + "]");
            }
        } catch (SQLException e) {
            rollback();
            throw new GnifRuntimeException("batch execute sql: " + sql + " occur error", e);
        } finally {
            closeResource(preparedStatement);
        }
    }

    private void doBatch(String statementName, List entityList) {
        jdbcExecuted = true;
        Connection connection = sqlSession.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            Entity firstEntity = (Entity) entityList.get(0);
            MappedStatement mappedStatement = sqlSession.getConfiguration().getMappedStatement(statementName);
            BoundSql boundSql = mappedStatement.getBoundSql(firstEntity);

            beginTransaction(connection);
            preparedStatement = connection.prepareStatement(boundSql.getSql());
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            int batchCount = 0;
            for (Object entity : entityList) {
                int index = 1;
                MetaObject metaObject = entity == null ? null : sqlSession.getConfiguration().newMetaObject(entity);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    if (parameterMapping.getMode() != ParameterMode.OUT) {
                        Object value = null;
                        String propertyName = parameterMapping.getProperty();
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                        TypeHandler typeHandler = parameterMapping.getTypeHandler();
                        JdbcType jdbcType = parameterMapping.getJdbcType();
                        if (value == null && jdbcType == null) {
                            jdbcType = sqlSession.getConfiguration().getJdbcTypeForNull();
                        }

                        typeHandler.setParameter(preparedStatement, index++, value, jdbcType);
                    }
                }

                preparedStatement.addBatch();

                // execute when batch sql count equals 500
                if (++batchCount % 1000 == 0) {
                    int[] executeBatchCount = preparedStatement.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("executed " + executeBatchCount.length + " batch with jdbc, sql id in mapper: " + statementName + ",using JDBC Connection [" + connection + "]");
                    }
                }
            }
            // execute left batch sql
            if (batchCount % 1000 != 0) {
                int[] executeBatchCount = preparedStatement.executeBatch();
                if (log.isDebugEnabled()) {
                    log.debug("executed " + executeBatchCount.length + " batch with jdbc, sql id in mapper: " + statementName + ",using JDBC Connection [" + connection + "]");
                }
            }
            // close connection give ThreadContext to handle
        } catch (SQLException e) {
            rollback();
            throw new GnifRuntimeException("batch execute statement: " + statementName + " error", e);
        } finally {
            closeResource(preparedStatement);
        }
    }

    private void beginTransaction(Connection connection) {
        try {
            if (!getDsConfig().isAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeResource(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPageIntoParamMap(Map paramMap, Page page) {
        paramMap.put("firstResult", page.getFirstResult());
        paramMap.put("maxResults", page.getMaxResults());
        paramMap.put("firstRow", page.getFirstResult() + 1);
        paramMap.put("lastRow",
                page.getMaxResults() == Integer.MAX_VALUE ?
                        page.getMaxResults() : page.getFirstResult() + page.getMaxResults() + 1);
    }

    private int doExecuteWithNativeSQL(String sql, List<Object> params) {
        jdbcExecuted = true;
        int updateRows = 0;
        PreparedStatement preparedStatement = null;
        Connection connection = sqlSession.getConnection();
        try {
            preparedStatement = getPreparedStatement(sql, params.toArray());
            updateRows = preparedStatement.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Executed sql: " + sql + ", using JDBC Connection [" + connection + "]");
                log.debug("Parameters: " + params.toString());
                log.debug("Updated " + updateRows + " rows, using JDBC Connection [" + connection + "]");
            }
        } catch (SQLException e) {
            rollback();
            throw new GnifRuntimeException("Error cause when execute sql", e);
        } finally {
            closeResource(preparedStatement);
        }

        return updateRows;
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return jdbcExecuted || force;
    }


    private enum ActionType {
        NativeSQL, MyBatis, BatchWithNativeSQL, BatchWithMyBatis
    }


}
