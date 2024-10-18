package org.example.redislock.service;

import org.example.redislock.domain.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(RedisTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Test
    @DisplayName("레디스에서 읽기에 대한 락이 보장되지 않는다.")
    void failWithConcurrencyIssue() throws InterruptedException {
        // given
        final int expect = 12;
        final Person person = new Person(1L, "철수", 10);
        personService.savePerson(person);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        // when
        executorService.execute(
                () -> {
                    try {
                        personService.waitAndUpdateById(person.getId(), expect);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                    countDownLatch.countDown();
                    }
                }
        );

        executorService.execute(
                () -> {
                    person.setAge(11);
                    personService.savePerson(person);
                    countDownLatch.countDown();
                }
        );

        // then
        countDownLatch.await();
        final Person actual = personService.getPersonById(person.getId());
        assertThat(actual.getAge()).isEqualTo(expect);
    }
}