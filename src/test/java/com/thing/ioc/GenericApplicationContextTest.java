package com.thing.ioc;

import com.thing.DefaultMailService;
import com.thing.DefaultUserService;
import com.thing.MailService;
import com.thing.ioc.entity.Bean;
import com.thing.ioc.entity.BeanDefinition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GenericApplicationContextTest {

    @Test
    public void testCreateBeans() {
        BeanDefinition firstBeanDefinition = new BeanDefinition();
        firstBeanDefinition.setId("mailService");
        firstBeanDefinition.setClassName("com.thing.DefaultMailService");

        BeanDefinition secondBeanDefinition = new BeanDefinition();
        secondBeanDefinition.setId("userService");
        secondBeanDefinition.setClassName("com.thing.DefaultUserService");

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        beanDefinitions.add(firstBeanDefinition);
        beanDefinitions.add(secondBeanDefinition);

        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        List<Bean> beans = genericApplicationContext.createBeans(beanDefinitions);

        assertEquals(2, beans.size());

        Bean firstBean = beans.get(0);
        assertEquals("mailService", firstBean.getId());
        String firstBeanClassName = firstBean.getValue().getClass().getName();
        assertEquals("com.thing.DefaultMailService", firstBeanClassName);

        Bean secondBean = beans.get(1);
        assertEquals("userService", secondBean.getId());
        String secondBeanClassName = secondBean.getValue().getClass().getName();
        assertEquals("com.thing.DefaultUserService", secondBeanClassName);
    }

    @Test
    public void testToProperType() {
        GenericApplicationContext applicationContext = new GenericApplicationContext();

        Object anInt = applicationContext.toProperType("123", "int");
        assertTrue(anInt instanceof Integer);
        assertEquals(123, anInt);

        Object aByte = applicationContext.toProperType("12", "byte");
        assertTrue(aByte instanceof Byte);
        assertEquals((byte) 12, aByte);

        Object aShort = applicationContext.toProperType("33", "short");
        assertTrue(aShort instanceof Short);
        assertEquals((short) 33, aShort);

        Object aLong = applicationContext.toProperType("12121654", "long");
        assertTrue(aLong instanceof Long);
        assertEquals(12121654L, aLong);

        Object aDouble = applicationContext.toProperType("121.225", "double");
        assertTrue(aDouble instanceof Double);
        assertEquals(121.225, aDouble);

        Object aFloat = applicationContext.toProperType("1.22", "float");
        assertTrue(aFloat instanceof Float);
        assertEquals(1.22f, aFloat);

        Object aBoolean = applicationContext.toProperType("true", "boolean");
        assertTrue(aBoolean instanceof Boolean);
        assertEquals(true, aBoolean);

        Object aChar = applicationContext.toProperType("a", "char");
        assertTrue(aChar instanceof Character);
        assertEquals('a', aChar);

        Object aString = applicationContext.toProperType("Some text", "java.lang.String");
        assertTrue(aString instanceof String);
        assertEquals("Some text", aString);
    }

    @Test
    public void testInjectValueDependencies() {
        Bean bean = new Bean("mailService", new DefaultMailService());

        List<Bean> beans = new ArrayList<>();
        beans.add(bean);

        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId("mailService");
        Map<String, String> valueDependencies = new HashMap<>();
        valueDependencies.put("port", "2233");
        valueDependencies.put("protocol", "GG99");
        beanDefinition.setValueDependencies(valueDependencies);

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        beanDefinitions.add(beanDefinition);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.injectValueDependencies(beans, beanDefinitions);

        DefaultMailService mailService = (DefaultMailService) beans.get(0).getValue();
        assertEquals(2233, mailService.getPort());
        assertEquals("GG99", mailService.getProtocol());

    }

    @Test
    public void testInjectRefDependencies(){
        List<Bean> beans = new ArrayList<>();

        Bean bean = new Bean("userService", new DefaultUserService());
        beans.add(bean);

        DefaultMailService mailService = new DefaultMailService();
        mailService.setPort(4455);
        mailService.setProtocol("123GT");
        beans.add(new Bean("defaultMailService", mailService));


        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId("userService");
        Map<String, String> refDependencies = new HashMap<>();
        refDependencies.put("mailService", "defaultMailService");
        beanDefinition.setRefDependencies(refDependencies);

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        beanDefinitions.add(beanDefinition);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.injectRefDependencies(beans, beanDefinitions);

        DefaultUserService userService = (DefaultUserService) beans.get(0).getValue();
        assertNotNull(userService.getMailService());

        DefaultMailService resultMailService = (DefaultMailService) userService.getMailService();
        assertEquals(4455, resultMailService.getPort());
        assertEquals("123GT", resultMailService.getProtocol());
    }

}