package com.baeldung.namingstrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.hibernate.exception.SQLGrammarException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest(excludeAutoConfiguration = TestDatabaseAutoConfiguration.class)
@TestPropertySource("quoted-lower-case-naming-strategy.properties")
class QuotedLowerCaseNamingStrategyH2IntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void insertPeople() {
        personRepository.saveAll(Arrays.asList(
          new Person(1L, "John", "Doe"),
          new Person(2L, "Jane", "Doe"),
          new Person(3L, "Ted", "Mosby")
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"person", "PERSON", "Person"})
    void givenPeopleAndLowerCaseNamingStrategy_whenQueryPersonUnquoted_thenException(String tableName) {
        Query query = entityManager.createNativeQuery("select * from " + tableName);

        // Unexpected result
        assertThrows(SQLGrammarException.class, query::getResultStream);
    }

    @Test
    void givenPeopleAndLowerCaseNamingStrategy_whenQueryPersonQuotedUpperCase_thenException() {
        Query query = entityManager.createNativeQuery("select * from \"PERSON\"");

        // Expected result
        assertThrows(SQLGrammarException.class, query::getResultStream);
    }

    @Test
    void givenPeopleAndLowerCaseNamingStrategy_whenQueryPersonQuotedLowerCase_thenResult() {
        Query query = entityManager.createNativeQuery("select * from \"person\"");

        // Expected result
        List<Person> result = (List<Person>) query.getResultStream()
          .map(this::fromDatabase)
          .collect(Collectors.toList());

        assertThat(result).isNotEmpty();
    }

    public Person fromDatabase(Object databaseRow) {
        Object[] typedDatabaseRow = (Object[]) databaseRow;

        return new Person((Long) typedDatabaseRow[0],
          (String) typedDatabaseRow[1],
          (String) typedDatabaseRow[2]
        );
    }
}
