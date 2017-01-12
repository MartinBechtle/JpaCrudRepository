package com.martinbechtle.crudrepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Leverages spring boot to easily spin up a JPA environment with Hibernate as provider
 * @author Martin Bechtle
 */
@SpringBootTest(classes = {JpaCrudRepositoryIntegrationTestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class JpaCrudRepositoryIntegrationTest {

    private JpaCrudRepository<TestEntity, Long> jpaCrudRepository; // actual repository under test

    final String CONTENT = "CONTENT";

    @Inject
    EntityManager entityManager; // shared, managed entity manager by spring data

    @Inject
    TestEntityRepository testEntityRepository; // useful spring data jpa repository to do db queries without using the JpaCrudRepository methods when testing one of those methods

    @Before
    public void setUp() {

        testEntityRepository.deleteAll();
        jpaCrudRepository = new JpaCrudRepository<>(entityManager, TestEntity.class);
    }

    @Test
    public void save_ShouldPersistEntity() {


        TestEntity testEntity = new TestEntity(CONTENT);
        assertEquals(0, testEntityRepository.findAll().size());

        jpaCrudRepository.save(testEntity);
        entityManager.flush(); // not really necessary since default setup is autoflush for hibernate, but still formally correct to make the test independent from such config
        assertEquals(1, testEntityRepository.findAll().size());
    }

    @Test
    public void saveAndFlush_ShouldPersistEntity_AndReturnEntityWithGeneratedId() {

        TestEntity testEntity = new TestEntity(CONTENT);
        assertNull(testEntity.getId());

        TestEntity savedTestEntity = jpaCrudRepository.saveAndFlush(testEntity);
        assertNotNull(savedTestEntity.getId());
        assertEquals(CONTENT, savedTestEntity.getContent());
    }

    @Test
    public void save_ShouldPersistAllEntitiesInCorrectOrder_WhenIterableIsArgument() {

        List<TestEntity> testEntities = Arrays.asList(
                new TestEntity("test1"), new TestEntity("test2"), new TestEntity("test3"));

        List<TestEntity> persistedEntities = jpaCrudRepository.save(testEntities);
        assertEquals(3, persistedEntities.size());
        entityManager.flush();

        List<TestEntity> foundEntities = testEntityRepository.findAll();
        assertEquals(3, foundEntities.size());
        assertEquals("test1", foundEntities.get(0).getContent());
        assertEquals("test2", foundEntities.get(1).getContent());
        assertEquals("test3", foundEntities.get(2).getContent());
    }

    @Test
    public void findOne_ShouldReturnEmptyOptional_WhenNoRecordFoundWithSpecifiedId() {

        assertFalse(jpaCrudRepository.findOne(1L).isPresent());
    }

    @Test
    public void findOne_ShouldReturnOptionalWithValue_WhenRecordFoundWithSpecifiedId() {

        TestEntity testEntity = testEntityRepository.save(new TestEntity("test"));
        assertTrue(jpaCrudRepository.findOne(testEntity.getId()).isPresent());
    }

    @Test
    public void requireOne_ShouldReturnEntity_WhenRecordFoundWithSpecifiedId() {

        TestEntity testEntity = testEntityRepository.save(new TestEntity("test"));
        TestEntity foundEntity = jpaCrudRepository.requireOne(testEntity.getId());
        assertEquals(testEntity.getId(), foundEntity.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void requireOne_ShoulThrowException_WhenRecordNotFoundWithSpecifiedId() {

        jpaCrudRepository.requireOne(100L);
    }

    @Test
    public void exists_ShouldReturnFalse_WhenNoRecordFoundWithSpecifiedId() {

        assertFalse(jpaCrudRepository.exists(1L));
    }

    @Test
    public void exists_ShouldReturnTrue_WhenRecordFoundWithSpecifiedId() {

        TestEntity testEntity = testEntityRepository.save(new TestEntity("test"));
        assertTrue(jpaCrudRepository.exists(testEntity.getId()));
    }

    @Test
    public void findAll_ShouldReturnEmptyList_WhenNoRecordsInTable() {

        assertEquals(0, jpaCrudRepository.findAll().size());
    }

    @Test
    public void findAll_ShouldReturnAllRecordsInInsertionOrder_WhenRecordsInTable() {

        List<TestEntity> testEntities = Arrays.asList(
                new TestEntity("test1"), new TestEntity("test2"), new TestEntity("test3"));

        testEntityRepository.save(testEntities);
        entityManager.flush();

        List<TestEntity> foundEntities = jpaCrudRepository.findAll();
        assertEquals(3, foundEntities.size());
        assertEquals("test1", foundEntities.get(0).getContent());
        assertEquals("test2", foundEntities.get(1).getContent());
        assertEquals("test3", foundEntities.get(2).getContent());
    }

    @Test
    public void delete_ShouldDoNothing_WhenRecordDoesNotExist() {

        testEntityRepository.saveAndFlush(new TestEntity("test1"));
        assertEquals(1, testEntityRepository.count());

        jpaCrudRepository.delete(new TestEntity("test1"));
        jpaCrudRepository.delete(Arrays.asList(
                new TestEntity("test1"), new TestEntity("test2"), new TestEntity("test3")));

        assertEquals(1, testEntityRepository.count());
    }

    @Test
    public void delete_ShouldDeleteRecord_WhenRecordExists() {

        TestEntity testEntity = testEntityRepository.saveAndFlush(new TestEntity("test1"));
        assertEquals(1, testEntityRepository.count());

        jpaCrudRepository.delete(testEntity);
        entityManager.flush();
        assertEquals(0, testEntityRepository.count());
    }

    @Test
    public void deleteAll_ShouldDeleteAllRecordsFromTable() {

        List<TestEntity> testEntities = Arrays.asList(
                new TestEntity("test1"), new TestEntity("test2"), new TestEntity("test3"));

        testEntityRepository.save(testEntities);
        entityManager.flush();

        List<TestEntity> foundEntities = testEntityRepository.findAll();
        assertEquals(3, foundEntities.size());

        jpaCrudRepository.deleteAll();
        jpaCrudRepository.flush(); // indirectly tests the flush method
        assertEquals(0, testEntityRepository.count());
    }

}