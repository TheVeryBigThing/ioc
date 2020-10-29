package com.thing.processor;

public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String id);

    Object postProcessAfterInitialization(Object bean, String id);
}
