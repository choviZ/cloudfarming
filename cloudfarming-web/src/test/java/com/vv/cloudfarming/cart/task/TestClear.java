package com.vv.cloudfarming.cart.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TestClear {

    @Autowired
    private ClearCartTask clearCartTask;

    @Test
    void testManualTrigger() throws NoSuchMethodException {
        clearCartTask.clear();
    }
}