package io.github.querysculptor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;

class JpaComposition {

  interface Combiner extends Serializable {
    Predicate combine(CriteriaBuilder builder, Predicate lhs, Predicate rhs);
  }

  static <T> QuerySculptor<T> composed(QuerySculptor<T> lhs, QuerySculptor<T> rhs, Combiner combiner) {

    return (root, query, builder) -> {
      Predicate thisPredicate = toPredicate(lhs, root, query, builder);
      Predicate otherPredicate = toPredicate(rhs, root, query, builder);

      if (thisPredicate == null) {
        return otherPredicate;
      }

      return otherPredicate == null
          ? thisPredicate
          : combiner.combine(builder, thisPredicate, otherPredicate);
    };
  }

  private static <T> Predicate toPredicate(
          QuerySculptor<T> specification, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return specification == null ? null : specification.carveCondition(root, query, builder);
  }
}
