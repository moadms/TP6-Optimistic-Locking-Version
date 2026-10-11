package ma.enset.tp6.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_salles_concurrentes")
public class SalleConcurrente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salle")
    private Long id;

    @Column(name = "nom_espace", nullable = false)
    private String nomEspace;

    @Column(name = "statut_occupation")
    private String statutOccupation; // LIBRE, RESERVEE

    @Column(name = "dernier_demandeur")
    private String dernierDemandeur;

    // Champ de verrouillage optimiste JPA
    @Version
    @Column(name = "version_opt_lock")
    private Long version;

    public SalleConcurrente() {
        this.statutOccupation = "LIBRE";
    }

    public SalleConcurrente(String nomEspace) {
        this();
        this.nomEspace = nomEspace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomEspace() {
        return nomEspace;
    }

    public void setNomEspace(String nomEspace) {
        this.nomEspace = nomEspace;
    }

    public String getStatutOccupation() {
        return statutOccupation;
    }

    public void setStatutOccupation(String statutOccupation) {
        this.statutOccupation = statutOccupation;
    }

    public String getDernierDemandeur() {
        return dernierDemandeur;
    }

    public void setDernierDemandeur(String dernierDemandeur) {
        this.dernierDemandeur = dernierDemandeur;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SalleConcurrente{" +
                "id=" + id +
                ", nomEspace='" + nomEspace + '\'' +
                ", statutOccupation='" + statutOccupation + '\'' +
                ", dernierDemandeur='" + dernierDemandeur + '\'' +
                ", version=" + version +
                '}';
    }
}
