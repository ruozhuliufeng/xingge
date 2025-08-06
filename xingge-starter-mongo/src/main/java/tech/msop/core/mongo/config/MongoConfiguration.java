package tech.msop.core.mongo.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tech.msop.core.mongo.converter.DBObjectToJsonNodeConverter;
import tech.msop.core.mongo.converter.JsonNodeToDocumentConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Mongo 配置
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration
public class MongoConfiguration {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>(2);
        converters.add(DBObjectToJsonNodeConverter.INSTANCE);
        converters.add(JsonNodeToDocumentConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }
}
