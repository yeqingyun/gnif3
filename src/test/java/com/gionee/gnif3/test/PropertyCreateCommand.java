package com.gionee.gnif3.test;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
@Data
@NoArgsConstructor
public class PropertyCreateCommand {
    private String key;
    private long value;

    public PropertyCreateCommand(String key, long value) {
        this.key = key;
        this.value = value;
    }
}
