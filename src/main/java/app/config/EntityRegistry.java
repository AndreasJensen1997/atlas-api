package app.config;

import app.entities.Chapter;
import app.entities.AppUser;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {
    }

    static void registerEntities(Configuration configuration) {

        configuration.addAnnotatedClass(AppUser.class);
        configuration.addAnnotatedClass(Chapter.class);


    }
}