package org.example.redislock.service;

import lombok.RequiredArgsConstructor;
import org.example.redislock.domain.Person;
import org.example.redislock.domain.PersonRepository;
import org.example.redislock.domain.RedisLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final RedisLockRepository redisLockRepository;

    @Transactional
    public Person getPersonById(Long id) throws InterruptedException {
        String key = getLock(String.valueOf(id));
        Person person = personRepository.findById(id).orElse(null);
        unlock(String.valueOf(id), key);
        return person;
    }

    @Transactional
    public Person savePerson(Person person) throws InterruptedException {
        String key = getLock(String.valueOf(person.getId()));
        Person savedPerson = personRepository.save(person);
        unlock(String.valueOf(person.getId()), key);
        return savedPerson;
    }

    @Transactional
    public void waitAndUpdateById(long id, int age) throws InterruptedException {
        String key = getLock(String.valueOf(id));
        Thread.sleep(100);
        Person person = personRepository.findById(id).orElse(null);
        person.setAge(age);
        personRepository.save(person);
        unlock(String.valueOf(id), key);
    }

    private String getLock(final String id) throws InterruptedException {
        String key = "random_value_" + id; // 실제로는 유니크한 랜덤값을 집어넣어야함
        while (Boolean.FALSE.equals(redisLockRepository.lock(id, key, 30_000L))) {
        }
        return key;
    }

    private void unlock(final String id, final String key) {
        redisLockRepository.unlock(id, key);
    }
}