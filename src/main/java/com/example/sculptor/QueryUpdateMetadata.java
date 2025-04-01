package com.example.sculptor;

import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;

class QueryUpdateMetadata<T> extends AbstractQueryMetadata<T> {

    private final CriteriaUpdate<T> criteriaUpdate;

    public QueryUpdateMetadata(Root<T> root, CriteriaUpdate<T> criteriaUpdate) {
        super(root);
        this.criteriaUpdate = criteriaUpdate;
    }

    public CriteriaUpdate<T> getCriteriaUpdate() {
        return criteriaUpdate;
    }
}
