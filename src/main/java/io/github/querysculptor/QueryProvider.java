package io.github.querysculptor;

import jakarta.persistence.criteria.*;

/**
 * @author Chanthavithou THEN
 */
class QueryProvider<ENTITY> {

  private final QuerySculptorExecutor<ENTITY> delegate;

  public QueryProvider(QuerySculptorExecutor<ENTITY> delegate) {
    this.delegate = delegate;
  }

  public QueryMetadata<ENTITY> deleteQuery() {
    return new DeleteQueryMetaDataBuilder<ENTITY>().buildQuery(delegate);
  }

  public QueryMetadata<ENTITY> selectQuery() {
    return new SelectQueryMetadataBuilder<ENTITY>().buildQuery(delegate);
  }

  public QueryMetadata<ENTITY> updateQuery() {
    return new UpdateQueryMetaDataBuilder<ENTITY>().buildQuery(delegate);
  }

  private static class DeleteQueryMetaDataBuilder<ENTITY>
      implements QueryMetadataBuilder<ENTITY> {
    @Override
    public QueryDeletionMetadata<ENTITY> buildQuery(QuerySculptorExecutor<ENTITY> executor) {
      Class<ENTITY> entityClass = executor.entityClass();
      CriteriaBuilder cb = executor.getCriteriaBuilder();
      CriteriaDelete<ENTITY> criteriaDelete = cb.createCriteriaDelete(entityClass);
      Root<ENTITY> root = criteriaDelete.from(entityClass);
      return new QueryDeletionMetadata<>(root, criteriaDelete);
    }
  }

  private static class UpdateQueryMetaDataBuilder<ENTITY>
      implements QueryMetadataBuilder<ENTITY> {
    @Override
    public QueryMetadata<ENTITY> buildQuery(QuerySculptorExecutor<ENTITY> executor) {
      Class<ENTITY> entityClass = executor.entityClass();
      CriteriaBuilder cb = executor.getCriteriaBuilder();
      CriteriaUpdate<ENTITY> updateQuery = cb.createCriteriaUpdate(entityClass);
      Root<ENTITY> root = updateQuery.from(entityClass);
      return new QueryUpdateMetadata<>(root, updateQuery);
    }
  }

  private static class SelectQueryMetadataBuilder<ENTITY>
      implements QueryMetadataBuilder<ENTITY> {
    @Override
    public QueryMetadata<ENTITY> buildQuery(QuerySculptorExecutor<ENTITY> executor) {
      CriteriaBuilder cb = executor.getCriteriaBuilder();
      CriteriaQuery<ENTITY> query = cb.createQuery(executor.entityClass());
      Root<ENTITY> root = query.from(executor.entityClass());
      return new QuerySelectionMetadata<>(root, query);
    }
  }
}
