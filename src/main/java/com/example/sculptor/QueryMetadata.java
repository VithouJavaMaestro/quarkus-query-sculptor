package com.example.sculptor;

import jakarta.persistence.criteria.Root;

interface QueryMetadata<T> {
    Root<T> getRoot();
}
