package com.thing.processor.impl;

import com.thing.ioc.entity.BeanDefinition;
import com.thing.processor.BeanFactoryPostProcessor;

import java.util.List;

public class DefaultBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(List<BeanDefinition> beanDefinitions) {
        System.out.println("Post process bean factory finished!");
    }
}
