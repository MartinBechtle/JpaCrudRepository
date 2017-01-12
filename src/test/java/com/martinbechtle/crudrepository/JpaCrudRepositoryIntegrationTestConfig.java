package com.martinbechtle.crudrepository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for {@link JpaCrudRepositoryIntegrationTest}
 *
 * <p>Relies on spring boot's autoconfiguration which will scan the classpath and notice the H2
 * database, thus configuring JPA with a connection pool pointing to an in memory database.</p>
 *
 * @author Martin Bechtle
 */
@EnableAutoConfiguration
public class JpaCrudRepositoryIntegrationTestConfig {

}
