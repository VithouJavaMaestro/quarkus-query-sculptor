package io.github.querysculptor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.function.Consumer;

public interface QuerySculptorExecutor<ENTITY> {

    default List<ENTITY> list(QuerySculptor<ENTITY> querySculptor) {
        throw new UnsupportedOperationException("Unsupported list method");
    }

    default org.hibernate.query.Query<ENTITY> findAll(QuerySculptor<ENTITY> querySculptor, PageRequest pageRequest) {
        throw new UnsupportedOperationException("Unsupported findAll method");
    }

    default org.hibernate.query.Query<ENTITY> findAll(QuerySculptor<ENTITY> querySculptor) {
        throw new UnsupportedOperationException("Unsupported findAll method");
    }

    default ENTITY findOne(QuerySculptor<ENTITY> querySculptor) {
        throw new UnsupportedOperationException("Unsupported findOne method");
    }

    default int delete(QuerySculptor<ENTITY> querySculptorQuery) {
        throw new UnsupportedOperationException("Unsupported delete method");
    }

    default int update(QuerySculptor<ENTITY> querySculptorQuery, Consumer<CriteriaUpdate<ENTITY>> callback) {
        throw new UnsupportedOperationException("Unsupported update method");
    }

    default boolean exists(QuerySculptor<ENTITY> querySculptor) {
        throw new UnsupportedOperationException("Unsupported exists method");
    }

    default SessionFactory getSessionFactory() {
        throw new UnsupportedOperationException("Unsupported getSessionFactory method");
    }

    default CriteriaBuilder getCriteriaBuilder() {
        throw new UnsupportedOperationException("Unsupported getCriteriaBuilder method");
    }

    default CriteriaQuery<ENTITY> createCriteriaQuery() {
        return getCriteriaBuilder().createQuery(entityClass());
    }

    Class<ENTITY> entityClass();
}