package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.Activity;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ActivityDao extends GenericDao<Activity> {

    public ActivityDao() {
        super(Activity.class);
    }

    /**
     * Devuelve todas las actividades de una asignatura.
     * Útil para el desplegable "Asignatura" → "Actividad" en la ventana de evaluación.
     */
    public List<Activity> findBySubjectId(Integer subjectId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "FROM Activity WHERE subject.id = :id",
                Activity.class)
                .setParameter("id", subjectId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
