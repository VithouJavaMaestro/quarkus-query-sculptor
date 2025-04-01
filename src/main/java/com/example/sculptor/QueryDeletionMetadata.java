package com.example.sculptor;

import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;

class QueryDeletionMetadata<T> extends AbstractQueryMetadata<T> {

    private final CriteriaDelete<T> criteriaDelete;

    public QueryDeletionMetadata(Root<T> root, CriteriaDelete<T> criteriaDelete) {
        super(root);
        this.criteriaDelete = criteriaDelete;
    }

    public CriteriaDelete<T> getCriteriaDelete() {
        return criteriaDelete;
    }
}
