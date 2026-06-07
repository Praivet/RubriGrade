package es.ieselrincon.rubrigrade.service;

import es.ieselrincon.rubrigrade.model.*;
import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;

/**
 * Servicio que se encarga de la lógica de evaluación:
 *   - Validar las puntuaciones introducidas.
 *   - Calcular la nota total y la nota final (escala 0-10).
 *   - Guardar la evaluación completa (cabecera + desglose) en una sola transacción.
 *
 * Es la parte más importante del proyecto desde el punto de vista de negocio.
 */
public class GradingService {

    /**
     * Evalúa a un alumno en una actividad.
     *
     * @param studentId       id del alumno a evaluar
     * @param activityId      id de la actividad
     * @param scoresByCriterion Mapa con la puntuación obtenida en cada criterio
     *                          (clave = id del criterio, valor = puntos)
     * @param comments        Comentarios generales (puede ser null)
     * @return  La nota guardada en la BD (con id, total y nota final 0-10)
     */

    public StudentActivityGrade evaluate(
            Integer studentId,
            Integer activityId,
            Map<Integer, Double> scoresByCriterion,
            String comments) {

        EntityManager em = HibernateUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            // 1) Cargar alumno y actividad
            Student student = em.find(Student.class, studentId);
            Activity activity = em.find(Activity.class, activityId);

            if (student == null) {
                throw new IllegalArgumentException("No existe el alumno con id " + studentId);
            }
            if (activity == null) {
                throw new IllegalArgumentException("No existe la actividad con id " + activityId);
            }
            if (scoresByCriterion == null || scoresByCriterion.isEmpty()) {
                throw new IllegalArgumentException("Debes puntuar al menos un criterio");
            }

            // 2) Validar las puntuaciones y calcular totales
            double totalScore = 0.0;
            double maxPossible = 0.0;

            for (Map.Entry<Integer, Double> entry : scoresByCriterion.entrySet()) {
                RubricCriterion criterion = em.find(RubricCriterion.class, entry.getKey());
                Double score = entry.getValue();

                if (criterion == null) {
                    throw new IllegalArgumentException("No existe el criterio con id " + entry.getKey());
                }
                if (score == null) {
                    throw new IllegalArgumentException(
                        "Falta puntuación en el criterio: " + criterion.getName());
                }
                if (score < 0) {
                    throw new IllegalArgumentException(
                        "La puntuación de '" + criterion.getName() + "' no puede ser negativa");
                }
                if (score > criterion.getMaxScore()) {
                    throw new IllegalArgumentException(
                        "La puntuación de '" + criterion.getName() + "' (" + score + ") "
                      + "supera el máximo permitido (" + criterion.getMaxScore() + ")");
                }

                totalScore  += score;
                maxPossible += criterion.getMaxScore();
            }

            // 3) Convertir a escala 0-10 según la nota máxima de la actividad
            double finalGrade = (totalScore / maxPossible) * activity.getMaxScore();

            // 4) ¿Ya estaba evaluado? -> actualizamos. Si no -> creamos
            List<StudentActivityGrade> existing = em.createQuery(
                    "FROM StudentActivityGrade WHERE student.id = :sid AND activity.id = :aid",
                    StudentActivityGrade.class)
                    .setParameter("sid", studentId)
                    .setParameter("aid", activityId)
                    .getResultList();

            StudentActivityGrade grade;
            if (existing.isEmpty()) {
                grade = new StudentActivityGrade(student, activity);
                em.persist(grade);
            } else {
                grade = existing.get(0);
                // Borramos las puntuaciones anteriores (orphanRemoval hará el resto)
                grade.getCriterionScores().clear();
            }

            grade.setTotalScore(totalScore);
            grade.setFinalGrade(finalGrade);
            grade.setComments(comments);

            // 5) Crear las nuevas puntuaciones por criterio
            for (Map.Entry<Integer, Double> entry : scoresByCriterion.entrySet()) {
                RubricCriterion criterion = em.find(RubricCriterion.class, entry.getKey());
                StudentCriterionScore scs =
                        new StudentCriterionScore(grade, criterion, entry.getValue());
                grade.getCriterionScores().add(scs);
                em.persist(scs);
            }

            em.getTransaction().commit();
            return grade;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Calcula la nota media de todos los alumnos en una actividad.
     * Útil para la pantalla de consultas.
     */
    public double calculateActivityAverage(Integer activityId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Double avg = em.createQuery(
                    "SELECT AVG(g.finalGrade) FROM StudentActivityGrade g WHERE g.activity.id = :id",
                    Double.class)
                    .setParameter("id", activityId)
                    .getSingleResult();
            return avg == null ? 0.0 : avg;
        } finally {
            em.close();
        }
    }

    /**
     * Calcula la nota ponderada (nota final × peso de la actividad).
     * Ej: nota 7,8 con peso 30% -> 2,34
     */
    public double calculateWeightedGrade(StudentActivityGrade grade) {
        if (grade == null || grade.getFinalGrade() == null) return 0.0;

        Double weight = grade.getActivity().getWeight();

        if (weight == null) weight = 100.0;
        return grade.getFinalGrade() * (weight / 100.0);
    }
}
