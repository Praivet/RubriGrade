# Especificaciones Técnicas — RubriGrade


---

## 1. Descripción general

RubriGrade es una aplicación de escritorio desarrollada en Java que permite a un profesor gestionar rúbricas de evaluación y calcular las notas de sus alumnos de forma automática a partir de los criterios definidos en cada rúbrica. La aplicación cubre el ciclo completo: creación de asignaturas, actividades, rúbricas con sus criterios, alumnos, y evaluación de cada alumno en cada actividad.

---

## 2. Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Maven | 3.x | Gestión de dependencias y build |
| Hibernate ORM | 6.6.4.Final | Mapeo objeto-relacional (JPA) |
| Jakarta Persistence | 3.0 | API estándar de persistencia |
| MySQL Connector/J | 9.1.0 | Driver JDBC para MySQL |
| MySQL | 8.x | Base de datos relacional |
| Java Swing | (incluido en JDK) | Interfaz gráfica de escritorio |
| SLF4J | 2.0.13 | Logging |

---

## 3. Arquitectura del proyecto

El proyecto sigue una arquitectura en capas:

```
src/main/java/es/ieselrincon/rubrigrade/
│
├── Main.java                  — Punto de entrada
│
├── model/                     — Entidades JPA (tablas de la BD)
│   ├── Student.java
│   ├── Subject.java
│   ├── Rubric.java
│   ├── RubricCriterion.java
│   ├── Activity.java
│   ├── StudentActivityGrade.java
│   └── StudentCriterionScore.java
│
├── dao/                       — Acceso a datos
│   ├── GenericDao.java        — CRUD genérico reutilizable
│   ├── StudentDao.java
│   ├── SubjectDao.java
│   ├── RubricDao.java
│   ├── RubricCriterionDao.java
│   ├── ActivityDao.java
│   ├── StudentActivityGradeDao.java
│   └── StudentCriterionScoreDao.java
│
├── service/                   — Lógica de negocio
│   └── GradingService.java
│
├── view/                      — Interfaz gráfica (Swing)
│   ├── MainWindow.java
│   ├── StudentsWindow.java
│   ├── SubjectsWindow.java
│   ├── RubricsWindow.java
│   └── ActivitiesWindow.java
│
└── util/
    └── HibernateUtil.java     — Gestión del EntityManagerFactory
```

---

## 4. Modelo de datos

### Tablas y relaciones

```
students ──────────────────────────────────────────────────────┐
                                                               │
subjects ──→ activities ──→ student_activity_grades ←──────────┘
                 │                    │
rubrics ─────────┘                    └──→ student_criterion_scores
   │                                              │
   └──→ rubric_criteria ──────────────────────────┘
```

### Entidades

**`students`** — Alumnos
- `id`, `first_name`, `last_name`, `email` (único), `enrollment_number` (único), `created_at`

**`subjects`** — Asignaturas
- `id`, `code` (único), `name`, `description`, `academic_year`

**`rubrics`** — Rúbricas de evaluación
- `id`, `name`, `description`, `max_score`, `created_at`

**`rubric_criteria`** — Criterios de cada rúbrica
- `id`, `rubric_id` (FK), `name`, `description`, `max_score`, `display_order`

**`activities`** — Actividades evaluables
- `id`, `subject_id` (FK), `rubric_id` (FK), `name`, `description`, `max_score`, `weight`, `due_date`

**`student_activity_grades`** — Nota de un alumno en una actividad
- `id`, `student_id` (FK), `activity_id` (FK), `total_score`, `final_grade`, `comments`, `evaluated_at`
- Restricción: `UNIQUE(student_id, activity_id)` — un alumno solo puede tener una nota por actividad

**`student_criterion_scores`** — Puntuación por criterio dentro de una nota
- `id`, `student_activity_grade_id` (FK), `rubric_criterion_id` (FK), `score`, `comment`
- Restricción: `UNIQUE(student_activity_grade_id, rubric_criterion_id)`

