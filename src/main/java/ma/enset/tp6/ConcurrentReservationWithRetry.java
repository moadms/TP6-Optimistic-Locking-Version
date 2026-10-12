package ma.enset.tp6;

import ma.enset.tp6.model.SalleConcurrente;
import ma.enset.tp6.service.ReservationRetryHandler;
import ma.enset.tp6.service.ReservationService;
import ma.enset.tp6.service.ReservationServiceImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ConcurrentReservationWithRetry {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("    DEMONSTRATION RETRY HANDLER CONCURRENCE & @VERSION OPTIMISTIC LOCKING       ");
        System.out.println("================================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tp6_concurrence_locking");
        try {
            ReservationService service = new ReservationServiceImpl(emf);
            SalleConcurrente salle = service.createSalle("Salle Polyvalente B12");
            System.out.println("Salle initiale creee : ID=" + salle.getId() + ", Version=" + salle.getVersion());

            ReservationRetryHandler retryHandler = new ReservationRetryHandler(emf, 3);
            SalleConcurrente updated = retryHandler.reserverAvecRetry(salle.getId(), "Moad Moussaoui");

            System.out.println("Statut final : " + updated.getStatutOccupation() + " par " + updated.getDernierDemandeur() + ", Version=" + updated.getVersion());
            System.out.println("================================================================================");
            System.out.println("                   TEST DU RETRY HANDLER REUSSI A 100%                          ");
            System.out.println("================================================================================");
        } finally {
            emf.close();
        }
    }
}
