package io.github.querysculptor;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.persistence.criteria.*;
import org.hibernate.SessionFactory;
import org.hibernate.query.Order;
import org.hibernate.query.Page;
import org.hibernate.query.Query;
import org.hibernate.query.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Decorator
@Priority(0)
public class SimpleQuerySculptorExecutor<ENTITY> implements QuerySculptorExecutor<ENTITY> {

    private final QuerySculptorExecutor<ENTITY> delegate;

    private final QueryProvider<ENTITY> queryProvider;

    public SimpleQuerySculptorExecutor(
            @Delegate @Any QuerySculptorExecutor<ENTITY> delegate) {
        this.delegate = delegate;
        queryProvider = new QueryProvider<>(this);
    }

    @Override
    public List<ENTITY> list(QuerySculptor<ENTITY> querySculptor) {
        Assert.notNull(querySculptor, "querySculptor must be not null");

        return getSessionFactory().fromTransaction(session -> session.createQuery(getCriteriaQuery(querySculptor, queryProvider.selectQuery())).getResultList());
    }

    @Override
    public <R> R findAll(QuerySculptor<ENTITY> querySculptor, Function<Query<ENTITY>, R> callback) {
        return findAll(querySculptor, new PageRequest(Paging.unPaged()), callback);
    }

    @Override
    public <R> R findAll(QuerySculptor<ENTITY> querySculptor, PageRequest pageRequest, Function<Query<ENTITY>, R> callback) {
        Assert.notNull(querySculptor, "querySculptor must be not null");
        Assert.notNull(pageRequest, "pageRequest must be not null");
        Assert.notNull(callback, "callback cannot be null");

        return getSessionFactory().fromSession(session -> {

            Paging requestPaging = pageRequest.getPage();
            org.hibernate.query.Query<ENTITY> selectionQuery = session.createQuery(getCriteriaQuery(querySculptor, queryProvider.selectQuery()));

            if (!requestPaging.isUnPaged()) {
                Page paging = Page.page(requestPaging.getSize(), requestPaging.getIndex());
                Sort sort = pageRequest.getSort();
                List<Order<? super ENTITY>> orders = new ArrayList<>();
                for (Sort.Column column : sort.getColumns()) {
                    SortDirection direction = SortDirection.valueOf(column.getDirection().name());
                    orders.add(Order.by(entityClass(), column.getName(), direction));
                }
                selectionQuery = selectionQuery.setPage(paging).setOrder(orders);
            }

            return callback.apply(selectionQuery);
        });
    }

    @Override
    public boolean exists(QuerySculptor<ENTITY> querySculptor) {
        Assert.notNull(querySculptor);

        return getSessionFactory().fromSession(session -> {

            CriteriaQuery<ENTITY> criteriaQuery = getCriteriaQuery(querySculptor, queryProvider.selectQuery());
            Query<ENTITY> query = session.createQuery(criteriaQuery);
            query.setMaxResults(1);

            return (query.uniqueResult() != null);
        });
    }

    @Override
    public int delete(QuerySculptor<ENTITY> querySculptor) {
        Assert.notNull(querySculptor);

        return getSessionFactory().fromTransaction(session -> {
            CriteriaDelete<ENTITY> criteriaDelete = createCriteriaDelete(querySculptor, queryProvider.deleteQuery());
            return session.createMutationQuery(criteriaDelete).executeUpdate();
        });
    }

    @Override
    public int update(QuerySculptor<ENTITY> querySculptor, Consumer<CriteriaUpdate<ENTITY>> callback) {
        Assert.notNull(querySculptor);

        return getSessionFactory().fromTransaction(session -> {
            CriteriaUpdate<ENTITY> criteriaUpdate = createCriteriaUpdate(querySculptor, queryProvider.updateQuery());
            if (callback != null) {
                callback.accept(criteriaUpdate);
            }

            return session.createMutationQuery(criteriaUpdate).executeUpdate();
        });
    }

    @Override
    public ENTITY findOne(QuerySculptor<ENTITY> querySculptor) {
        Assert.notNull(querySculptor);

        return getSessionFactory().fromTransaction(session -> {
            CriteriaQuery<ENTITY> criteriaQuery = getCriteriaQuery(querySculptor, queryProvider.selectQuery());
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

        return (predicate != null ? criteriaDelete.where(predicate) : criteriaDelete);
    }

    private CriteriaQuery<ENTITY> getCriteriaQuery(QuerySculptor<ENTITY> querySculptor, QueryMetadata<ENTITY> metadata) {
        Root<ENTITY> root = metadata.getRoot();

        CriteriaQuery<ENTITY> criteriaQuery = ((QuerySelectionMetadata<ENTITY>) metadata).getQuery();
        Predicate predicate = querySculptor.carveCondition(root, criteriaQuery, getCriteriaBuilder());

        return (predicate != null ? criteriaQuery.where(predicate) : criteriaQuery);
    }

    public SessionFactory getSessionFactory() {
        return Panache.getSession().getSessionFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return getSessionFactory().getCriteriaBuilder();
    }

    @Override
    public Class<ENTITY> entityClass() {
        //This need to improve without providing entity class
        Class<ENTITY> entityClass = delegate.entityClass();
        Assert.notNull(entityClass, "entity class cannot be null");
        return entityClass;
    }
}