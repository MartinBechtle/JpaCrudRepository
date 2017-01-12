package com.martinbechtle.crudrepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by martin on 05/01/2017.
 * @author Martin Bechtle
 */
@Repository
public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}
