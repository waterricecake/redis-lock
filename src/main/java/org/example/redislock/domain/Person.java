package org.example.redislock.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("person")
@Getter
@Setter
@AllArgsConstructor
public class Person {

    @Id
    private Long id;
    private String name;
    private int age;
}
