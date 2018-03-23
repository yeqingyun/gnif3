package com.gionee.gnif3.api;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public interface CommandBus {

    void subscribe(String commandName, CommandHandler<? extends CommandMessage<?>, ?> commandHandler);

    <C> Object dispatch(CommandMessage<C> commandMessage);
}
