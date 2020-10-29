package com.thing.ioc;

import com.thing.annotation.PostConstruct;
import com.thing.ioc.entity.Bean;
import com.thing.ioc.entity.BeanDefinition;
import com.thing.ioc.io.BeanDefinitionReader;
import com.thing.ioc.io.XmlBeanDefinitionReader;
import com.thing.processor.BeanFactoryPostProcessor;
import com.thing.processor.BeanPostProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GenericApplicationContext implements ApplicationContext {
    private List<Bean> beans;

    public GenericApplicationContext(String... paths) {
        this(new XmlBeanDefinitionReader(paths));
    }

    public GenericApplicationContext(BeanDefinitionReader beanDefinitionReader) {
        List<BeanDefinition> beanDefinitions = beanDefinitionReader.readBeanDefinitions();
        runBeanFactoryPostProcessors(beanDefinitions);
        beans = createBeans(beanDefinitions);
        injectValueDependencies(beans, beanDefinitions);
        injectRefDependencies(beans, beanDefinitions);
        postProcessBeforeInitialization();
        runPostConstructMethods();
        runPostProcessAfterInitialization();

    }

    private void runPostConstructMethods() {
        for (Bean bean : beans) {
            Class<?> aClass = bean.getValue().getClass();
            for (Method method : aClass.getMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    try {
                        method.invoke(bean.getValue());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Post construct failed!", e);
                    }
                }

            }
        }
    }

    private void runPostProcessAfterInitialization() {
        for (Bean bean : beans) {
            if (isImplementing(bean.getValue().getClass(), BeanPostProcessor.class)) {
                runPostProcessOnBeans(bean, "postProcessAfterInitialization");
            }
        }
    }

    private void postProcessBeforeInitialization() {
        for (Bean bean : beans) {
            if (isImplementing(bean.getValue().getClass(), BeanPostProcessor.class)) {
                runPostProcessOnBeans(bean, "postProcessBeforeInitialization");
            }
        }
    }

    private void runPostProcessOnBeans(Bean systemBean, String methodName) {
        for (Bean bean : beans) {
            if (!bean.isSystem()) {
                try {
                    Class<?> aClass = systemBean.getValue().getClass();
                    Method method = aClass.getMethod(methodName, Object.class, String.class);
                    Object newBeanValue = method.invoke(systemBean.getValue(), bean, bean.getId());
                    bean.setValue(newBeanValue);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Post process before initialization failed!", e);
                }
            }
        }
    }

    private void runBeanFactoryPostProcessors(List<BeanDefinition> beanDefinitions) {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getClassName());
                if (isImplementing(clazz, BeanFactoryPostProcessor.class)) {
                    Object value = clazz.getConstructor().newInstance();
                    Method postProcessBeanFactory = clazz.getMethod("postProcessBeanFactory", List.class);
                    postProcessBeanFactory.invoke(value, beanDefinitions);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException("BeanFactoryPostProcessor failed!", e);
            }
        }
    }

    private boolean isImplementing(Class<?> clazz, Class<?> implementedClazz) {
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (anInterface.getName().equals(implementedClazz.getName())) {
                return true;
            }
        }
        return false;
    }

    List<Bean> createBeans(List<BeanDefinition> beanDefinitions) {
        List<Bean> createdBeans = new ArrayList<>();
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Class<?> clazz = Class.forName(beanDefinition.getClassName());
                Object value = clazz.getConstructor().newInstance();

                Bean bean = new Bean(beanDefinition.getId(), value);
                bean.setSystem(isSystemBean(bean));
                createdBeans.add(bean);
            }
            return createdBeans;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Create beans failed", e);
        }
    }

    private boolean isSystemBean(Bean bean) {
        Class<?> clazz = bean.getValue().getClass();
        if (isImplementing(clazz, BeanFactoryPostProcessor.class) || isImplementing(clazz, BeanPostProcessor.class)) {
            return true;
        }
        return false;
    }

    void injectValueDependencies(List<Bean> beansToInject, List<BeanDefinition> beanDefinitionsToInject) {
        try {
            for (BeanDefinition beanDefinition : beanDefinitionsToInject) {
                for (Bean bean : beansToInject) {
                    if (Objects.equals(beanDefinition.getId(), bean.getId())) {

                        Set<Map.Entry<String, String>> entries = beanDefinition.getValueDependencies().entrySet();

                        for (Map.Entry<String, String> entry : entries) {
                            Class<?> clazz = bean.getValue().getClass();
                            Method[] methods = clazz.getMethods();

                            String methodName = "set" + entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                            for (Method method : methods) {
                                if (method.getName().equals(methodName)) {
                                    method.invoke(bean.getValue(), toProperType(entry.getValue(), method.getParameterTypes()[0].getName()));
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Inject value dependencies failed", e);
        }
    }

    Object toProperType(String value, String parameterName) {
        if ("int".equals(parameterName)) {
            return Integer.parseInt(value);
        } else if ("long".equals(parameterName)) {
            return Long.parseLong(value);
        } else if ("double".equals(parameterName)) {
            return Double.parseDouble(value);
        } else if ("byte".equals(parameterName)) {
            return Byte.parseByte(value);
        } else if ("short".equals(parameterName)) {
            return Short.parseShort(value);
        } else if ("boolean".equals(parameterName)) {
            return Boolean.parseBoolean(value);
        } else if ("float".equals(parameterName)) {
            return Float.parseFloat(value);
        } else if ("char".equals(parameterName)) {
            char[] array = value.toCharArray();
            if (array.length == 1) {
                return array[0];
            } else {
                throw new IllegalArgumentException("Too much symbols in char value");
            }
        }
        return value;
    }

    void injectRefDependencies(List<Bean> beansToInject, List<BeanDefinition> beanDefinitionsToInject) {
        try {
            for (BeanDefinition beanDefinition : beanDefinitionsToInject) {
                for (Bean bean : beansToInject) {
                    if (Objects.equals(beanDefinition.getId(), bean.getId())) {

                        Set<Map.Entry<String, String>> entries = beanDefinition.getRefDependencies().entrySet();
                        for (Map.Entry<String, String> entry : entries) {
                            Class<?> clazz = bean.getValue().getClass();
                            Method[] methods = clazz.getMethods();
                            String methodName = "set" + entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);

                            for (Method method : methods) {
                                if (method.getName().equals(methodName)) {

                                    for (Bean valueBean : beansToInject) {
                                        if (valueBean.getId().equals(entry.getValue())) {
                                            Object value = valueBean.getValue();
                                            method.invoke(bean.getValue(), value);
                                        }
                                    }

                                }
                            }

                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Inject ref dependencies failed", e);
        }
    }

    @Override
    public Object getBean(String beanId) {
        for (Bean bean : beans) {
            if (Objects.equals(bean.getId(), beanId)) {
                return bean.getValue();
            }
        }
        throw new RuntimeException("No bean found for id: " + beanId);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        int duplicatesCount = 0;
        Object value = null;
        for (Bean bean : beans) {
            if (bean.getValue().getClass().equals(clazz)) {
                duplicatesCount++;
                value = bean.getValue();
            }
        }

        if (duplicatesCount > 1) {
            throw new RuntimeException("More than one bean found with class: " + clazz.getName() + ", try getBean(String beanId).");
        }

        if (duplicatesCount == 0) {
            throw new RuntimeException("No bean found with class: " + clazz.getName());
        }

        return (T) value;
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        Object value = getBean(id);
        if (!value.getClass().equals(clazz)) {
            throw new RuntimeException("Bean with id:" + id + " has different class than: " + clazz.getName());
        }
        return (T) value;
    }

    @Override
    public List<String> getBeanNames() {
        List<String> beanNames = new ArrayList<>();
        for (Bean bean : beans) {
            beanNames.add(bean.getId());
        }
        return beanNames;
    }
}
