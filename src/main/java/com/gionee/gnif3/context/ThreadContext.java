package com.gionee.gnif3.context;


import com.gionee.gnif3.config.AppConfig;
import com.gionee.gnif3.db.DbSqlSession;
import com.gionee.gnif3.db.DsConfig;
import com.gionee.gnif3.db.Session;
import com.gionee.gnif3.db.VirtualSession;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by doit on 2016/4/19.
 */
public class ThreadContext {

    private Stack<Session> sessionStack = new Stack<>();

    public ThreadContext() {

    }

    public ThreadContext(Session session) {
        sessionStack.push(session);
    }

    public boolean isSessionEmpty() {
        return sessionStack.size() == 0;
    }

    public void requiredSession(DsConfig dsConfig) {
        if (isDataSourceEmpty(dsConfig)) {
            pushSession(openSession(dsConfig));
        } else {
            switch (dsConfig.getSessionType()) {
                case REUSE:
                    pushSession(sessionStack.peek());
                    break;
                case RENEW:
                    pushSession(openSession(dsConfig));
                    break;
                case NEW:
                    pushSession(openSession(dsConfig));
                    break;
            }
        }
    }

    public DbSqlSession getSession() {
        // TODO: use stack to manage session.
        if (sessionStack.empty()) {
            requiredSession(AppConfig.getDefaultDsConfig());
        }

        Session session = sessionStack.peek();
        if (session instanceof VirtualSession) {
            session = ((VirtualSession) sessionStack.pop()).openSession();
            if (sessionStack.search(session) == -1) {
                sessionStack.push(session);
            }
        }

        return (DbSqlSession) session;
    }

    public void commitAndCloseSession() {
        Session sqlSession = sessionStack.peek();
        // 判断当前栈顶的session是否仍在堆栈中存在（command内部嵌套session调用），如果存在则不能commit
        // 应由最后的session进行commit
        if (sessionStack.lastIndexOf(sqlSession) == (sessionStack.size() - 1)) {
            sqlSession = sessionStack.pop();
            sqlSession.flush();
            sqlSession.commit();
            sqlSession.close();
        } else {
            sessionStack.pop().flush();
        }
    }

    public void rollbackAndCloseSession() {
        Session session = sessionStack.pop();
        session.flush();
        session.rollback();
        session.close();
    }

    public void pushSession(Session session) {
        sessionStack.push(session);
    }


    private Session openSession(DsConfig dsConfig) {
        return new VirtualSession(Context.getSqlSessionFactory(dsConfig.getName()), dsConfig);
    }

    private boolean isDataSourceEmpty(DsConfig dsconfig) {
        boolean isDataSourceEmpty = true;
        if (sessionStack.size() == 0) {
            isDataSourceEmpty = true;
        } else {
            for (Iterator<Session> iterator = sessionStack.iterator(); iterator.hasNext(); ) {
                Session session = iterator.next();
                DsConfig config = session instanceof DbSqlSession ?
                        ((DbSqlSession) session).getDsConfig() : ((VirtualSession) session).getDsConfig();
                if (config.getName().equals(dsconfig.getName())) {
                    isDataSourceEmpty = false;
                    break;
                }
            }
        }

        return isDataSourceEmpty;
    }

}
