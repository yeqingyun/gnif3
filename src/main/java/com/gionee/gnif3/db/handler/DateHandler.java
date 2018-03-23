package com.gionee.gnif3.db.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Administrator on 2015/12/29.
 */
public class DateHandler implements ParamHandler {

    @Override
    public void setParam(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
        preparedStatement.setTimestamp(index, new java.sql.Timestamp(((Date) object).getTime()));
    }
}
