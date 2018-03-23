package com.gionee.gnif3.bus;

import com.gionee.gnif3.api.CommandBus;
import com.gionee.gnif3.api.CommandHandler;
import com.gionee.gnif3.api.CommandMessage;
import com.gionee.gnif3.impl.GenericCommandMessage;

/**
 * Command Bus
 * Created by Leon.Yu on 2016/10/14.
 */
public class CBus {

    static CommandBusProxy commandBusProxy;

    public static void setCommandBusProxy(CommandBusProxy commandBusProxy) {
        CBus.commandBusProxy = commandBusProxy;
    }

    public static class CommandBusProxy {
        private CommandBus commandBus;

        public CommandBusProxy(CommandBus commandBus) {
            this.commandBus = commandBus;
        }

        public <C> Object dispatch(CommandMessage<C> commandMessage) {
            return commandBus.dispatch(commandMessage);
        }

        public void subscribe(String commandName, CommandHandler<? extends CommandMessage<?>, ?> commandHandler) {
            commandBus.subscribe(commandName, commandHandler);
        }
    }


    public static <C, R> R sendAndWait(C command, Class<R> resultType) {
        return (R) commandBusProxy.dispatch(new GenericCommandMessage<>(command));
    }

    public static void subscribe(String commandName, CommandHandler<? extends CommandMessage<?>, ?> commandHandler) {
        commandBusProxy.subscribe(commandName, commandHandler);
    }

}
