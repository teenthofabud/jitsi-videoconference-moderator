package com.teenthofabud.videoconference.mdr.error.core;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class MDRMessageSource extends ReloadableResourceBundleMessageSource{

    private static final String PROPERTIES_SUFFIX = ".properties";

    private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


    @Override
    protected ReloadableResourceBundleMessageSource.PropertiesHolder refreshProperties(String filename, ReloadableResourceBundleMessageSource.PropertiesHolder propHolder) {
        if (filename.contains("messages")) {
            return refreshClassPathProperties(filename, propHolder);
        } else {
            return super.refreshProperties(filename, propHolder);
        }
    }

    private ReloadableResourceBundleMessageSource.PropertiesHolder refreshClassPathProperties(String filename, ReloadableResourceBundleMessageSource.PropertiesHolder propHolder) {
        Properties properties = new Properties();
        long lastModified = -1;
        try {
            Resource[] resources = resolver.getResources(filename + PROPERTIES_SUFFIX);
            log.debug("{} message resources available", resources.length);
            for (Resource resource : resources) {
                String sourcePath = resource.getURI().toString().replace(PROPERTIES_SUFFIX, "");
                log.debug("Loading message resource: {}", sourcePath);
                ReloadableResourceBundleMessageSource.PropertiesHolder holder = super.refreshProperties(sourcePath, propHolder);
                properties.putAll(holder.getProperties());
                if (lastModified < resource.lastModified()) {
                    lastModified = resource.lastModified();
                }
            }
        } catch (IOException e) {
            log.debug("Unable to load message resources", e);
        }
        return new ReloadableResourceBundleMessageSource.PropertiesHolder(properties, lastModified);
    }

}