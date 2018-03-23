package com.gionee.gnif3.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by doit on 2016/4/18.
 */
public interface ObjectMapper<T> {

    T handleRow(ResultSet resultSet) throws SQLException;

}
