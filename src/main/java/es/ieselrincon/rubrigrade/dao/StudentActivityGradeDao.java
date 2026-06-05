package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.StudentActivityGrade;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class StudentActivityGradeDao extends GenericDao<StudentActivityGrade> {

    public StudentActivityGradeDao() {
        super(StudentActivityGrade.class);
    }

    /**
     * Busca la nota de un alumno en una actividad concreta.
     * Sirve para saber si ya está evaluado o todavía no.
     */
    public StudentActivityGrade findByStudentAndActivity(Integer studentId, Integer activityId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<StudentActivityGrade> result = em.createQuery(
                "FROM StudentActivityGrade WHERE student.id = :sid AND activity.id = :aid",
                StudentActivityGrade.class)
                .setParameter("sid", studentId)
                .setParameter("aid", activityId)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve todas las notas de un alumno (todas las actividades).
     */
    public List<StudentActivityGrade> findByStudentId(Integer studentId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "FROM StudentActivityGrade WHERE student.id = :id",
                StudentActivityGrade.class)
                .setParameter("id", studentId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve todas las notas de una actividad (todos los alumnos evaluados).
     */
    public List<StudentActivityGrade> findByActivityId(Integer activityId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "FROM StudentActivityGrade WHERE activity.id = :id",
                StudentActivityGrade.class)
                .setParameter("id", activityId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
