package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.Subject;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class SubjectDao extends GenericDao<Subject> {

    public SubjectDao() {
        super(Subject.class);
    }

    /**
     * Busca una asignatura por su código (ej: "DAW-PROG").
     */
    public Subject findByCode(String code) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Subject> result = em
                .createQuery("FROM Subject WHERE code = :code", Subject.class)
                .setParameter("code", code)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }
}
