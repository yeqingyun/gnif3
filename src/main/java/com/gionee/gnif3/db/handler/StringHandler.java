package com.gionee.gnif3.db.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by doit on 2015/12/29.
 */
public class StringHandler implements ParamHandler {
    @Override
    public void setParam(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
        preparedStatement.setString(index, (String) object);
    }
}
