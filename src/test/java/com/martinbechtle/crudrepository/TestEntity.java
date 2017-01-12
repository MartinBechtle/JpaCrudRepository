package com.martinbechtle.crudrepository;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity used for integration testing
 * @author Martin Bechtle
 */
@Entity
public class TestEntity {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String content;

    public TestEntity(String content) {

        this.content = content;
    }

    public TestEntity() {
    }

    public Long getId() {
        return id;
    }

    public TestEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getContent() {
        return content;
    }

    public TestEntity setContent(String content) {
        this.content = content;
        return this;
    }
}
