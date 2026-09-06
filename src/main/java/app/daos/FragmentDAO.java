package app.daos;

import app.entities.Fragment;
import jakarta.persistence.EntityManagerFactory;

public class FragmentDAO extends UserOwnedDAO <Fragment, Integer> {

    public FragmentDAO(EntityManagerFactory emf) {
        super(emf, Fragment.class);
    }







}
