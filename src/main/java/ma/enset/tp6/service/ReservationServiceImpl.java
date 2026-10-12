package ma.enset.tp6.service;

import ma.enset.tp6.model.SalleConcurrente;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class ReservationServiceImpl implements ReservationService {
    private final EntityManagerFactory emf;

    public ReservationServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public SalleConcurrente createSalle(String nomEspace) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalleConcurrente salle = new SalleConcurrente(nomEspace);
            em.persist(salle);
            tx.commit();
            return salle;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public SalleConcurrente updateSalleStatut(Long id, String statutOccupation, String demandeur) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalleConcurrente salle = em.find(SalleConcurrente.class, id);
            if (salle != null) {
                salle.setStatutOccupation(statutOccupation);
                salle.setDernierDemandeur(demandeur);
            }
            tx.commit();
            return salle;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public SalleConcurrente findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(SalleConcurrente.class, id);
        } finally {
            em.close();
        }
    }
}
