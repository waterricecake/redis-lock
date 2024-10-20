package org.example.redislock.service;

import lombok.RequiredArgsConstructor;
import org.example.redislock.domain.Person;
import org.example.redislock.domain.PersonRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public Person getPersonById(Long id) {
        RLock lock = getLock(String.valueOf(id));
        Person person = personRepository.findById(id).orElse(null);
        lock.unlock();
        return person;
    }

    @Transactional
    public Person savePerson(Person person) {
        RLock lock = getLock(String.valueOf(person.getId()));
        Person savedPerson = personRepository.save(person);
        lock.unlock();
        return savedPerson;
    }

    public void waitAndUpdateById(long id, int age) throws InterruptedException {
        RLock lock = getLock(String.valueOf(id));
        Thread.sleep(100);
        Person person = personRepository.findById(id).orElse(null);
        person.setAge(age);
        personRepository.save(person);
        lock.unlock();
    }

    private RLock getLock(String key) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked;
        try {
            isLocked = lock.tryLock(15, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("락을 획득하는 중 오류 발생", e);
        }

        if (!isLocked) {
            throw new IllegalStateException("락을 획득할 수 없습니다: " + key);
        }
        return lock;
    }
}