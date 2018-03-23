package com.gionee.gnif3.query;

import com.gionee.gnif3.context.ThreadContextHolder;
import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.db.DsConfig;
import com.gionee.gnif3.exception.GnifRuntimeException;

import java.util.List;

/**
 * Created by doit on 2016/4/19.
 */
public abstract class BaseQuery<T> implements Query<T> {
    private static final int QUERY = 0;
    private static final int QUERY_SINGLE = 1;
    private static final int QUERY_PAGE = 2;
    private static final int QUERY_COUNT = 3;

    private int queryType;

    protected Page page;
    protected DbSqlSession session;
    protected DsConfig dsConfig;

    protected BaseQuery() {
        session = ThreadContextHolder.getContext().getSession();
    }

    protected BaseQuery(DsConfig dsConfig) {
        this.dsConfig = dsConfig;
        session = ThreadContextHolder.newSession(dsConfig);
    }

    protected abstract List<T> query(Page page);

    protected abstract long queryCount();

    protected Object execute() {
        Object result = null;
        switch (queryType) {
            case QUERY:
                result = query(null);
                break;
            case QUERY_SINGLE:
                result = querySingle();
                break;
            case QUERY_PAGE:
                result = query(page);
                break;
            case QUERY_COUNT:
                result = queryCount();
                break;
        }

        return result;
    }

    private T querySingle() {
        List<T> list = query(null);
        if (list.size() > 0) {
            return list.get(0);
        }

        throw new GnifRuntimeException("error, no query result");
    }

    @Override
    public List<T> list() {
        this.queryType = QUERY;
        return (List<T>) execute();
    }

    @Override
    public T single() {
        this.queryType = QUERY_SINGLE;
        return (T) execute();
    }

    @Override
    public List<T> list(Page page) {
        this.page = page;
        this.queryType = QUERY_PAGE;
        return (List<T>) execute();
    }


    @Override
    public long count() {
        this.queryType = QUERY_COUNT;
        return (long) execute();
    }

}
