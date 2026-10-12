package ma.enset.tp6.service;

import ma.enset.tp6.model.SalleConcurrente;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;

public class ReservationRetryHandler {
    private final EntityManagerFactory emf;
    private final int maxRetries;

    public ReservationRetryHandler(EntityManagerFactory emf, int maxRetries) {
        this.emf = emf;
        this.maxRetries = maxRetries;
    }

    public SalleConcurrente reserverAvecRetry(Long salleId, String demandeur) {
        int attempts = 0;
        while (attempts < maxRetries) {
            attempts++;
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SalleConcurrente salle = em.find(SalleConcurrente.class, salleId);
                if (salle == null) {
                    tx.rollback();
                    throw new IllegalArgumentException("Salle non trouvee: " + salleId);
                }
                salle.setStatutOccupation("RESERVEE");
                salle.setDernierDemandeur(demandeur + " (tentative " + attempts + ")");
                tx.commit();
                System.out.println(">>> Reservation reussie a la tentative " + attempts + " (Version: " + salle.getVersion() + ")");
                return salle;
            } catch (OptimisticLockException ole) {
                if (tx != null && tx.isActive()) tx.rollback();
                System.out.println(">>> Conflit d'acces concurrent detecte (Tentative " + attempts + "/" + maxRetries + "). Reessai...");
                try {
                    Thread.sleep(50 * attempts);
                } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw e;
            } finally {
                em.close();
            }
        }
        throw new RuntimeException("Echec de la reservation apres " + maxRetries + " tentatives suite a des conflits de version.");
    }
}
