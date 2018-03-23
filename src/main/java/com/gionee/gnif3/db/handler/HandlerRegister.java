package com.gionee.gnif3.db.handler;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by doit on 2015/12/29.
 */
public class HandlerRegister {

    private static Map<Class, ParamHandler> handlerRegistry;

    static {
        handlerRegistry = new HashMap<>();
        handlerRegistry.put(Integer.class, new IntegerHandler());
        handlerRegistry.put(int.class, new IntegerHandler());
        handlerRegistry.put(String.class, new StringHandler());
        handlerRegistry.put(Date.class, new DateHandler());
    }

    public static ParamHandler getHandler(Class<?> clazz) {
        return handlerRegistry.get(clazz);
    }

}
