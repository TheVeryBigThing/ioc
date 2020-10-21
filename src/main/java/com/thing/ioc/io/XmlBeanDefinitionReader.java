package com.thing.ioc.io;

import com.thing.ioc.entity.BeanDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlBeanDefinitionReader implements BeanDefinitionReader {
    private String[] paths;

    public XmlBeanDefinitionReader(String... paths) {
        this.paths = paths;
    }

    @Override
    public List<BeanDefinition> readBeanDefinitions() {
        try{
            List<BeanDefinition> beanDefinitions = new ArrayList<>();

            for (String path : paths) {
                try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(path))) {
                    beanDefinitions.addAll(getBeanDefinitions(inputStream));
                }
            }

            return beanDefinitions;

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Read bean definitions failed", e);
        }
    }

    List<BeanDefinition> getBeanDefinitions(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XmlHandler handler = new XmlHandler();

        saxParser.parse(inputStream, handler);

        return handler.getBeanDefinitions();
    }

    private static class XmlHandler extends DefaultHandler {
        private List<BeanDefinition> beanDefinitions = new ArrayList<>();
        private BeanDefinition tmpBeanDefinition;
        private Map<String, String> tmpValueDependencies;
        private Map<String, String> tmpRefDependencies;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("bean".equalsIgnoreCase(qName)){
                tmpBeanDefinition = new BeanDefinition();
                tmpBeanDefinition.setId(attributes.getValue("id"));
                tmpBeanDefinition.setClassName(attributes.getValue("class"));
                tmpValueDependencies = new HashMap<>();
                tmpRefDependencies = new HashMap<>();
            }

            if ("property".equalsIgnoreCase(qName)) {
                String name = attributes.getValue("name");
                String value = attributes.getValue("value");
                String ref = attributes.getValue("ref");

                if (ref == null){
                    tmpValueDependencies.put(name, value);
                } else {
                    tmpRefDependencies.put(name, ref);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("bean".equalsIgnoreCase(qName)) {
                tmpBeanDefinition.setValueDependencies(tmpValueDependencies);
                tmpBeanDefinition.setRefDependencies(tmpRefDependencies);
                beanDefinitions.add(tmpBeanDefinition);
                tmpBeanDefinition = null;
                tmpValueDependencies = null;
                tmpRefDependencies = null;
            }
        }

        public List<BeanDefinition> getBeanDefinitions() {
            return beanDefinitions;
        }
    }
}
