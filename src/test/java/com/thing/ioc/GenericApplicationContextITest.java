package com.thing.ioc;

import com.thing.service.DefaultMailService;
import com.thing.service.DefaultUserService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GenericApplicationContextITest {
    private final GenericApplicationContext APPLICATION_CONTEXT = new GenericApplicationContext("src/main/resources/context.xml");

    @Test
    public void testGetBeanById() {
        DefaultMailService mailService = (DefaultMailService) APPLICATION_CONTEXT.getBean("mailService");
        assertNotNull(mailService);
        assertEquals(1099, mailService.getPort());
        assertEquals("POP3", mailService.getProtocol());

        DefaultUserService userService = (DefaultUserService) APPLICATION_CONTEXT.getBean("userService");
        assertNotNull(userService.getMailService());
    }

    @Test
    public void testGetBeanByClass() {
        DefaultMailService mailService = APPLICATION_CONTEXT.getBean(DefaultMailService.class);
        assertNotNull(mailService);
        assertEquals(1099, mailService.getPort());
        assertEquals("POP3", mailService.getProtocol());

        DefaultUserService userService = APPLICATION_CONTEXT.getBean(DefaultUserService.class);
        assertNotNull(userService.getMailService());
    }

    @Test
    public void testGetBeanByIdAndClass() {
        DefaultMailService mailService = APPLICATION_CONTEXT.getBean("mailService", DefaultMailService.class);
        assertNotNull(mailService);
        assertEquals(1099, mailService.getPort());
        assertEquals("POP3", mailService.getProtocol());

        DefaultUserService userService = APPLICATION_CONTEXT.getBean("userService", DefaultUserService.class);
        assertNotNull(userService.getMailService());
    }

    @Test
    public void testGetBeanNames() {
        List<String> beanNames = APPLICATION_CONTEXT.getBeanNames();
        assertEquals(2, beanNames.size());
        assertEquals("mailService", beanNames.get(0));
        assertEquals("userService", beanNames.get(1));
    }
}
