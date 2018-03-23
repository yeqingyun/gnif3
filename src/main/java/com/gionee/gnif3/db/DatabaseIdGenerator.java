package com.gionee.gnif3.db;


import com.gionee.gnif3.command.BaseCommand;
import com.gionee.gnif3.config.AppConfig;
import com.gionee.gnif3.context.IContext;
import com.gionee.gnif3.context.ThreadContextHolder;
import com.gionee.gnif3.entity.PropertyEntity;
import com.gionee.gnif3.exception.GnifException;
import com.gionee.gnif3.exception.GnifRuntimeException;
import com.gionee.gnif3.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Leon on 2016/4/25.
 */
public class DatabaseIdGenerator implements IdGenerator {

    private static final int DEFAULT_ID_BLOCK_SIZE = 5;

    private DsConfig dsConfig;

    private Map<String, IdBlockInfo> blockCache = new ConcurrentHashMap<>();

    public DatabaseIdGenerator() {
        this.dsConfig = AppConfig.getDefaultDsConfig();
    }

    public DatabaseIdGenerator(String dsConfigName) {
        this.dsConfig = AppConfig.getConfig(dsConfigName);
    }

    public DatabaseIdGenerator(DsConfig dsConfig) {
        this.dsConfig = dsConfig;
    }

    @Override
    public synchronized long getNextId(String key) {
        IdBlockInfo idBlockInfo;
        // 缓存了对应的key值
        if (blockCache.containsKey(key)) {
            idBlockInfo = blockCache.get(key);
            if (idBlockInfo.maxId > idBlockInfo.nextId) {
                return ++idBlockInfo.nextId;
            }
        }

        idBlockInfo = getNewIdBlockInfo(key);
        return idBlockInfo.nextId;
    }

    private IdBlockInfo getNewIdBlockInfo(String key) {
        DatabaseIdGenerateCommand databaseIdGenerateCommand = new DatabaseIdGenerateCommand(key);
        databaseIdGenerateCommand.execute(this.dsConfig.setSessionType(SessionType.RENEW));
        IdBlockInfo idBlockInfo = databaseIdGenerateCommand.getIdBlockInfo();
        blockCache.put(key, idBlockInfo);

        return idBlockInfo;
    }

    private class DatabaseIdGenerateCommand extends BaseCommand<Long> {

        private String key;

        private IdBlockInfo idBlockInfo;

        private int tryTime = 10;

        public IdBlockInfo getIdBlockInfo() {
            return this.idBlockInfo;
        }

        public DatabaseIdGenerateCommand(String key) {
            this.key = key;
        }

        @Override
        public void perform(IContext context) {
            getNewIdBlockInfo(context, this.tryTime);
        }

        private int getBlockSize() {
            String blockSizeStr = AppConfig.getPropertyValue(key + ".blockSize");
            if (StringUtils.hasText(blockSizeStr)) {
                return Integer.parseInt(blockSizeStr);
            }

            return DEFAULT_ID_BLOCK_SIZE;
        }

        private void getNewIdBlockInfo(IContext context, int tryTime) {
            if (tryTime > 0) {
                DbSqlSession dbSqlSession = ThreadContextHolder.getContext().getSession();
                PropertyEntity property = (PropertyEntity) dbSqlSession.selectOne("selectPropertyByKey", key);
                if (property != null) {
                    long curId = Long.parseLong(property.getValue().toString());
                    this.idBlockInfo = new IdBlockInfo(curId + 1, curId + getBlockSize());
                    property.setValue(this.idBlockInfo.maxId);
                    try {
                        dbSqlSession.update(property);
                        dbSqlSession.flush();
                    } catch (GnifRuntimeException e) {
                        // 若更新出错，则表示资源已被其他线程更新，那么接着进行重试tryTime次
                        getNewIdBlockInfo(context, tryTime--);
                    }
                } else {
                    property = new PropertyEntity(key, 1l);
                    dbSqlSession.getSqlSession().insert("insertProperty", property);
                    dbSqlSession.flush();
                    perform(context);
                }
            }
        }
    }

    private class IdBlockInfo {
        private long nextId;
        private long maxId;

        public IdBlockInfo(long nextId, long maxId) {
            this.nextId = nextId;
            this.maxId = maxId;
        }

    }
}
