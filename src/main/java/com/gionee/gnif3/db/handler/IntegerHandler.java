package com.gionee.gnif3.db.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by doit on 2015/12/29.
 */
public class IntegerHandler implements ParamHandler {
    @Override
    public void setParam(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
        preparedStatement.setInt(index, (Integer) object);
    }
}
