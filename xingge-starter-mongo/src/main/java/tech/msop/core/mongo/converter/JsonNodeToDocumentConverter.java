package tech.msop.core.mongo.converter;


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import javax.annotation.Nonnull;

/**
 * JsonNode转 mongo Document
 *
 * @author ruozhuliufeng
 */
@WritingConverter
public enum JsonNodeToDocumentConverter implements Converter<ObjectNode, Document> {
    /**
     * 实例
     */
    INSTANCE;

    @Override
    public Document convert(@Nonnull ObjectNode source) {
        return Document.parse(source.toString());
    }
}
