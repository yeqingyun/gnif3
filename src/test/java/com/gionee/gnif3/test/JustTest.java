package com.gionee.gnif3.test;

import com.gionee.gnif3.bus.CBus;
import com.gionee.gnif3.config.AppConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Leon.Yu on 2016/9/28.
 */
public class JustTest {
    @Test
    public void test() {
        AppConfig.init();

        CBus.subscribe(PropertyCreateCommand.class.getSimpleName(), new PropertyCreateCommandHandler());
        Boolean result = CBus.sendAndWait(new PropertyCreateCommand("newkey", 123), Boolean.class);
        Assert.assertTrue(result);
    }
}
