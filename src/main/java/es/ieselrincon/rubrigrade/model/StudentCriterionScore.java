package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "student_criterion_scores",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"student_activity_grade_id", "rubric_criterion_id"}
    )
)
public class StudentCriterionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Muchas puntuaciones pertenecen a una nota global
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_activity_grade_id", nullable = false)
    private StudentActivityGrade grade;

    // Muchas puntuaciones se refieren al mismo criterio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_criterion_id", nullable = false)
    private RubricCriterion criterion;

    @Column(nullable = false)
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // ===== Constructores =====
    public StudentCriterionScore() {
    }

    public StudentCriterionScore(StudentActivityGrade grade, RubricCriterion criterion, Double score) {
        this.grade = grade;
        this.criterion = criterion;
        this.score = score;
    }

    // ===== Getters y Setters =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public StudentActivityGrade getGrade() { return grade; }
    public void setGrade(StudentActivityGrade grade) { this.grade = grade; }

    public RubricCriterion getCriterion() { return criterion; }
    public void setCriterion(RubricCriterion criterion) { this.criterion = criterion; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return criterion.getName() + ": " + score;
    }
}
