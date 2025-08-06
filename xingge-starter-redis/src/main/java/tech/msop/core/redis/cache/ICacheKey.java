package tech.msop.core.redis.cache;

import org.springframework.lang.Nullable;
import tech.msop.core.tool.constant.StringConstant;
import tech.msop.core.tool.utils.ObjectUtil;
import tech.msop.core.tool.utils.StringUtil;

import java.time.Duration;

/**
 * Cache Key
 *
 * @author ruozhuliufeng
 */
public interface ICacheKey {
    /**
     * 获取前缀
     *
     * @return key 前缀
     */
    String getPrefix();

    /**
     * 超时时间
     *
     * @return 超时时间
     */
    @Nullable
    default Duration getExpire() {
        return null;
    }

    default CacheKey getKey(Object... suffix){
        String prefix = this.getPrefix();
        // 拼接参数
        String key;
        if (ObjectUtil.isEmpty(suffix)){
            key = prefix;
        }else {
            key = prefix.concat(StringUtil.join(suffix, StringConstant.COLON));
        }
        Duration expire = this.getExpire();
        return expire == null ? new CacheKey(key) : new CacheKey(key,expire);
    }
}
