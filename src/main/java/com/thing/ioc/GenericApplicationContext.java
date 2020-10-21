package com.thing.ioc;

import com.thing.ioc.entity.Bean;
import com.thing.ioc.entity.BeanDefinition;
import com.thing.ioc.io.BeanDefinitionReader;
import com.thing.ioc.io.XmlBeanDefinitionReader;

import java.lang.reflect.Method;
import java.util.*;

public class GenericApplicationContext implements ApplicationContext {
    private BeanDefinitionReader definitionReader;
    private List<Bean> beans;
    private List<BeanDefinition> beanDefinitions;

    public GenericApplicationContext(String... paths) {
        this(new XmlBeanDefinitionReader(paths));
    }

    public GenericApplicationContext(BeanDefinitionReader beanDefinitionReader) {
        definitionReader = beanDefinitionReader;

        beanDefinitions = definitionReader.readBeanDefinitions();
        beans = createBeans(beanDefinitions);
        injectValueDependencies(beans, beanDefinitions);
        injectRefDependencies(beans, beanDefinitions);

    }

    List<Bean> createBeans(List<BeanDefinition> beanDefinitions) {
        List<Bean> createdBeans = new ArrayList<>();
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Class<?> clazz = Class.forName(beanDefinition.getClassName());
                Object value = clazz.getConstructor().newInstance();

                Bean bean = new Bean(beanDefinition.getId(), value);
                createdBeans.add(bean);
            }
            return createdBeans;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Create beans failed", e);
        }
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
            throw new RuntimeException("Bean with id:" + id + " has deffernt class than: " + clazz.getName());
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
