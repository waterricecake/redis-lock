package org.example.redislock.service;

import lombok.RequiredArgsConstructor;
import org.example.redislock.domain.Person;
import org.example.redislock.domain.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional
    public Person getPersonById(Long id) {
        return personRepository.findById(id).orElse(null);
    }

    @Transactional
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    public void waitAndUpdateById(long id, int age) throws InterruptedException {
        Thread.sleep(100);
        Person person = personRepository.findById(id).orElse(null);
        person.setAge(age);
        personRepository.save(person);
    }
}