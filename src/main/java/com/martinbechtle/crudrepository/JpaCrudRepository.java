package com.martinbechtle.crudrepository;

import com.martinbechtle.jrequire.Require;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Allows to perform CRUD operations on a JPA entity
 * @param <T> the type of the entity
 * @param <I> the type of the entity's id
 */
public class JpaCrudRepository<T, I extends Serializable> {

    private final EntityManager entityManager;
    private final Class<T> clazz;

    private final TypedQuery<T> findAllQuery;
    private final Query deleteAllQuery;

    @SuppressWarnings("unchecked")
    public JpaCrudRepository(EntityManager entityManager, Class<T> entityClass) {

        this.entityManager = entityManager;
        this.clazz = entityClass;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = criteriaBuilder.createQuery(clazz);
        findAllQuery = entityManager.createQuery(
                cq.select(cq.from(clazz)));

        CriteriaDelete<T> delete = criteriaBuilder.createCriteriaDelete(clazz);
        delete.from(clazz);
        deleteAllQuery = entityManager.createQuery(delete);
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely. Changes are flushed only if the jpa provider's flush mode is set to AUTO.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    public <S extends T> S save(S entity) {

        Require.notNull(entity);
        return entityManager.merge(entity);
    }

    /**
     * Saves an entity and flushes changes instantly.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    public <S extends T> S saveAndFlush(S entity) {

        Require.notNull(entity);
        S savedEntity = entityManager.merge(entity);
        entityManager.flush();
        return savedEntity;
    }

    /**
     * Flush changes to the database (not only related to this entity)
     */
    public void flush() {

        entityManager.flush();
    }

    /**
     * Saves all given entities.
     *
     * @param entities the entities to save
     * @return the saved entities
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public <S extends T> List<S> save(Iterable<S> entities) {

        Require.notNull(entities);
        List<S> persistedEntities = new ArrayList<>();
        for (S entity : entities) {
            persistedEntities.add(save(entity)); // TODO would it be possible to do only one query?
        }
        return persistedEntities;
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or an empty {@link Optional} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    public Optional<T> findOne(I id) {

        Require.notNull(id);
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    /**
     * Retrieve an entity by its id and require it to be found.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id
     * @throws EntityNotFoundException if entity not found
     */
    public T requireOne(I id) throws EntityNotFoundException {

        Require.notNull(id);
        return findOne(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));
    }

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return true if an entity with the given id exists, {@literal false} otherwise
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    public boolean exists(I id) {

        Require.notNull(id);
        return findOne(id).isPresent();
    }

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    public List<T> findAll() {

        return findAllQuery.getResultList();
    }

    /**
     * Deletes a given entity.
     *
     * @param entity the entity to delete
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public void delete(T entity) {

        Require.notNull(entity);
        entityManager.remove(entity);
    }

    /**
     * Deletes the given entities.
     *
     * @param entities the entities to delete
     * @throws IllegalArgumentException in case the given {@link Iterable} is {@literal null}.
     */
    public void delete(Iterable<? extends T> entities) {

        Require.notNull(entities);
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     * Deletes all entities managed by the repository.
     */
    public void deleteAll() {

        deleteAllQuery.executeUpdate();
    }

}
