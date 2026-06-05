package es.ieselrincon.rubrigrade;

import es.ieselrincon.rubrigrade.dao.StudentDao;
import es.ieselrincon.rubrigrade.model.Student;
import es.ieselrincon.rubrigrade.util.HibernateUtil;

import java.util.List;

// TEST TEMPORAL JP TE QUIERO

public class Main {

    public static void main(String[] args) {

        StudentDao dao = new StudentDao();

        System.out.println("==== 1) LEER todos los alumnos ====");
        List<Student> alumnos = dao.findAll();
        alumnos.forEach(a -> System.out.println("  " + a));

        System.out.println("\n==== 2) CREAR un alumno nuevo ====");
        Student nuevo = new Student("Pepe", "Pérez", "pepe@test.com", "NIA999");
        dao.save(nuevo);
        System.out.println("  Creado con id: " + nuevo.getId());

        System.out.println("\n==== 3) BUSCAR por id ====");
        Student encontrado = dao.findById(nuevo.getId());
        System.out.println("  Encontrado: " + encontrado);

        System.out.println("\n==== 4) BUSCAR por email ====");
        Student porEmail = dao.findByEmail("pepe@test.com");
        System.out.println("  Encontrado por email: " + porEmail);

        System.out.println("\n==== 5) MODIFICAR ====");
        encontrado.setFirstName("Pepito");
        dao.update(encontrado);
        System.out.println("  Modificado: " + dao.findById(encontrado.getId()));

        System.out.println("\n==== 6) BORRAR ====");
        dao.deleteById(encontrado.getId());
        System.out.println("  Borrado. ¿Existe aún? " + dao.findById(encontrado.getId()));

        System.out.println("\n==== TEST COMPLETO OK ====");

        HibernateUtil.shutdown();
    }
}
