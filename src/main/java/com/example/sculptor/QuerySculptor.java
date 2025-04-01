package com.example.sculptor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.StreamSupport;

/**
 * An interface for dynamically constructing JPA criteria queries. It provides utility methods to
 * build flexible query conditions using the Criteria API.
 *
 * @param <T> The entity type the query is built for.
 * @author Chanthavithou THEN
 */
public interface QuerySculptor<T> {

  /**
   * Returns a query sculptor that always evaluates to a conjunction (true condition).
   *
   * @param <T> The entity type.
   * @return A {@code QuerySculptor} representing a conjunction.
   */
  static <T> QuerySculptor<T> conjunction() {
    return (root, query, cb) -> cb.conjunction();
  }

  /**
   * Returns the given query sculptor if the provided value is not null, otherwise returns a
   * conjunction.
   *
   * @param <T> The entity type.
   * @param querySculptor The query sculptor to apply.
   * @param value The value to check.
   * @return The given query sculptor if {@code value} is not null; otherwise, a conjunction.
   */
  static <T> QuerySculptor<T> ifNotNull(QuerySculptor<T> querySculptor, Object value) {
    if (value == null) {
      return conjunction();
    }
    return querySculptor;
  }

  /**
   * Returns the given query sculptor if the provided string is not empty or null, otherwise returns
   * a conjunction.
   *
   * @param <T> The entity type.
   * @param querySculptor The query sculptor to apply.
   * @param value The string to check.
   * @return The given query sculptor if {@code value} is not empty or null; otherwise, a
   *     conjunction.
   */
  static <T> QuerySculptor<T> ifNotEmpty(QuerySculptor<T> querySculptor, String value) {
    if (value == null || value.isEmpty()) {
      return conjunction();
    }
    return querySculptor;
  }

  /**
   * Returns the given query sculptor if the provided collection is not empty or null, otherwise
   * returns a conjunction.
   *
   * @param <T> The entity type.
   * @param querySculptor The query sculptor to apply.
   * @param value The collection to check.
   * @return The given query sculptor if {@code value} is not empty or null; otherwise, a
   *     conjunction.
   */
  static <T> QuerySculptor<T> ifNotEmpty(QuerySculptor<T> querySculptor, Collection<?> value) {
    if (value == null || value.isEmpty()) {
      return conjunction();
    }
    return querySculptor;
  }

  /**
   * Returns the given query sculptor, or null if it is null.
   *
   * @param <T> The entity type.
   * @param querySculptor The query sculptor.
   * @return The given query sculptor or null.
   */
  static <T> QuerySculptor<T> where(QuerySculptor<T> querySculptor) {
    return querySculptor == null ? (root, query, cb) -> null : querySculptor;
  }

  /**
   * Combines this query sculptor with another using a logical AND.
   *
   * @param other The other query sculptor.
   * @return A new query sculptor combining both conditions with AND.
   */
  default QuerySculptor<T> and(QuerySculptor<T> other) {
    return JpaComposition.composed(this, other, CriteriaBuilder::and);
  }

  /**
   * Combines this query sculptor with another using a logical OR.
   *
   * @param other The other query sculptor.
   * @return A new query sculptor combining both conditions with OR.
   */
  default QuerySculptor<T> or(QuerySculptor<T> other) {
    return JpaComposition.composed(this, other, CriteriaBuilder::or);
  }

  /**
   * Negates the given query sculptor.
   *
   * @param <T> The entity type.
   * @param querySculptor The query sculptor to negate.
   * @return A new query sculptor representing NOT condition.
   */
  static <T> QuerySculptor<T> not(QuerySculptor<T> querySculptor) {
    if (querySculptor == null) {
      return ((root, query, cb) -> null);
    }
    return ((root, query, cb) -> cb.not(querySculptor.carveCondition(root, query, cb)));
  }

  /**
   * Combines multiple query sculptors using a logical AND.
   *
   * @param <T> The entity type.
   * @param jpaQueries The iterable collection of query sculptors.
   * @return A query sculptor combining all provided conditions with AND.
   */
  static <T> QuerySculptor<T> allOf(Iterable<QuerySculptor<T>> jpaQueries) {
    return StreamSupport.stream(jpaQueries.spliterator(), false)
        .reduce(QuerySculptor.where(null), QuerySculptor::and);
  }

  /**
   * Combines multiple query sculptors using a logical OR.
   *
   * @param <T> The entity type.
   * @param jpaQueries The iterable collection of query sculptors.
   * @return A query sculptor combining all provided conditions with OR.
   */
  static <T> QuerySculptor<T> anyOf(Iterable<QuerySculptor<T>> jpaQueries) {
    return StreamSupport.stream(jpaQueries.spliterator(), false)
        .reduce(QuerySculptor.where(null), QuerySculptor::or);
  }

  /**
   * Combines multiple query sculptors using a logical OR.
   *
   * @param <T> The entity type.
   * @param jpaQueries The query sculptors.
   * @return A query sculptor combining all provided conditions with OR.
   */
  @SafeVarargs
  static <T> QuerySculptor<T> anyOf(QuerySculptor<T>... jpaQueries) {
    return anyOf(Arrays.asList(jpaQueries));
  }

  /**
   * Defines how this query sculptor applies filtering conditions in a criteria query.
   *
   * @param root The root entity reference in the query.
   * @param query The criteria query instance.
   * @param cb The criteria builder.
   * @return A {@link Predicate} representing the query condition.
   */
  Predicate carveCondition(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
