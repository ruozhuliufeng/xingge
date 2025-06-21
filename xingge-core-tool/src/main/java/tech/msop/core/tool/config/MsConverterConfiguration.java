package tech.msop.core.tool.config;

import tech.msop.core.tool.convert.EnumToStringConverter;
import tech.msop.core.tool.convert.StringToEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ms enum <=> String 转换配置
 * @author ruozhuliufeng
 */
@Configuration
public class MsConverterConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new EnumToStringConverter());
        registry.addConverter(new StringToEnumConverter());
    }
}
