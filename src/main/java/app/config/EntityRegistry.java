package app.config;

import app.entities.*;
import org.hibernate.cfg.Configuration;


final class EntityRegistry {

    private EntityRegistry() {
    }

    static void registerEntities(Configuration configuration) {

        configuration.addAnnotatedClass(AppUser.class);
        configuration.addAnnotatedClass(Artifact.class);
        configuration.addAnnotatedClass(Chapter.class);
        configuration.addAnnotatedClass(Fragment.class);
        configuration.addAnnotatedClass(List.class);
        configuration.addAnnotatedClass(Memory.class);
        configuration.addAnnotatedClass(Mention.class);
        configuration.addAnnotatedClass(Person.class);
        configuration.addAnnotatedClass(Place.class);
        configuration.addAnnotatedClass(Story.class);
        configuration.addAnnotatedClass(TimeCapsule.class);


    }
}