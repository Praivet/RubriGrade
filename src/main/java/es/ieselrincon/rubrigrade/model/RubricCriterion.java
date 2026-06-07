package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rubric_criteria")
public class RubricCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Muchos criterios pertenecen a una rúbrica
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubric_id", nullable = false)
    private Rubric rubric;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score", nullable = false)
    private Double maxScore;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    //  Constructores
    public RubricCriterion() {
    }

    public RubricCriterion(Rubric rubric, String name, Double maxScore, Integer displayOrder) {
        this.rubric = rubric;
        this.name = name;
        this.maxScore = maxScore;
        this.displayOrder = displayOrder;
    }

    // ===== Getters y Setters =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Rubric getRubric() { return rubric; }
    public void setRubric(Rubric rubric) { this.rubric = rubric; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    @Override
    public String toString() {
        return name + " (max " + maxScore + ")";
    }
}
