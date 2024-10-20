package org.example.redislock.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UNLOCK_SCRIPT =
            """
            if redis.call("get",KEYS[1]) == ARGV[1] then
                return redis.call("del",KEYS[1])
            else
                return 0
            end
            """;

    public Boolean lock(String resource, String key, long timeout ) {
        return redisTemplate.opsForValue()
                .setIfAbsent(resource, key, Duration.ofMillis(timeout));
    }

    public Boolean unlock(String resource, String key) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(resource), key);
        return result != null && result == 1;
    }
}
