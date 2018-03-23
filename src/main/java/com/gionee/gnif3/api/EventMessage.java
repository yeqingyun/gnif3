package com.gionee.gnif3.api;

/**
 * Created by Leon.Yu on 2016/9/30.
 */
public interface EventMessage<T> extends Message<T> {
    String getEventName();
}
