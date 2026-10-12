package ma.enset.tp6;

import ma.enset.tp6.model.SalleConcurrente;
import ma.enset.tp6.service.ReservationRetryHandler;
import ma.enset.tp6.service.ReservationService;
import ma.enset.tp6.service.ReservationServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

public class OptimisticLockingTest {
    private static EntityManagerFactory emf;
    private static ReservationService reservationService;

    @BeforeAll
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("tp6_concurrence_locking");
        reservationService = new ReservationServiceImpl(emf);
    }

    @AfterAll
    public static void tearDown() {
        if (emf != null && emf.isOpen()) emf.close();
    }

    @Test
    public void testVersionIncrementOnUpdate() {
        SalleConcurrente salle = reservationService.createSalle("Salle Conf A");
        assertNotNull(salle.getId());
        assertEquals(0L, salle.getVersion());

        SalleConcurrente updated = reservationService.updateSalleStatut(salle.getId(), "RESERVEE", "User1");
        assertEquals(1L, updated.getVersion());
    }

    @Test
    public void testOptimisticLockExceptionSimulation() {
        SalleConcurrente salle = reservationService.createSalle("Salle Conflit");
        Long id = salle.getId();

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        try {
            em1.getTransaction().begin();
            SalleConcurrente s1 = em1.find(SalleConcurrente.class, id);

            em2.getTransaction().begin();
            SalleConcurrente s2 = em2.find(SalleConcurrente.class, id);

            // User 1 commits first
            s1.setStatutOccupation("RESERVE_PAR_USER_1");
            em1.getTransaction().commit();

            // User 2 tries to commit with stale version -> RollbackException wrapping OptimisticLockException
            s2.setStatutOccupation("RESERVE_PAR_USER_2");
            Exception thrown = assertThrows(Exception.class, () -> {
                em2.getTransaction().commit();
            });
            assertTrue(thrown instanceof javax.persistence.OptimisticLockException || 
                       thrown instanceof javax.persistence.RollbackException,
                       "Expected OptimisticLockException or RollbackException");
        } finally {
            if (em1.isOpen()) em1.close();
            if (em2.isOpen()) em2.close();
        }
    }

    @Test
    public void testRetryHandlerSuccess() {
        SalleConcurrente salle = reservationService.createSalle("Salle AutoRetry");
        ReservationRetryHandler handler = new ReservationRetryHandler(emf, 3);
        SalleConcurrente updated = handler.reserverAvecRetry(salle.getId(), "Moad Moussaoui");
        assertNotNull(updated);
        assertEquals("RESERVEE", updated.getStatutOccupation());
    }
}
