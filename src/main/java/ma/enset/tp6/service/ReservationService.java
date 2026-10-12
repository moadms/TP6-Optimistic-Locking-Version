package ma.enset.tp6.service;

import ma.enset.tp6.model.SalleConcurrente;

public interface ReservationService {
    SalleConcurrente createSalle(String nomEspace);
    SalleConcurrente updateSalleStatut(Long id, String statutOccupation, String demandeur);
    SalleConcurrente findById(Long id);
}
