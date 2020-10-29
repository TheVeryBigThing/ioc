package com.thing;

import com.thing.ioc.ApplicationContext;
import com.thing.ioc.GenericApplicationContext;
import com.thing.service.UserService;

public class Starter {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext applicationContext = new GenericApplicationContext("src/main/resources/context.xml");
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.activateUsers();
    }
}
