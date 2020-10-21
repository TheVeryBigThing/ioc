package com.thing.ioc.io;

import com.thing.ioc.entity.BeanDefinition;

import java.util.List;

public interface BeanDefinitionReader {
    List<BeanDefinition> readBeanDefinitions();
}
