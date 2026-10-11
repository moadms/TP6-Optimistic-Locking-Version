package ma.enset.tp6;

import ma.enset.tp6.model.SalleConcurrente;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;

public class App {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(">>> DEMARRAGE TP6 - SIMULATION CONFLIT CONCURRENT ET OPTIMISTIC LOCKING <<<");
        System.out.println("==========================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tp6_concurrence_locking");

        // [1] Initialisation de la salle dans la base
        EntityManager emInit = emf.createEntityManager();
        emInit.getTransaction().begin();

        SalleConcurrente salle = new SalleConcurrente("Amphithéâtre Turing (Salle 101)");
        emInit.persist(salle);
        emInit.getTransaction().commit();
        Long salleId = salle.getId();
        emInit.close();

        System.out.println("\n--- [1] INITIALISATION ---");
        System.out.println("Salle créée avec succès | ID: " + salleId + " | Version initiale: " + salle.getVersion());

        // [2] Simulation de deux sessions concurrentes (Utilisateur 1 et Utilisateur 2)
        System.out.println("\n--- [2] SIMULATION DE DEUX LECTURES CONCURRENTES ---");
        EntityManager emClient1 = emf.createEntityManager();
        EntityManager emClient2 = emf.createEntityManager();

        emClient1.getTransaction().begin();
        emClient2.getTransaction().begin();

        // Les deux clients lisent la même salle au même moment (même version initiale = 0)
        SalleConcurrente salleVueParClient1 = emClient1.find(SalleConcurrente.class, salleId);
        SalleConcurrente salleVueParClient2 = emClient2.find(SalleConcurrente.class, salleId);

        System.out.println("Client 1 a lu la salle : " + salleVueParClient1.getNomEspace() + " [Version: " + salleVueParClient1.getVersion() + "]");
        System.out.println("Client 2 a lu la salle : " + salleVueParClient2.getNomEspace() + " [Version: " + salleVueParClient2.getVersion() + "]");

        // [3] Le Client 1 modifie et valide sa réservation en premier
        System.out.println("\n--- [3] CLIENT 1 VALIDE SA RESERVATION EN PREMIER ---");
        salleVueParClient1.setStatutOccupation("RESERVEE_PAR_CLIENT_1");
        salleVueParClient1.setDernierDemandeur("Moad Moussaoui");
        emClient1.getTransaction().commit(); // La version en base passe de 0 à 1 !
        System.out.println("✓ Client 1 a validé son commit avec succès !");
        System.out.println("  Nouvelle version enregistrée en base : " + salleVueParClient1.getVersion());
        emClient1.close();

        // [4] Le Client 2 tente de valider sa réservation avec son ancienne version (0)
        System.out.println("\n--- [4] CLIENT 2 TENTE DE VALIDER (CONFLIT DETECTE) ---");
        salleVueParClient2.setStatutOccupation("RESERVEE_PAR_CLIENT_2");
        salleVueParClient2.setDernierDemandeur("Autre Utilisateur");

        try {
            emClient2.getTransaction().commit();
            System.out.println("Erreur: Le commit a réussi alors qu'il devait échouer !");
        } catch (RollbackException | OptimisticLockException e) {
            System.out.println("✓ SUCCES DE LA DETECTION DU CONFLIT !");
            System.out.println("Exception interceptée : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.out.println("Cause racine : " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            System.out.println(">>> L'Optimistic Locking avec @Version a bien protégé les données contre la perte de mise à jour (Lost Update).");
            if (emClient2.getTransaction().isActive()) {
                emClient2.getTransaction().rollback();
            }
        } finally {
            emClient2.close();
        }

        // [5] Vérification finale de l'état de la ressource
        EntityManager emVerif = emf.createEntityManager();
        SalleConcurrente salleFinale = emVerif.find(SalleConcurrente.class, salleId);
        System.out.println("\n--- [5] ETAT FINAL DE LA SALLE EN BASE ---");
        System.out.println("Statut effectif : " + salleFinale.getStatutOccupation());
        System.out.println("Réservé par     : " + salleFinale.getDernierDemandeur());
        System.out.println("Version finale  : " + salleFinale.getVersion());
        emVerif.close();

        emf.close();
        System.out.println("\n==========================================================================");
        System.out.println(">>> DEMONSTRATION TP6 TERMINEE AVEC SUCCES <<<");
        System.out.println("==========================================================================");
    }
}
