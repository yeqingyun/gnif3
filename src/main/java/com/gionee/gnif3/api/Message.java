package com.gionee.gnif3.api;

import java.io.Serializable;

/**
 * Created by Leon.Yu on 2016/9/30.
 */
public interface Message<T> extends Serializable {
    T getPayload();
}
