package io.github.querysculptor;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.*;
import org.hibernate.SessionFactory;
import org.hibernate.query.Order;
import org.hibernate.query.Page;
import org.hibernate.query.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Decorator
@Priority(0)
public class SimpleQuerySculptorExecutor<ENTITY> implements QuerySculptorExecutor<ENTITY> {

    private final QuerySculptorExecutor<ENTITY> delegate;
    private final SessionFactory sessionFactory;
    private final Query<ENTITY> query;

    public SimpleQuerySculptorExecutor(
            @Delegate @Any QuerySculptorExecutor<ENTITY> delegate, SessionFactory sessionFactory) {
        this.delegate = delegate;
        this.sessionFactory = sessionFactory;
        query = new Query<>(this);
    }

    @Override
    public List<ENTITY> list(QuerySculptor<ENTITY> querySculptor) {
        return sessionFactory.fromTransaction(session -> session.createQuery(getCriteriaQuery(querySculptor, query.selectQuery())).getResultList());
    }

    @Override
    public void findAll(QuerySculptor<ENTITY> querySculptor, PageRequest pageRequest, SelectionQueryCallback<ENTITY> executor) {
        sessionFactory.inSession(session -> {

            Paging requestPaging = pageRequest.getPage();
            org.hibernate.query.Query<ENTITY> selectionQuery;

            if (requestPaging.isUnPaged()) {
                selectionQuery =
                        session.createQuery(getCriteriaQuery(querySculptor, query.selectQuery()));
            } else {
                Page paging = Page.page(requestPaging.getSize(), requestPaging.getIndex());
                Sort sort = pageRequest.getSort();
                List<Order<? super ENTITY>> orders = new ArrayList<>();
                for (Sort.Column column : sort.getColumns()) {
                    SortDirection direction = SortDirection.valueOf(column.getDirection().name());
                    orders.add(Order.by(entityClass(), column.getName(), direction));
                }

                selectionQuery =
                        session
                                .createQuery(getCriteriaQuery(querySculptor, query.selectQuery()))
                                .setPage(paging)
                                .setOrder(orders);
            }

            executor.execute(selectionQuery);
        });
    }

    @Override
    public void findAll(QuerySculptor<ENTITY> querySculptor, SelectionQueryCallback<ENTITY> callback) {
        findAll(querySculptor, new PageRequest(Paging.unPaged()), callback);
    }

    @Override
    public boolean exists(QuerySculptor<ENTITY> querySculptor) {
        return sessionFactory.fromSession(session -> {
            CriteriaQuery<ENTITY> criteriaQuery = getCriteriaQuery(querySculptor, query.selectQuery());
            return session.createQuery(criteriaQuery).getResultCount() != 0;
        });
    }

    @Override
    public int delete(QuerySculptor<ENTITY> querySculptorQuery) {
        return sessionFactory.fromTransaction(session -> {
            CriteriaDelete<ENTITY> criteriaDelete = createCriteriaDelete(querySculptorQuery, query.deleteQuery());
            return session.createMutationQuery(criteriaDelete).executeUpdate();
        });
    }

    @Override
    public int update(QuerySculptor<ENTITY> querySculptor, Consumer<CriteriaUpdate<ENTITY>> callback) {
        return sessionFactory.fromTransaction(session -> {
            CriteriaUpdate<ENTITY> criteriaUpdate = createCriteriaUpdate(querySculptor, query.updateQuery());
            callback.accept(criteriaUpdate);
            return session.createMutationQuery(criteriaUpdate).executeUpdate();
        });
    }

    @Override
    public ENTITY findOne(QuerySculptor<ENTITY> querySculptor) {
        return sessionFactory.fromTransaction(session -> {
            CriteriaQuery<ENTITY> criteriaQuery = getCriteriaQuery(querySculptor, query.selectQuery());
            return session.createQuery(criteriaQuery).getSingleResult();
        });
    }

    private CriteriaUpdate<ENTITY> createCriteriaUpdate(QuerySculptor<ENTITY> jpaUpdateQuery, QueryMetadata<ENTITY> metadata) {
        Root<ENTITY> root = metadata.getRoot();
        CriteriaUpdate<ENTITY> criteriaUpdate =
                ((QueryUpdateMetadata<ENTITY>) metadata).getCriteriaUpdate();
        Predicate predicate =
                jpaUpdateQuery.carveCondition(root, createCriteriaQuery(), getCriteriaBuilder());
        return predicate != null ? criteriaUpdate.where(predicate) : criteriaUpdate;
    }

    private CriteriaDelete<ENTITY> createCriteriaDelete(QuerySculptor<ENTITY> jpaDeleteQuery, QueryMetadata<ENTITY> metadata) {
        Root<ENTITY> root = metadata.getRoot();
        CriteriaDelete<ENTITY> criteriaDelete =
                ((QueryDeletionMetadata<ENTITY>) metadata).getCriteriaDelete();
        Predicate predicate =
                jpaDeleteQuery.carveCondition(root, createCriteriaQuery(), getCriteriaBuilder());
        return predicate != null ? criteriaDelete.where(predicate) : criteriaDelete;
    }

    private CriteriaQuery<ENTITY> getCriteriaQuery(QuerySculptor<ENTITY> querySculptor, QueryMetadata<ENTITY> metadata) {
        Root<ENTITY> root = metadata.getRoot();
        CriteriaQuery<ENTITY> criteriaQuery = ((QuerySelectionMetadata<ENTITY>) metadata).getQuery();
        Predicate predicate = querySculptor.carveCondition(root, criteriaQuery, getCriteriaBuilder());
        return predicate != null ? criteriaQuery.where(predicate) : criteriaQuery;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return sessionFactory.getCriteriaBuilder();
    }

    @Override
    public Class<ENTITY> entityClass() {
        Class<ENTITY> entityClass = delegate.entityClass();
        if (entityClass == null) {
            throw new IllegalStateException("Entity class cannot be null");
        }
        if (!entityClass.isAnnotationPresent(Entity.class) || !entityClass.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException("Entity class must be annotated with @Entity and @Table");
        }
        return entityClass;
    }
}