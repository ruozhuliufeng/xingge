package tech.msop.core.mongo.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.lang.Nullable;
import org.bson.BasicBSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import tech.msop.core.tool.jackson.JsonUtil;

/**
 * Mongo DB 对象转为Json Node
 *
 * @author ruozhuliufeng
 */
@ReadingConverter
public enum DBObjectToJsonNodeConverter implements Converter<BasicBSONObject, JsonNode> {
    /**
     * 实例
     */
    INSTANCE;


    @Override
    public JsonNode convert(@Nullable BasicBSONObject source) {
        if (source == null) {
            return null;
        }
        return JsonUtil.getInstance().valueToTree(source);
    }
}
