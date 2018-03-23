package com.gionee.gnif3.impl;

import com.gionee.gnif3.api.*;
import com.gionee.gnif3.exception.NoCommandHandlerMatchedException;
import com.gionee.gnif3.unitofwork.CurrentUnitOfWork;
import com.gionee.gnif3.unitofwork.UnitOfWork;
import com.gionee.gnif3.unitofwork.UnitOfWorkFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class SimpleCommandBus implements CommandBus {

    private Iterable<CommandHandlerInterceptor> handlerInterceptors = Collections.EMPTY_LIST;
    private Iterable<CommandHandlerInterceptor> dispatchInterceptors = Collections.EMPTY_LIST;
    private Map<String, CommandHandler<? extends CommandMessage<?>, ?>> commandHandlerMap = new ConcurrentHashMap<>();
    private UnitOfWorkFactory unitOfWorkFactory;

    public SimpleCommandBus(UnitOfWorkFactory unitOfWorkFactory) {
        this.unitOfWorkFactory = unitOfWorkFactory;
        //TODO init commandHandlerMap
    }

    public void subscribe(String commandName, CommandHandler<? extends CommandMessage<?>, ?> commandHandler) {
        commandHandlerMap.put(commandName, commandHandler);
    }

    public <C> Object dispatch(CommandMessage<C> commandMessage) {
        UnitOfWork unitOfWork = unitOfWorkFactory.createUnitOfWork();
        CurrentUnitOfWork.set(unitOfWork);
        CommandHandler commandHandler = findCommandHandler(commandMessage);
        return unitOfWork.execute(() -> commandHandler.handle(commandMessage));
    }

    private <C> CommandHandler findCommandHandler(CommandMessage<C> commandMessage) {
        C command = commandMessage.getPayload();
        String commandName = command.getClass().getSimpleName();
        if (commandHandlerMap.containsKey(commandName)) {
            return commandHandlerMap.get(commandName);
        } else {
            throw new NoCommandHandlerMatchedException();
        }
    }

    public void setHandlerInterceptors(Iterable<CommandHandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptors = handlerInterceptors;
    }

    public void setDispatchInterceptors(Iterable<CommandHandlerInterceptor> dispatchInterceptors) {
        this.dispatchInterceptors = dispatchInterceptors;
    }

    public void setUnitOfWorkFactory(UnitOfWorkFactory unitOfWorkFactory) {
        this.unitOfWorkFactory = unitOfWorkFactory;
    }
}
