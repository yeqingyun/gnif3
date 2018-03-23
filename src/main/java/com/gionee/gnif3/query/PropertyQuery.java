package com.gionee.gnif3.query;

import java.util.List;

/**
 * Created by Leon.Yu on 2016/10/20.
 */
public class PropertyQuery<PropertyEntity> extends BaseQuery<PropertyEntity> {
    private String key;

    public PropertyQuery key(String key) {
        this.key = key;
        return this;
    }

    @Override
    protected List<PropertyEntity> query(Page page) {
        return session.selectList("selectPropertyByKey", this);
    }

    @Override
    protected long queryCount() {
        return 0;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
