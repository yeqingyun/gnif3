package com.gionee.gnif3.api;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public interface CommandGateway {

    <C> void send(C command);

    <C, R> void send(C command, CommandCallback<? super C, R> commandCallback);

    <C, R> R sendAndWait(C command, Class<R> resultType);
}
