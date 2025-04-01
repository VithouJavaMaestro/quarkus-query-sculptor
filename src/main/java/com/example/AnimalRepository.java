package com.example;

import com.example.sculptor.QuerySculptorExecutor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnimalRepository implements QuerySculptorExecutor<Animal>, PanacheRepository<Animal> {
    @Override
    public Class<Animal> entityClass() {
        return Animal.class;
    }
}