---

## 5. Capa de persistencia (Hibernate / JPA)

### Configuración

La conexión a la base de datos se configura en `src/main/resources/META-INF/persistence.xml`:

- **Persistence unit:** `rubrigradePU`
- **Driver:** `com.mysql.cj.jdbc.Driver`
- **URL:** `jdbc:mysql://localhost:3306/rubrigrade`
- **Dialecto:** `org.hibernate.dialect.MySQLDialect`
- **DDL:** `hibernate.hbm2ddl.auto = update` (Hibernate actualiza las tablas automáticamente si cambia el modelo)

### HibernateUtil

Clase utilitaria que mantiene un único `EntityManagerFactory` durante toda la vida de la aplicación. Por cada operación contra la BD se crea un `EntityManager` nuevo que se cierra al terminar, siguiendo el patrón *open-session-per-request*.

### Patrón DAO

Se implementa un `GenericDao<T>` abstracto con las operaciones CRUD comunes (`save`, `update`, `deleteById`, `findById`, `findAll`). Cada DAO específico hereda de él y añade únicamente las consultas propias que necesita, evitando duplicar código.

---

## 6. Lógica de negocio — GradingService

El `GradingService` centraliza el proceso de evaluación de un alumno:

1. Carga el alumno y la actividad desde la BD.
2. Valida cada puntuación introducida (no negativa, no superior al máximo del criterio).
3. Calcula la nota total sumando las puntuaciones de todos los criterios.
4. Convierte la nota total a escala 0-10 en función de la nota máxima de la actividad.
5. Si el alumno ya estaba evaluado en esa actividad, actualiza la nota existente; si no, crea una nueva.
6. Guarda la nota y el desglose por criterios en una única transacción.

También proporciona métodos auxiliares para calcular la media de una actividad y la nota ponderada por peso.

---

## 7. Interfaz gráfica (Swing)

La interfaz sigue el patrón **master-detail**: una tabla superior muestra todos los registros y un formulario inferior permite crear, modificar o borrar el registro seleccionado.

### Ventanas

| Ventana | Tipo | Descripción |
|---|---|---|
| `MainWindow` | `JFrame` | Ventana principal con barra de menú |
| `StudentsWindow` | `JDialog` modal | CRUD de alumnos |
| `SubjectsWindow` | `JDialog` modal | CRUD de asignaturas |
| `RubricsWindow` | `JDialog` modal | CRUD de rúbricas y criterios (master-detail doble) |
| `ActivitiesWindow` | `JDialog` modal | CRUD de actividades con desplegables de asignatura y rúbrica |

### Layout Managers utilizados

- `BorderLayout` — estructura general de cada ventana
- `GridBagLayout` — formularios con campos de distinto tamaño
- `GridLayout` — formularios simples de pares etiqueta-campo
- `FlowLayout` — filas de botones

### Arranque

La aplicación se inicia en el Event Dispatch Thread (EDT) mediante `SwingUtilities.invokeLater`, tal como exige Swing para garantizar la seguridad del hilo de la interfaz gráfica.

---

## 8. Cómo ejecutar el proyecto

**Requisitos previos:**
- JDK 21 o superior
- Maven 3.x
- MySQL 8.x en ejecución local

**Pasos:**

1. Crear la base de datos en MySQL:
```sql
CREATE DATABASE rubrigrade;
```

2. Ajustar usuario y contraseña en `persistence.xml` si es necesario.

3. Compilar y ejecutar con Maven:
```bash
mvn compile exec:java -Dexec.mainClass="es.ieselrincon.rubrigrade.Main"
```

Hibernate creará las tablas automáticamente en el primer arranque gracias a `hbm2ddl.auto = update`.

---

*Desarrollado por Juan Pablo Miguel Velásquez e Iván Brito Pérez · 1.º DAWTA · IES El Rincón · Curso 2025/26*