package com.gionee.gnif3.test;

import com.gionee.gnif3.api.CommandHandler;
import com.gionee.gnif3.api.CommandMessage;
import com.gionee.gnif3.bus.EBus;
import com.gionee.gnif3.entity.PropertyEntity;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class PropertyCreateCommandHandler implements CommandHandler<CommandMessage<PropertyCreateCommand>, Boolean> {

    public PropertyCreateCommandHandler() {

    }

    public Boolean handle(CommandMessage<PropertyCreateCommand> commandMessage) {
        PropertyCreateCommand payload = commandMessage.getPayload();
        PropertyEntity newPropertyEntity = new PropertyEntity();
        newPropertyEntity.setKey(payload.getKey());
        newPropertyEntity.setValue(payload.getValue());
        EBus.insert(newPropertyEntity);

        return true;
    }
}
