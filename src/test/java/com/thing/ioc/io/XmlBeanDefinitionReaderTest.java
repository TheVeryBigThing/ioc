package com.thing.ioc.io;

import com.thing.ioc.entity.BeanDefinition;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class XmlBeanDefinitionReaderTest {
    private static final String CONTEXT = "<beans>\n" +
            "    <bean id=\"defaultMailService\" class=\"com.thing.DefaultMailService\">\n" +
            "        <property name=\"port\" value=\"1099\"/>\n" +
            "        <property name=\"protocol\" value=\"POP3\"/>\n" +
            "    </bean>\n" +
            "\n" +
            "    <bean id=\"userService\" class=\"com.thing.DefaultUserService\">\n" +
            "        <property name=\"mailService\" ref=\"defaultMailService\" />\n" +
            "    </bean>\n" +
            "</beans>";

    @Test
    public void testGetBeanDefinitions() throws IOException, ParserConfigurationException, SAXException {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(CONTEXT.getBytes())) {
            List<BeanDefinition> beanDefinitions = beanDefinitionReader.getBeanDefinitions(inputStream);
            assertEquals(2, beanDefinitions.size());

            BeanDefinition firstBeanDefinition = beanDefinitions.get(0);
            assertEquals("defaultMailService", firstBeanDefinition.getId());
            assertEquals("com.thing.DefaultMailService", firstBeanDefinition.getClassName());

            assertTrue(firstBeanDefinition.getRefDependencies().isEmpty());

            Map<String, String> valueDependencies = firstBeanDefinition.getValueDependencies();
            assertEquals(2, valueDependencies.size());
            assertTrue(valueDependencies.containsKey("port"));
            assertTrue(valueDependencies.containsKey("protocol"));
            assertEquals("1099", valueDependencies.get("port"));
            assertEquals("POP3", valueDependencies.get("protocol"));

            BeanDefinition secondBeanDefinition = beanDefinitions.get(1);
            assertEquals("userService", secondBeanDefinition.getId());
            assertEquals("com.thing.DefaultUserService", secondBeanDefinition.getClassName());

            assertTrue(secondBeanDefinition.getValueDependencies().isEmpty());

            Map<String, String> refDependencies = secondBeanDefinition.getRefDependencies();
            assertEquals(1, refDependencies.size());
            assertTrue(refDependencies.containsKey("mailService"));
            assertEquals("defaultMailService", refDependencies.get("mailService"));

        }
    }

}