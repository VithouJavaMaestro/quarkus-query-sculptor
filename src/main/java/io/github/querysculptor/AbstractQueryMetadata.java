package io.github.querysculptor;

import jakarta.persistence.criteria.Root;

abstract class AbstractQueryMetadata<T> implements QueryMetadata<T> {

  private final Root<T> root;

  protected AbstractQueryMetadata(Root<T> root) {
    this.root = root;
  }

  @Override
  public Root<T> getRoot() {
    return root;
  }
}
