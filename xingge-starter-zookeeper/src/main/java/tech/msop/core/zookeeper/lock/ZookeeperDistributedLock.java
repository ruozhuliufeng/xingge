package tech.msop.core.zookeeper.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import tech.msop.core.tool.constant.StringConstant;
import tech.msop.core.tool.exception.LockException;
import tech.msop.core.tool.lock.DistributedLock;
import tech.msop.core.tool.lock.LockType;
import tech.msop.core.tool.lock.MLock;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 基于ZK实现的分布式锁
 */
@Slf4j
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnProperty(prefix = "ms.lock",name = "lockerType",havingValue = "ZK")
public class ZookeeperDistributedLock implements DistributedLock {
    @Resource
    private CuratorFramework client;
    private MLock getLock(String lockName){
        InterProcessMutex lock = new InterProcessMutex(client,lockName);
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
        MLock mLock = getLock(lockName);
        InterProcessMutex ipm = (InterProcessMutex) mLock.getLock();
        ipm.acquire();
        return mLock;
    }

    /**
     * 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param lockName  锁的key
     * @param waitTime  获取锁的最大尝试时间(单位 {@code unit})
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @param lockType
     * @return 锁对象，如果获取锁失败则为null
     */
    @Override
    public MLock tryLock(String lockName, long waitTime, long leaseTime, TimeUnit unit, LockType lockType) throws Exception {
        MLock mLock = getLock(lockName);
        InterProcessMutex ipm = (InterProcessMutex) mLock.getLock();
        if (ipm.acquire(waitTime,unit)){
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
            if (lock instanceof InterProcessMutex){
                InterProcessMutex ipm = (InterProcessMutex) lock;
                if (ipm.isAcquiredInThisProcess()){
                    ipm.release();
                }

            }else {
                throw new LockException("require InterProcessMutex type","必须是InterProcessMutex的锁才可以解锁");
            }
        }
    }

    public String getPath(String lockName){
        return StringConstant.SLASH +  lockName;
    }
}
