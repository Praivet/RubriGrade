package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * DAO genérico: contiene los métodos CRUD comunes a todas las entidades.
 * Cada DAO específico (StudentDao, SubjectDao...) hereda de aquí y
 * solo añade las consultas propias que necesite.
 *
 * @param <T> Tipo de la entidad (Student, Subject, Rubric, etc.)
 */

public abstract class GenericDao<T> {

    // Guardamos la "clase" de la entidad para poder usarla en em.find() y queries
    protected final Class<T> entityClass;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Inserta una entidad nueva en la BD.
     */
    public void save(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // metemos el objeto en la bd
            em.persist(entity);
            //commit lo guarda
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza una entidad existente en la BD.
     * Devuelve la entidad actualizada (porque merge() devuelve una copia).
     */
    public T update(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            T merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Borra una entidad por su id.
     */
    public void deleteById(Integer id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Busca una entidad por su id. Devuelve null si no existe.
     */
    public T findById(Integer id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve todas las entidades de la tabla.
     */
    public List<T> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "FROM " + entityClass.getSimpleName();
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }
}
