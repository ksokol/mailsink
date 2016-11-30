package com.github.ksokol.mailsink.configuration;

import com.github.ksokol.mailsink.converter.InputStreamToMailConverter;
import com.github.ksokol.mailsink.converter.MailToMime4jMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kamill Sokol
 */
@Configuration
public class MailsinkConfiguration {

    @Bean
    @MailsinkConversionService
    public ConversionServiceFactoryBean mailsinkConversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();

        Set<Converter<?, ?>> converter = new HashSet<>();
        converter.add(new MailToMime4jMessageConverter());
        converter.add(new InputStreamToMailConverter());

        bean.setConverters(converter);
        return bean;
    }
}
