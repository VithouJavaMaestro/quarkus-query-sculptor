package com.example;

import com.example.sculptor.Pagination;
import com.example.sculptor.QuerySculptor;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/animal")
public class AnimalResource {

    private final AnimalRepository animalRepository;

    public AnimalResource(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @POST
    @Transactional
    public Uni<Animal> createAnimal(@RestQuery String name) {
        return Uni.createFrom().emitter((emitter) -> {
            Animal animal = new Animal();
            animal.setName(name);
            animal.persist();
            emitter.complete(animal);
        });
    }

    @GET
    @Transactional
    public Pagination<Animal> get() {
        return animalRepository.findAll(QuerySculptor.conjunction());
    }
}
