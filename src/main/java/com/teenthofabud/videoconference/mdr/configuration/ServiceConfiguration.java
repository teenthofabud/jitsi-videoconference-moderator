package com.teenthofabud.videoconference.mdr.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.error.core.MDRMessageSource;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.hibernate.validator.HibernateValidator;

import java.io.IOException;

@Configuration
public class ServiceConfiguration {

    @Bean
    public Validator validator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {

        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure().constraintValidatorFactory(new SpringConstraintValidatorFactory(autowireCapableBeanFactory))
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator;
    }

    @Bean
    public MDRMessageSource wmsMessageSource() throws IOException {
        MDRMessageSource messageSource = new MDRMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasenames("messages");
        return messageSource;
    }

    @Bean
    public ObjectMapper mapper(){
        return new ObjectMapper();
    }
}
