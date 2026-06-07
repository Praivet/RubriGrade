package es.ieselrincon.rubrigrade.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "enrollment_number", unique = true, length = 20)
    private String enrollmentNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Un alumno puede tener muchas notas (en muchas actividades)

    /**
     * @oneToMany relacion 1:N
     * mappedBy = "student" la FK esta en otra tabla (student_activity_grades) en vez de crear otra tabla le dice a hibernate que
     * vaya a la otra tabla
     *
     * cascade = CascadeType.ALL si borro un alumno borro sus notas
     * orphanRemoval = true si una nota se queda sin alumno se borra
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentActivityGrade> grades = new ArrayList<>();

    //Constructores
    public Student() {
    }

    public Student(String firstName, String lastName, String email, String enrollmentNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enrollmentNumber = enrollmentNumber;
    }

    //Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<StudentActivityGrade> getGrades() { return grades; }
    public void setGrades(List<StudentActivityGrade> grades) { this.grades = grades; }

    @Override
    public String toString() {
        return id + " - " + firstName + " " + lastName;
    }
}
