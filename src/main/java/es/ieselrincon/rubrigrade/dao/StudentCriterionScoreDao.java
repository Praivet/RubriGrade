package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.StudentCriterionScore;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.sql.SQLException;
import java.util.List;

public class StudentCriterionScoreDao extends GenericDao<StudentCriterionScore> {

    public StudentCriterionScoreDao() {
        super(StudentCriterionScore.class);
    }

    /**
     * Devuelve el desglose de puntuaciones de una nota concreta.
     * Ej: la nota 7,8 se compone de 0,8 + 1,5 + 1,7 + ...
     */
    public List<StudentCriterionScore> findByGradeId(Integer gradeId)  {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "FROM StudentCriterionScore WHERE grade.id = :id",
                StudentCriterionScore.class)
                .setParameter("id", gradeId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
