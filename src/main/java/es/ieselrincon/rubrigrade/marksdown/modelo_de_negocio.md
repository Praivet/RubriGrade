# Modelo de Negocio — RubriGrade

---

## 1. ¿Qué problema resuelve?

Evaluar a un alumno mediante una rúbrica es un proceso que, hecho a mano, implica sumar puntuaciones criterio a criterio, convertirlas a escala 0-10, y registrarlas en algún sitio. Con varios alumnos y varias actividades, esto se vuelve tedioso y propenso a errores.

RubriGrade automatiza ese proceso: el profesor define una vez la rúbrica, introduce las puntuaciones de cada alumno, y la aplicación calcula y guarda la nota final automáticamente.

---

## 2. Actores

**Profesor** — Es el único usuario de la aplicación. Se encarga de:
- Dar de alta alumnos, asignaturas, rúbricas y actividades.
- Evaluar a cada alumno en cada actividad introduciendo las puntuaciones por criterio.
- Consultar las notas resultantes.

---

## 3. Entidades del negocio y su relación

### Asignatura (`Subject`)
Una asignatura agrupa un conjunto de actividades evaluables. Por ejemplo: *Programación DAW 2025/26*.

### Rúbrica (`Rubric`)
Una rúbrica es una plantilla de evaluación formada por criterios. Define **cómo** se va a evaluar una actividad. Una misma rúbrica puede reutilizarse en varias actividades.

### Criterio de rúbrica (`RubricCriterion`)
Cada rúbrica se descompone en criterios, cada uno con una puntuación máxima propia. Por ejemplo, una rúbrica de programación podría tener:
- *Funcionalidad* → máx. 5 puntos
- *Limpieza del código* → máx. 3 puntos
- *Documentación* → máx. 2 puntos

La suma de los máximos de todos los criterios determina la puntuación máxima total de la rúbrica.

### Actividad (`Activity`)
Una actividad es una tarea concreta que se va a evaluar dentro de una asignatura. Tiene asignada una rúbrica y un peso (%) sobre la nota final de la asignatura. Por ejemplo: *Proyecto UT8 · peso 30% · nota máxima 10*.

### Alumno (`Student`)
Persona que va a ser evaluada. Se identifica por nombre, apellidos, email y número de matrícula (NIA).

### Nota de actividad (`StudentActivityGrade`)
Representa la evaluación de un alumno en una actividad concreta. Almacena:
- La puntuación total obtenida (suma de todos los criterios).
- La nota final convertida a escala 0-10.
- Los comentarios generales del profesor.
- La fecha en que se realizó la evaluación.

Un alumno solo puede tener una nota por actividad. Si se evalúa de nuevo, la nota anterior se sobreescribe.

### Puntuación por criterio (`StudentCriterionScore`)
Es el desglose de la nota: cuántos puntos obtuvo el alumno en cada criterio concreto. Permite saber no solo la nota final, sino por qué aspectos el alumno ha subido o bajado puntos.

---

## 4. Flujo principal de uso

```
1. El profesor crea una asignatura

2. El profesor crea una rúbrica y le añade sus criterios

3. El profesor crea una actividad vinculando asignatura + rúbrica

4. El profesor da de alta a los alumnos

5. Por cada alumno, el profesor abre "Evaluar alumno",
   selecciona alumno y actividad, introduce la puntuación
   de cada criterio y guarda

6. La aplicación calcula automáticamente la nota final
   y la guarda junto al desglose por criterios

7. El profesor puede consultar las notas por alumno
   o por actividad
```

---

## 5. Reglas de negocio

- La puntuación de cada criterio **no puede ser negativa** ni **superar el máximo** definido en ese criterio.
- La nota final se calcula así:

```
nota_final = (suma_puntuaciones_obtenidas / suma_maximos_criterios) × nota_maxima_actividad
```

Por ejemplo: si el alumno obtiene 7 de 10 posibles, y la actividad tiene nota máxima 10, la nota final es **7.0**.

- Si una actividad tiene **peso**, se puede calcular la nota ponderada:

```
nota_ponderada = nota_final × (peso / 100)
```

Por ejemplo: nota 7.0 con peso 30% -> aporta **2.1 puntos** a la nota final de la asignatura.

- Un alumno **solo puede tener una nota por actividad**. Si se vuelve a evaluar, la nota anterior se reemplaza completamente, incluyendo el desglose por criterios.

- Borrar un alumno elimina también **todas sus notas** (cascade).
- Borrar una asignatura elimina también **todas sus actividades** y, en cascada, las notas asociadas.
- Borrar una rúbrica elimina también **todos sus criterios**.

---

## 6. Diagrama de relaciones entre entidades

```
Subject (Asignatura)
  │
  │ 1:N
  ▼
Activity (Actividad) ◄────── Rubric (Rúbrica)
  │                                │
  │ 1:N                            │ 1:N
  ▼                                ▼
StudentActivityGrade      RubricCriterion (Criterio)
  (Nota de alumno)                 │
  │                                │ N:1
  │ 1:N                            │
  ▼                                │
StudentCriterionScore ◄────────────┘
  (Puntuación por criterio)

Student (Alumno)
  │
  │ 1:N
  └──────────────► StudentActivityGrade
```

---

*Desarrollado por Juan Pablo y Iván Brito Pérez · 1.º DAWTA · IES El Rincón · Curso 2025/26*