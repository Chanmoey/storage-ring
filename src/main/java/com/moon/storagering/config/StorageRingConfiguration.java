package com.moon.storagering.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class StorageRingConfiguration {

    private static final StorageRingConfiguration configuration;

    private static Properties properties;

    static {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        configuration = new StorageRingConfiguration();
        try {
            StorageRingConfiguration.properties = new Properties();
            Resource[] resources = resourcePatternResolver.getResources("classpath:*.properties");

            for (Resource resource : resources) {
                Properties prop = new Properties();
                InputStream inputStream = resource.getInputStream();
                prop.load(inputStream);
                inputStream.close();
                StorageRingConfiguration.properties.putAll(prop);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StorageRingConfiguration() {

    }

    public static StorageRingConfiguration getConfiguration() {
        return configuration;
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public long getLong(String key) {
        return Long.parseLong(properties.getProperty(key));
    }
}