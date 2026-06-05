package es.ieselrincon.rubrigrade.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Clase de utilidad para manejar la conexión con Hibernate.
 *
 * Mantiene un único EntityManagerFactory (caro de crear) y lo reutiliza
 * durante toda la vida de la aplicación. Por cada operación contra la BD
 * se pide un EntityManager nuevo y se cierra al terminar.
 */
public class HibernateUtil {

    // Debe coincidir con el name="..." del persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "rubrigradePU";

    private static final EntityManagerFactory EMF;

    static {
        try {
            EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Throwable ex) {
            System.err.println("Error al inicializar EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Constructor privado: nadie debería instanciar esta clase
    private HibernateUtil() {
    }

    /**
     * Devuelve un EntityManager listo para usar.
     * El que lo pida es responsable de cerrarlo (em.close()).
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /**
     * Cierra el EntityManagerFactory.
     * Se llama una sola vez al cerrar la aplicación.
     */
    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
