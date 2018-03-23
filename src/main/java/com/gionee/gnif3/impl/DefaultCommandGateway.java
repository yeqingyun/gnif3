package com.gionee.gnif3.impl;

import com.gionee.gnif3.api.CommandBus;
import com.gionee.gnif3.api.CommandCallback;
import com.gionee.gnif3.api.CommandGateway;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class DefaultCommandGateway implements CommandGateway {
    private CommandBus commandBus;

    public DefaultCommandGateway(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    public DefaultCommandGateway(CommandBus commandBus, String packagesToScan) {
    }

    public <C> void send(C command) {
        commandBus.dispatch(new GenericCommandMessage<>(command));
    }

    public <C, R> void send(C command, CommandCallback<? super C, R> commandCallback) {

    }

    public <C, R> R sendAndWait(C command, Class<R> resultType) {
        return (R) commandBus.dispatch(new GenericCommandMessage<>(command));
    }

}
