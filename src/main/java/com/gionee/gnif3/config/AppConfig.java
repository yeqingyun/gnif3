package com.gionee.gnif3.config;

import com.gionee.gnif3.api.CommandBus;
import com.gionee.gnif3.bus.CBus;
import com.gionee.gnif3.context.Context;
import com.gionee.gnif3.db.DbSqlSessionFactory;
import com.gionee.gnif3.db.DsConfig;
import com.gionee.gnif3.db.SessionType;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.unitofwork.UnitOfWorkFactory;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Administrator on 2016/4/20.
 */
public class AppConfig {
    public static final String DB_DEFAULT = "default";
//    public static final String DB_OTHER = "other";
//    public static final String DB_THIRD = "third";

    public static final String COMMAND_BUS_CLASS = "app.cbus.class";
    public static final String UOW_FACTORY_CLASS_NAME = "app.uowfactory.class";

    // PropertyUtil.getBoolean("core.db.flushImmediately", false)
    public static boolean FLUSH_IMMEDIATELY = false;

    /**
     * URL protocol for an entry from a jar file: "jar"
     */
    public static final String URL_PROTOCOL_JAR = "jar";
    /**
     * URL protocol for an entry from a zip file: "zip"
     */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /**
     * URL protocol for an entry from a WebSphere jar file: "wsjar"
     */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /**
     * URL protocol for an entry from a JBoss jar file: "vfszip"
     */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    private static Map<String, DsConfig> dsConfigMap = new HashMap<>();

    private static Map<String, String> propertiesValueMap = new HashMap<>();


    private static final String DB_MAPPER_LOCATION = "META-INF/db/mapper/";

    private static final String BUNDLE_PROPERTY_FILE_NAME = "app.properties";

    public static void init() {
        // 读取配置文件
        readPropertiesConfig();
        // 初始化数据源
        initSqlSessionFactoryMap();
        // 初始化CommandBus
        initCommandBus();
    }

    private static void initCommandBus() {
        String cBusClassName = propertiesValueMap.get(COMMAND_BUS_CLASS);
        String uowFactoryClassName = propertiesValueMap.get(UOW_FACTORY_CLASS_NAME);

        try {
            Class<?> uowFactoryClass = Class.forName(uowFactoryClassName);
            Constructor<?> uowFactoryConstructor = uowFactoryClass.getConstructor();
            UnitOfWorkFactory unitOfWorkFactory = (UnitOfWorkFactory) uowFactoryConstructor.newInstance();
            Class<?> cBusClass = Class.forName(cBusClassName);
            Constructor<?> constructor = cBusClass.getConstructor(UnitOfWorkFactory.class);
            CommandBus commandBus = (CommandBus) constructor.newInstance(unitOfWorkFactory);
            CBus.CommandBusProxy commandBusProxy = new CBus.CommandBusProxy(commandBus);
            CBus.setCommandBusProxy(commandBusProxy);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void readPropertiesConfig() {
        // 读取所有的bundle.properties的配置文件，最后再读取app.properties文件并覆盖bundle.properties的配置
        try {
            Enumeration<URL> propertyFileURLs = AppConfig.class.getClassLoader().getResources(BUNDLE_PROPERTY_FILE_NAME);
            for (; propertyFileURLs.hasMoreElements(); ) {
                readPropertyConfigByURL(propertyFileURLs.nextElement());
            }
        } catch (IOException e) {
            throw new GnifRuntimeException("try to read app.properties error", e);
        }

        URL appPropertiesURL = AppConfig.class.getClassLoader().getResource("app.properties");
        if (appPropertiesURL != null) {
            readPropertyConfigByURL(appPropertiesURL);
        }
    }

    public static String getPropertyValue(String key) {
        return propertiesValueMap.get(key);
    }

    private static void readPropertyConfigByURL(URL url) {
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            readPropertyConfigFile(inputStream);
        } catch (IOException e) {
            throw new GnifRuntimeException("read properties file occur error,file URL: " + url.toString(), e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new GnifRuntimeException("try to close InputStream for properties file occur error, file URL: " + url.toString(), e);
            }
        }
    }

    private static void readPropertyConfigFile(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return;
        }

        Properties properties = new Properties();
        properties.load(inputStream);
        Set<Object> keySet = properties.keySet();
        Map<String, List<String>> configMap = new HashMap<>();
        for (Object object : keySet) {
            String key = (String) object;
            // 读取db开头的数据库配置，将作为初始化数据源的条件
            if (key.toString().contains("db.")) {
                String configName = key.substring(3, key.lastIndexOf("."));
                if (configMap.containsKey(configName)) {
                    configMap.get(configName).add(key);
                } else {
                    List<String> configKeyList = new ArrayList<>();
                    configKeyList.add(key);
                    configMap.put(configName, configKeyList);
                }
            } else {
                propertiesValueMap.put(key, properties.getProperty(key));
            }
        }
        for (Map.Entry<String, List<String>> configEntry : configMap.entrySet()) {
            DsConfig dsConfig = new DsConfig();
            for (String configKey : configEntry.getValue()) {
                dsConfig.setName(configEntry.getKey());
                String configValue = properties.getProperty(configKey);
                if (configKey.endsWith("url")) {
                    dsConfig.setUrl(configValue);
                } else if (configKey.endsWith("username")) {
                    dsConfig.setUsername(configValue);
                } else if (configKey.endsWith("password")) {
                    dsConfig.setPassword(configValue);
                }
            }
            dsConfigMap.put(configEntry.getKey(), dsConfig);
        }

    }


