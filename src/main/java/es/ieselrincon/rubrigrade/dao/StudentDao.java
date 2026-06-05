package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.Student;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class StudentDao extends GenericDao<Student> {

    public StudentDao() {
        super(Student.class);
    }

    /**
     * Busca un alumno por su email. Devuelve null si no existe.
     */
    public Student findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Student> result = em
                .createQuery("FROM Student WHERE email = :email", Student.class)
                .setParameter("email", email)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }
}
