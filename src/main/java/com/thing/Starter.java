package com.thing;

import com.thing.ioc.ApplicationContext;
import com.thing.ioc.GenericApplicationContext;

public class Starter {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new GenericApplicationContext("src/main/resources/context.xml");
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.activateUsers();
    }
}
