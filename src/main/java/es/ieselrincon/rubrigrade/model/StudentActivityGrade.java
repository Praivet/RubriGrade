package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "student_activity_grades",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "activity_id"})
)
public class StudentActivityGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Muchas notas pertenecen a un alumno
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Muchas notas pertenecen a una actividad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "evaluated_at", updatable = false)
    private LocalDateTime evaluatedAt;

    // Una nota se desglosa en muchas puntuaciones (una por criterio)
    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCriterionScore> criterionScores = new ArrayList<>();

    // ===== Constructores =====
    public StudentActivityGrade() {
    }

    public StudentActivityGrade(Student student, Activity activity) {
        this.student = student;
        this.activity = activity;
    }

    // ===== Getters y Setters =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getFinalGrade() { return finalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public List<StudentCriterionScore> getCriterionScores() { return criterionScores; }
    public void setCriterionScores(List<StudentCriterionScore> criterionScores) {
        this.criterionScores = criterionScores;
    }

    @Override
    public String toString() {
        return "Nota " + finalGrade + "/10";
    }
}
