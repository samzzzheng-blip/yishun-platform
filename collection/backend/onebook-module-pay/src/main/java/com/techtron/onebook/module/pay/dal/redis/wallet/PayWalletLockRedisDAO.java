package com.techtron.onebook.module.pay.dal.redis.wallet;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.techtron.onebook.module.pay.dal.redis.RedisKeyConstants.PAY_WALLET_LOCK;

/**
 * 支付钱包的锁 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class PayWalletLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long id, Long timeoutMillis, Callable<V> callable) throws Exception {
        String lockKey = formatKey(id);
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            // 最多等待 timeoutMillis，拿到锁后保留更充足的执行时间，避免慢事务过早释放锁。
            long leaseMillis = Math.max(timeoutMillis * 3, 30_000L);
            acquired = lock.tryLock(timeoutMillis, leaseMillis, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new IllegalStateException("钱包操作繁忙，请稍后重试");
            }
            // 执行逻辑
            return callable.call();
        } catch (Exception e) {
            throw e;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private static String formatKey(Long id) {
        return String.format(PAY_WALLET_LOCK, id);
    }

}
