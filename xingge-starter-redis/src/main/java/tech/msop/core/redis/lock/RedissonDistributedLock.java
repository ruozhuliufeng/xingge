package tech.msop.core.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import tech.msop.core.tool.exception.LockException;
import tech.msop.core.tool.lock.DistributedLock;
import tech.msop.core.tool.lock.LockType;
import tech.msop.core.tool.lock.MLock;

import java.util.concurrent.TimeUnit;

/**
 * Redisson 分布式锁实现，基本锁功能的抽象实现
 *
 * @author ruozhuliufeng
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "ms.lock",name = "lockerType", havingValue = "REDIS", matchIfMissing = true)
public class RedissonDistributedLock implements DistributedLock {
    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    private MLock getLock(String lockName,LockType lockType){
        RLock lock;
        if (LockType.FAIR == lockType){
            lock = redissonClient.getFairLock(lockName);
        }else {
            lock = redissonClient.getLock(lockName);
        }
        return new MLock(lock,this);
    }

    /**
     * 自动获取锁
     *
     * @param lockName  锁名
     * @param leaseTime 自动解锁时间，默认100
     * @param timeUnit  时间单位
     * @param lockType  锁类型
     * @return 锁对象
     */
    @Override
    public MLock lock(String lockName, long leaseTime, TimeUnit timeUnit, LockType lockType) throws Exception {
        MLock mLock = getLock(lockName,lockType);
        RLock lock = (RLock) mLock.getLock();
        lock.lock(leaseTime,timeUnit);
        return mLock;
    }

    /**
     * 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param lockName       锁的key
     * @param waitTime  获取锁的最大尝试时间(单位 {@code unit})
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @param lockType
     * @return 锁对象，如果获取锁失败则为null
     */
    @Override
    public MLock tryLock(String lockName, long waitTime, long leaseTime, TimeUnit unit, LockType lockType) throws Exception {
        MLock mLock = getLock(lockName,lockType);
        RLock lock = (RLock) mLock.getLock();
        if (lock.tryLock(waitTime,leaseTime,unit)){
            return mLock;
        }
        return null;
    }

    /**
     * 释放锁
     *
     * @param lock 锁对象
     * @throws Exception 异常信息
     */
    @Override
    public void unlock(Object lock) throws Exception {
        if (lock != null){
            if (lock instanceof RLock){
                RLock rLock = (RLock) lock;
                if (rLock.isLocked()){
                    rLock.unlock();
                }
            }else {
                throw new LockException("require RLock type","锁类型必须为RLock");
            }
        }
    }
}
