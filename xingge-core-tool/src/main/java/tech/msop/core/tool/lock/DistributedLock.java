package tech.msop.core.tool.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁顶级接口
 *
 * @author ruozhuliufeng
 */
public interface DistributedLock {
    /**
     * 自动获取锁
     *
     * @param lockName  锁名
     * @param lockType  锁类型
     * @param leaseTime 自动解锁时间，默认100
     * @param timeUnit  时间单位
     * @return 锁对象
     */
    XingGeLock lock(String lockName, long leaseTime, TimeUnit timeUnit, LockType lockType) throws Exception;

    /**
     * 默认重入锁
     */
    default XingGeLock lock(String lockName, long leaseTime, TimeUnit timeUnit) throws Exception {
        return this.lock(lockName, leaseTime, timeUnit, LockType.REENTRANT);
    }

    default XingGeLock lock(String lockName, LockType lockType) throws Exception {
        return this.lock(lockName, -1, TimeUnit.SECONDS, lockType);
    }

    default XingGeLock lock(String lockName) throws Exception {
        return this.lock(lockName, LockType.REENTRANT);
    }

    /**
     * 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param lockName  锁的key
     * @param waitTime  获取锁的最大尝试时间(单位 {@code unit})
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @return 锁对象，如果获取锁失败则为null
     */
    XingGeLock tryLock(String lockName, long waitTime, long leaseTime, TimeUnit unit, LockType lockType) throws Exception;

    default XingGeLock tryLock(String lockName, long waitTime, long leaseTime, TimeUnit unit) throws Exception {
        return this.tryLock(lockName, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    default XingGeLock tryLock(String lockName, long waitTime, TimeUnit unit, LockType lockType) throws Exception {
        return this.tryLock(lockName, waitTime, -1, unit, lockType);
    }

    default XingGeLock tryLock(String lockName, long waitTime, TimeUnit unit) throws Exception {
        return this.tryLock(lockName, waitTime, -1, unit, LockType.REENTRANT);
    }


    /**
     * 释放锁
     *
     * @param lock 锁对象
     * @throws Exception 异常信息
     */
    void unlock(Object lock) throws Exception;

    /**
     * 释放锁
     *
     * @param xgLock 锁对象抽象
     * @throws Exception 异常信息
     */
    default void unlock(XingGeLock xgLock) throws Exception {
        if (xgLock != null) {
            this.unlock(xgLock.getLock());
        }
    }
}
