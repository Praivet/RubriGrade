package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Entity esta clase representa una tabla
 * @Table es la tabla
 *
 * @Id es la clave primaria
 * @GeneratedValue  el id se genera automáticamente
 */

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    // Muchas actividades pertenecen a una asignatura
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Muchas actividades pueden usar la misma rúbrica
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubric_id", nullable = false)
    private Rubric rubric;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score", nullable = false)
    private Double maxScore = 10.0;

    @Column
    private Double weight = 100.0;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // Una actividad tiene muchas notas (una por cada alumno evaluado)
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentActivityGrade> grades = new ArrayList<>();

    //Constructores
    public Activity() {
    }

    public Activity(Subject subject, Rubric rubric, String name, Double maxScore, LocalDate dueDate) {
        this.subject = subject;
        this.rubric = rubric;
        this.name = name;
        this.maxScore = maxScore;
        this.dueDate = dueDate;
    }

    //Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Rubric getRubric() { return rubric; }
    public void setRubric(Rubric rubric) { this.rubric = rubric; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public List<StudentActivityGrade> getGrades() { return grades; }
    public void setGrades(List<StudentActivityGrade> grades) { this.grades = grades; }

    @Override
    public String toString() {
        return name;
    }
}
