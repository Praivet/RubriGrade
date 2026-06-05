package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rubrics")
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score", nullable = false)
    private Double maxScore = 10.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Una rúbrica está formada por varios criterios
    @OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RubricCriterion> criteria = new ArrayList<>();

    // Una rúbrica puede usarse en varias actividades
    @OneToMany(mappedBy = "rubric")
    private List<Activity> activities = new ArrayList<>();

    // ===== Constructores =====
    public Rubric() {
    }

    public Rubric(String name, String description, Double maxScore) {
        this.name = name;
        this.description = description;
        this.maxScore = maxScore;
    }

    // ===== Getters y Setters =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<RubricCriterion> getCriteria() { return criteria; }
    public void setCriteria(List<RubricCriterion> criteria) { this.criteria = criteria; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }

    @Override
    public String toString() {
        return name;
    }
}
