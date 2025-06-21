package tech.msop.core.tool.convert;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.msop.core.tool.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class StringToEnumConverter implements ConditionalGenericConverter {
    /**
     * 缓存Enum 类信息，提高性能
     */
    private static final ConcurrentMap<Class<?>, AccessibleObject> ENUM_CACHE_MAP = new ConcurrentHashMap<>(8);

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T valueOf(Class<?> clazz, String value) {
        return Enum.valueOf((Class<T>) clazz, value);
    }

    @Nullable
    private static AccessibleObject getAnnotation(Class<?> clazz) {
        Set<AccessibleObject> accessibleObjects = new HashSet<>();
        // JsonValue METHOD,FIELD
        Field[] fields = clazz.getDeclaredFields();
        Collections.addAll(accessibleObjects, fields);
        // methods
        Method[] methods = clazz.getDeclaredMethods();
        Collections.addAll(accessibleObjects, methods);
        for (AccessibleObject accessibleObject : accessibleObjects) {
            // 复用 jackson 的 JsonCreator 注解
            JsonCreator jsonCreator = accessibleObject.getAnnotation(JsonCreator.class);
            if (jsonCreator != null && JsonCreator.Mode.DISABLED != jsonCreator.mode()) {
                accessibleObject.setAccessible(true);
                return accessibleObject;
            }
        }
        return null;
    }

    @Nullable
    private static Object invoke(Class<?> clazz,
                                 AccessibleObject accessibleObject,
                                 String value)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (accessibleObject instanceof Constructor) {
            Constructor constructor = (Constructor) accessibleObject;
            Class<?> paramType = constructor.getParameterTypes()[0];
            // 类型转换
            Object object = ConvertUtil.convert(value, paramType);
            return constructor.newInstance(object);
        }
        if (accessibleObject instanceof Method) {
            Method method = (Method) accessibleObject;
            Class<?> paramType = method.getParameterTypes()[0];
            // 类型转换
            Object object = ConvertUtil.convert(value, paramType);
            return method.invoke(clazz, object);
        }
        return null;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return true;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Enum.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Class<?> sourceClazz = sourceType.getType();
        AccessibleObject accessibleObject = ENUM_CACHE_MAP.computeIfAbsent(sourceClazz, StringToEnumConverter::getAnnotation);
        String value = ((String) source).trim();
        // 如果为null，走默认的转换
        if (accessibleObject == null) {
            return valueOf(sourceClazz, value);
        }
        try {
            return StringToEnumConverter.invoke(sourceClazz, accessibleObject, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
