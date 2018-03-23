package com.gionee.gnif3.db;

import com.gionee.gnif3.exception.GnifRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class DatabaseTypeHelper {

    private static Map<String, String> databaseDriver = new HashMap<String, String>();

    static {
        databaseDriver.put("oracle", "oracle.jdbc.driver.OracleDriver");
        databaseDriver.put("db2", "com.ibm.db2.jdbc.app.DB2Driver");
        databaseDriver.put("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        databaseDriver.put("sybase", "com.sybase.jdbc.SybDriver");
        databaseDriver.put("mysql", "com.mysql.jdbc.Driver");
        databaseDriver.put("postgresql", "org.postgresql.Driver");
    }

    public static String getDatabaseType(String url) {
        String databaseType = null;
        if (url == null) {
            throw new NullPointerException("url of datasource can not be resolved.");
        }
        if (url.startsWith("jdbc:oracle")) {
            databaseType = "oracle";
        } else if (url.startsWith("jdbc:db2")) {
            databaseType = "db2";
        } else if (url.startsWith("jdbc:sqlserver")) {
            databaseType = "mssql";
        } else if (url.startsWith("jdbc:sybase")) {
            databaseType = "sybase";
        } else if (url.startsWith("jdbc:mysql")) {
            databaseType = "mysql";
        } else if (url.startsWith("jdbc:postgresql")) {
            databaseType = "postgresql";
        } else {
            throw new GnifRuntimeException("can not support database with url: [" + url + "]");
        }
        return databaseType;
    }

    public static String getDatabaseDriver(String databaseType) {
        return databaseDriver.get(databaseType);
    }

}
