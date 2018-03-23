package com.gionee.gnif3.context;

/**
 * Created by doit on 2016/4/19.
 */
public class ContextFactory {

    private static Context context;

    public static IContext getContext() {
        if (context == null) {
            context = Context.getInstance();
        }
        return context;
    }

}
