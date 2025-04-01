package com.example.sculptor;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

class QuerySelectionMetadata<T> extends AbstractQueryMetadata<T> {

  private final CriteriaQuery<T> criteriaQuery;

  public QuerySelectionMetadata(Root<T> root, CriteriaQuery<T> criteriaQuery) {
    super(root);
    this.criteriaQuery = criteriaQuery;
  }

  public CriteriaQuery<T> getQuery() {
    return criteriaQuery;
  }
}
