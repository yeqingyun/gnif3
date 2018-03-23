package com.gionee.gnif3.context;


import com.gionee.gnif3.config.AppConfig;
import com.gionee.gnif3.db.DbSqlSessionFactory;
import com.gionee.gnif3.exception.GnifRuntimeException;

import javax.inject.Named;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doit on 2016/4/19.
 */
public class Context implements IContext {
    private static Map<String, Object> container = new HashMap<String, Object>();


    public static Map<String, DbSqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();

    static {
//        init();
    }

    private Context() {
    }

    public static DbSqlSessionFactory getSqlSessionFactory() {
        return getSqlSessionFactory(AppConfig.DB_DEFAULT);
    }


    public static DbSqlSessionFactory getSqlSessionFactory(String name) {
        if (!sqlSessionFactoryMap.containsKey(name)) {
            throw new GnifRuntimeException("no datasource found");
        }
        return sqlSessionFactoryMap.get(name);
    }


    public static ThreadContext getThreadContext() {
        return ThreadContextHolder.getContext();
    }

    public static Context getInstance() {
        return new Context();
    }

    @Override
    public <T> T getManager(Class<T> managerType) {
        return getManager(managerType, managerType.getName());
    }

    @Override
    public <T> T getManager(Class<T> managerType, String managerName) {
        // eager init
        if (container.containsKey(managerName)) {
            return (T) container.get(managerName);
        } else {
            return null;
        }
    }

    public static void scanTestPackage() {
        ClassLoader classLoader = Context.class.getClassLoader();
        URL url = classLoader.getResource(Context.class.getName().replace('.', File.separatorChar) + ".class");
        File rootDir = new File(url.getPath()).getParentFile().getParentFile();
        File[] dirs = rootDir.listFiles();
        File testClassesDir = null;
        for (File dir : dirs) {
            if (dir.getPath().indexOf("target" + File.separatorChar + "test-classes") != -1) {
                testClassesDir = dir;
                break;
            }
        }

        if (testClassesDir != null) {
            try {
                URL testClassesURL = testClassesDir.toURI().toURL();
                URLClassLoader testClassLoader = new URLClassLoader(new URL[]{testClassesURL});
                List<Class> classList = readFile(testClassesDir, testClassLoader);
                addManagerToContainer(classList);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void init() {
        try {
            ClassLoader classLoader = Context.class.getClassLoader();
            URL url = classLoader.getResource(Context.class.getName().replace('.', File.separatorChar) + ".class");
            File parentFile = new File(url.getPath()).getParentFile();
            url = parentFile.toURI().toURL();
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
            List<Class> classList = readFile(parentFile, urlClassLoader);
            addManagerToContainer(classList);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void addManagerToContainer(List<Class> classList) throws InstantiationException, IllegalAccessException {
        List<Class> interfaceClassList = new ArrayList<>();
        for (Class clazz : classList) {
            if (clazz.isInterface()) {
                interfaceClassList.add(clazz);
            }
        }

        for (Class clazz : classList) {
            if (clazz.isAnnotationPresent(Named.class)) {
                Class[] interfaces = clazz.getInterfaces();
                if (interfaces.length > 0) {
                    for (Class interfaceClass : interfaces) {
                        if (interfaceClassList.contains(interfaceClass)) {
                            container.put(interfaceClass.getName(), clazz.newInstance());
                        }
                    }
                } else {
                    container.put(clazz.getName(), clazz.newInstance());
                }
            }
        }
    }

    private static List<Class> readFile(File file, URLClassLoader urlClassLoader) {
        List<Class> classList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File insideFile : files) {
            if (insideFile.isDirectory()) {
                classList.addAll(readFile(insideFile, urlClassLoader));
            } else {
                if (insideFile.getPath().endsWith(".class")) {
                    try {
                        URL defaultUrl = urlClassLoader.getURLs()[0];
                        URL fileUrl = insideFile.toURI().toURL();
                        int packageIndex = fileUrl.getPath().lastIndexOf(defaultUrl.getPath());
                        String className = fileUrl.getPath().substring(packageIndex + defaultUrl.getPath().length()).replace("/", ".").replace(".class", "");
                        Class clazz = urlClassLoader.loadClass(className);
                        classList.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return classList;
    }
}
