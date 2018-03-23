package com.gionee.gnif3.db;

import com.gionee.gnif3.utils.StringUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;

/**
 * Created by doit on 2016/4/19.
 */
public class DsConfig {

    private String name;
    private String username;
    private String password;
    private String url;
    private boolean autoCommit;
    private SessionType sessionType = SessionType.NEW;
    private TransactionIsolationLevel transactionIsolation;

    public boolean supportDataSource() {
        if (StringUtils.hasText(url)
                && StringUtils.hasText(username)
                && StringUtils.hasText(password)) {
            return true;
        } else {
            return false;
        }
    }

    public DataSource createDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        String databaseType = DatabaseTypeHelper.getDatabaseType(url);
        dataSource.setDriverClassName(DatabaseTypeHelper.getDatabaseDriver(databaseType));
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnReturn(true);
        dataSource.setValidationQuery(getValidQuery(databaseType));
        return dataSource;
    }


    private String getValidQuery(String databaseType) {
        if (databaseType.equalsIgnoreCase("oracle")) {
            return "select 1 from dual";
        } else {
            return "select 1";
        }
    }

    public DsConfig setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public TransactionIsolationLevel getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(TransactionIsolationLevel transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

}