    public static DsConfig getDefaultDsConfig() {
        return dsConfigMap.get(AppConfig.DB_DEFAULT).setSessionType(SessionType.REUSE);
    }


    public static DsConfig getConfig(String name) {
        return dsConfigMap.get(name);
    }


    private static void initSqlSessionFactoryMap() {
        for (Map.Entry<String, DsConfig> entryConfig : dsConfigMap.entrySet()) {
            String dsConfigName = entryConfig.getKey();
            DsConfig dsConfig = entryConfig.getValue();
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment(dsConfigName, transactionFactory, dsConfig.createDataSource());
            Configuration configuration = new Configuration(environment);
            for (String resource : getMybatisXMLMappers()) {
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(getResourceAsStream(resource),
                        configuration, resource, configuration.getSqlFragments());
                mapperParser.parse();
            }
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
            DbSqlSessionFactory dbSqlSessionFactory = new DbSqlSessionFactory(sqlSessionFactory, dsConfig);
            Context.sqlSessionFactoryMap.put(dsConfigName, dbSqlSessionFactory);
        }
    }

    private static InputStream getResourceAsStream(String name) {
        InputStream resourceStream = null;
        ClassLoader classLoader = null;
        if (resourceStream == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            resourceStream = classLoader.getResourceAsStream(name);
            if (resourceStream == null) {
                classLoader = AppConfig.class.getClassLoader();
                resourceStream = classLoader.getResourceAsStream(name);
            }
        }
        return resourceStream;
    }

    private static List<String> getMybatisXMLMappers() {
        List<String> xmlMapperLocations = new ArrayList<>();
        // 扫描指定目录下的所有xml文件
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (Enumeration<URL> urls = classLoader.getResources(DB_MAPPER_LOCATION); urls.hasMoreElements(); ) {
                xmlMapperLocations.addAll(findMapperByUrl(urls.nextElement()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return xmlMapperLocations;
    }

    private static List<String> findMapperByUrl(URL url) throws IOException, URISyntaxException {
        if (isJarURL(url)) {
            return findMapperFromJar(url);
        } else {
            return findMapperFromFile(url);
        }
    }

    private static List<String> findMapperFromFile(URL url) throws URISyntaxException, MalformedURLException {
        List<String> mapperLocations = new ArrayList<>();
        File file = new File(url.toURI());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fileInDir : files) {
                if (fileInDir.isFile() && fileInDir.getPath().endsWith(".xml")) {
                    int dbMapperLocationIndex = fileInDir.toURI().toURL().getPath().indexOf(DB_MAPPER_LOCATION);
                    mapperLocations.add(fileInDir.getPath().substring(dbMapperLocationIndex - 1));
                }
            }
        }
        return mapperLocations;
    }

    private static List<String> findMapperFromJar(URL url) throws IOException {
        List<String> mapperLocations = new ArrayList<>();
        URLConnection urlConnection = url.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            JarFile jarFile = ((JarURLConnection) urlConnection).getJarFile();
            for (Enumeration<JarEntry> jarEntries = jarFile.entries(); jarEntries.hasMoreElements(); ) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().indexOf(DB_MAPPER_LOCATION) != -1 && jarEntry.getName().endsWith(".xml")) {
                    mapperLocations.add(jarEntry.getName());
                }
            }
        }

        return mapperLocations;
    }

    private static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) ||
                URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol));
    }

}
