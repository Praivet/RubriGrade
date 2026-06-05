package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.RubricCriterion;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class RubricCriterionDao extends GenericDao<RubricCriterion> {

    public RubricCriterionDao() {
        super(RubricCriterion.class);
    }

    /**
     * Devuelve todos los criterios de una rúbrica ordenados por display_order.
     */
    public List<RubricCriterion> findByRubricId(Integer rubricId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "FROM RubricCriterion WHERE rubric.id = :id ORDER BY displayOrder",
                RubricCriterion.class)
                .setParameter("id", rubricId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
