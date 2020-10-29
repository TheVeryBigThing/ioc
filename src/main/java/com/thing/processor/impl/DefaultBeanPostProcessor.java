package com.thing.processor.impl;

import com.thing.ioc.entity.Bean;
import com.thing.processor.BeanPostProcessor;

public class DefaultBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String id) {
        System.out.println("Post process before initialization finished!");
        Bean resultBean = (Bean) bean;
        return resultBean.getValue();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String id) {
        System.out.println("Post process after initialization finished!");
        Bean resultBean = (Bean) bean;
        return resultBean.getValue();
    }
}
