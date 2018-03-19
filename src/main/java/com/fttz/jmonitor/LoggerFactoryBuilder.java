package com.fttz.jmonitor;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * </p>
 *
 * @author fttz8706
 * @since 18/3/19
 */
public class LoggerFactoryBuilder {

    private String resource;

    public LoggerFactoryBuilder resource(String resource) {
        this.resource = resource;
        return this;
    }

    public ILoggerFactory build() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        try {
            Class<?> context = Class.forName("ch.qos.logback.core.Context");
            Class<?> joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator");
            Object joranConfiguratoroObj = joranConfigurator.newInstance();
            Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
            setContext.invoke(joranConfiguratoroObj, loggerFactory);
            URL url = LoggerFactoryBuilder.class.getClassLoader().getResource(resource);
            Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
            doConfigure.invoke(joranConfiguratoroObj, url);
        } catch (Exception e) {
            System.err.println(e);
        }
        return loggerFactory;
    }
}
