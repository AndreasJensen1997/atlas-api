package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Memory;
import jakarta.persistence.EntityManagerFactory;

public class MemoryDAO extends UserOwnedDAO<Memory, Integer> {

    public MemoryDAO(EntityManagerFactory emf) {
        super(emf, Memory.class);
    }




}
