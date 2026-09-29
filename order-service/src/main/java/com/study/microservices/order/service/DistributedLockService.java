package com.study.microservices.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private final StringRedisTemplate redisTemplate;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Thu thập Distributed Lock bằng lệnh SETNX (SET IF NOT EXISTS) của Redis với TTL.
     * @param lockKey Key định danh tài nguyên cần khóa (vd: lock:product:1)
     * @param lockValue Giá trị định danh ai đang giữ khóa (vd: UUID hoặc thread name)
     * @param ttlSeconds Thời gian tối đa giữ khóa để tránh Deadlock nếu server đột tử
     * @return true nếu lấy khóa thành công, false nếu tài nguyên đang bị tiến trình khác khóa
     */
    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
        boolean acquired = Boolean.TRUE.equals(success);
        if (acquired) {
            log.info("--> [REDIS LOCK ACQUIRED] Key='{}', Owner='{}', TTL={}s", lockKey, lockValue, ttlSeconds);
        } else {
            log.warn("--> [REDIS LOCK BUSY] Key='{}' is already locked by another transaction!", lockKey);
        }
        return acquired;
    }

    /**
     * Giải phóng Distributed Lock sau khi đã thực hiện xong giao dịch
     */
    public void releaseLock(String lockKey, String lockValue) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
            log.info("--> [REDIS LOCK RELEASED] Key='{}'", lockKey);
        }
    }
}
