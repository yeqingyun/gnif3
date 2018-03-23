package com.gionee.gnif3.entity;

/**
 * Created by Leon on 2016/4/26.
 */
public class PropertyEntity extends BaseEntity implements HasRevision {
    private String key;
    private Long value;

    public PropertyEntity() {

    }

    public PropertyEntity(String key, Long value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public Integer getRevisionNext() {
        return getRevision() + 1;
    }
}
