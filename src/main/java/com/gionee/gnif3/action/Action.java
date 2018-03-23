package com.gionee.gnif3.action;

import com.gionee.gnif3.db.Session;

/**
 * Created by Leon.Yu on 2016/10/18.
 */
public interface Action {
    void execute(Session session);
}
