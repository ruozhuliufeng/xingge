package tech.msop.core.tool.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * 分布式锁注解，redisson，支持的锁的种类有很多，适合注解形式的只有重入锁、公平锁
 *
 * <p>
 * 1. 可重入锁（Reentrant Lock）<br>
 * 2. 公平锁（Fair Lock）<br>
 * 3. 联锁（MultiLock）<br>
 * 4. 红锁（RedLock）<br>
 * 5. 读写锁（ReadWriteLock）<br>
 * </p>
 *
 * @author ruozhuliufeng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Lock {
    /**
     * 分布式锁的key，必须：保持唯一性
     *
     * @return key
     */
    String value();

    /**
     * 分布式锁参数，可选，支持spring el # 读取方法参数和 @ 读取spring bean
     *
     * @return param
     */
    String param() default "";

    /**
     * 等待锁超时时间，默认30
     *
     * @return int
     */
    long waitTime() default 30;

    /**
     * 自动解锁时间，自动解锁时间一定要大于方法执行时间，否则会导致锁提前释放，默认 100
     *
     * @return int
     */
    long leaseTime() default 100;

    /**
     * 时间单位，默认秒
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 默认公平锁
     *
     * @return LockTpe
     */
    LockType type() default LockType.FAIR;
}
