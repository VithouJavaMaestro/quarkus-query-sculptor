package io.github.querysculptor;

import jakarta.persistence.criteria.Root;

interface QueryMetadata<T> {
    Root<T> getRoot();
}
