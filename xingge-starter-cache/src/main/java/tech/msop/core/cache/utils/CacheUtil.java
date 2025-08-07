package tech.msop.core.cache.utils;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import tech.msop.core.tool.constant.StringConstant;
import tech.msop.core.tool.constant.XingGeConstant;
import tech.msop.core.tool.utils.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 缓存工具类
 *
 * @author ruozhuliufeng
 */
public class CacheUtil {
    private static CacheManager cacheManager;
    private static final Boolean TENANT_MODE = Boolean.TRUE;

    /**
     * 获取缓存工具类
     *
     * @return CacheManager
     */
    private static CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = SpringUtil.getBean(CacheManager.class);
        }
        return cacheManager;
    }

    /**
     * 获取缓存对象
     *
     * @param cacheName 缓存名
     * @return Cache
     */
    public static Cache getCache(String cacheName) {
        return getCache(cacheName, TENANT_MODE);
    }

    /**
     * 获取缓存对象
     *
     * @param cacheName  缓存名
     * @param tenantMode 租户模式
     * @return Cache
     */
    public static Cache getCache(String cacheName, Boolean tenantMode) {
        return getCacheManager().getCache(formatCacheName(cacheName, tenantMode));
    }

    /**
     * 获取缓存对象
     *
     * @param cacheName 缓存名
     * @param tenantId  租户ID
     * @return Cache
     */
    public static Cache getCache(String cacheName, String tenantId) {
        return getCacheManager().getCache(formatCacheName(cacheName, tenantId));
    }

    /**
     * 根据租户信息格式化缓存名
     *
     * @param cacheName  缓存名
     * @param tenantMode 租户模式
     * @return String
     */
    public static String formatCacheName(String cacheName, Boolean tenantMode) {
        if (!tenantMode) {
            return cacheName;
        }
        // TODO 暂无租户ID
        return formatCacheName(cacheName, "");
    }

    /**
     * 根据租户信息格式化缓存名
     *
     * @param cacheName 缓存名
     * @param tenantId  租户ID
     * @return String
     */
    public static String formatCacheName(String cacheName, String tenantId) {
        return (StringUtil.isBlank(tenantId) ? cacheName : tenantId.concat(StringConstant.COLON).concat(cacheName));
    }

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名
     * @param keyPrefix 缓存键前缀
     * @param key       缓存键值
     * @return Object
     */
    @Nullable
    public static Object get(String cacheName, String keyPrefix, Object key) {
        return get(cacheName, keyPrefix, key, TENANT_MODE);
    }

    /**
     * 获取缓存
     *
     * @param cacheName  缓存名
     * @param keyPrefix  缓存键前缀
     * @param key        缓存键值
     * @param tenantMode 租户模式
     * @return Object
     */
    @Nullable
    public static Object get(String cacheName, String keyPrefix, Object key, Boolean tenantMode) {
        if (Func.hasEmpty(cacheName, keyPrefix, key)) {
            return null;
        }
        Cache.ValueWrapper valueWrapper = getCache(cacheName, tenantMode).get(keyPrefix.concat(String.valueOf(key)));
        if (Func.isEmpty(valueWrapper)) {
            return null;
        }
        return valueWrapper.get();
    }

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名
     * @param keyPrefix 缓存键前缀
     * @param key       缓存键值
     * @param type      类型
     * @param <T>       泛型
     * @return T
     */
    @Nullable
    public static <T> T get(String cacheName, String keyPrefix, Object key, @Nullable Class<T> type) {
        return get(cacheName, keyPrefix, key, type, TENANT_MODE);
    }

    /**
     * 获取缓存
     *
     * @param cacheName  缓存名
     * @param keyPrefix  缓存键前缀
     * @param key        缓存键值
     * @param type       类型
     * @param tenantMode 租户模式
     * @param <T>        泛型
     * @return T
     */
    @Nullable
    public static <T> T get(String cacheName, String keyPrefix, Object key, @Nullable Class<T> type, Boolean tenantMode) {
        if (Func.hasEmpty(cacheName, keyPrefix, key)) {
            return null;
        }
        return getCache(cacheName, tenantMode).get(keyPrefix.concat(String.valueOf(key)), type);
    }

    /**
     * 获取缓存
     *
     * @param cacheName   缓存名
     * @param keyPrefix   缓存键前缀
     * @param key         缓存键值
     * @param valueLoader 重载对象
     * @param <T>         泛型
     * @return T
     */
    @Nullable
    public static <T> T get(String cacheName, String keyPrefix, Object key, Callable<T> valueLoader) {
        return get(cacheName, keyPrefix, key, valueLoader, TENANT_MODE);
    }

    /**
     * 获取缓存
     *
     * @param cacheName   缓存名
     * @param keyPrefix   缓存键前缀
     * @param key         缓存键值
     * @param valueLoader 重载对象
     * @param tenantMode  租户模式
     * @param <T>         泛型
     * @return T
     */
    @Nullable
    public static <T> T get(String cacheName, String keyPrefix, Object key, Callable<T> valueLoader, Boolean tenantMode) {
        if (Func.hasEmpty(cacheName, keyPrefix, key)) {
            return null;
        }
        try {
            Cache.ValueWrapper valueWrapper = getCache(cacheName, tenantMode).get(keyPrefix.concat(String.valueOf(key)));
            Object value = null;
            if (valueWrapper == null) {
                T call = valueLoader.call();
                if (ObjectUtil.isNotEmpty(call)) {
                    Field field = ReflectUtil.getField(call.getClass(), XingGeConstant.DB_PRIMARY_KEY);
                    if (ObjectUtil.isNotEmpty(field) && ObjectUtil.isEmpty(ClassUtil.getMethod(call.getClass(),XingGeConstant.DB_PRIMARY_KEY_METHOD).invoke(call))) {
                        return null;
                    }
                    getCache(cacheName, tenantMode).put(keyPrefix.concat(String.valueOf(key)), call);
                    value = call;
                }
            } else {
                value = valueWrapper.get();
            }
            return (T) value;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 设置缓存
     *
     * @param cacheName 缓存名
     * @param keyPrefix 缓存键前缀
     * @param key       缓存键值
     * @param value     缓存值
     */
    public static void put(String cacheName, String keyPrefix, Object key, @Nullable Object value) {
        put(cacheName, keyPrefix, key, value, TENANT_MODE);
    }

    /**
     * 设置缓存
     *
     * @param cacheName  缓存名
     * @param keyPrefix  缓存键前缀
     * @param key        缓存键值
     * @param value      缓存值
     * @param tenantMode 租户模式
     */
    public static void put(String cacheName, String keyPrefix, Object key, @Nullable Object value, Boolean tenantMode) {
        getCache(cacheName, tenantMode).put(keyPrefix.concat(String.valueOf(key)), value);
    }

    /**
     * 清除缓存
     *
     * @param cacheName 缓存名
     * @param keyPrefix 缓存键前缀
     * @param key       缓存键值
     */
    public static void evict(String cacheName, String keyPrefix, Object key) {
        evict(cacheName, keyPrefix, key, TENANT_MODE);
    }

    /**
     * 清除缓存
     *
     * @param cacheName  缓存名
     * @param keyPrefix  缓存键前缀
     * @param key        缓存键值
     * @param tenantMode 租户模式
     */
    public static void evict(String cacheName, String keyPrefix, Object key, Boolean tenantMode) {
        if (Func.hasEmpty(cacheName, keyPrefix, key)) {
            return;
        }
        getCache(cacheName, tenantMode).evict(keyPrefix.concat(String.valueOf(key)));
    }

    /**
     * 清空缓存
     *
     * @param cacheName 缓存名
     */
    public static void clear(String cacheName) {
        clear(cacheName, TENANT_MODE);
    }

    /**
     * 清空缓存
     *
     * @param cacheName  缓存名
     * @param tenantMode 租户模式
     */
    public static void clear(String cacheName, Boolean tenantMode) {
        if (Func.isEmpty(cacheName)) {
            return;
        }
        getCache(cacheName, tenantMode).clear();
    }

    /**
     * 清空缓存
     *
     * @param cacheName 缓存名
     * @param tenantId  租户ID
     */
    public static void clear(String cacheName, String tenantId) {
        if (Func.isEmpty(cacheName)) {
            return;
        }
        getCache(cacheName, tenantId).clear();
    }

    /**
     * 清空缓存
     *
     * @param cacheName 缓存名
     * @param tenantIds 租户ID集合
     */
    public static void clear(String cacheName, List<String> tenantIds) {
        if (Func.isEmpty(cacheName)) {
            return;
        }
        tenantIds.forEach(tenantId -> getCache(cacheName, tenantId).clear());
    }
}
