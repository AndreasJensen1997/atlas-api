package app.daos;

import app.entities.Memory;
import jakarta.persistence.EntityManagerFactory;

public class MemoryDAO extends AbstractDAO <Memory, Integer> {

    public MemoryDAO(EntityManagerFactory emf) {
        super(emf, Memory.class);
    }


}
