package app.daos;

import app.entities.Chapter;
import app.entities.Memory;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryDAO extends UserOwnedDAO <Memory, Integer> {

    public MemoryDAO(EntityManagerFactory emf) {
        super(emf, Memory.class);
    }





}
