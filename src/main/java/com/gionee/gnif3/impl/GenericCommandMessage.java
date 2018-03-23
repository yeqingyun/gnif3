package com.gionee.gnif3.impl;

import com.gionee.gnif3.api.CommandMessage;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class GenericCommandMessage<C> implements CommandMessage<C> {
    private C payload;

    public GenericCommandMessage(C payload) {
        this.payload = payload;
    }

    public C getPayload() {
        return this.payload;
    }

    @Override
    public String getCommandName() {
        return null;
    }
}
