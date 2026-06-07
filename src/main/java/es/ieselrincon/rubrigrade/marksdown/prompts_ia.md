# Uso de IA en el desarrollo de RubriGrade

Durante el desarrollo de **RubriGrade**, hemos utilizado Claude como herramienta de apoyo para resolver dudas técnicas concretas que surgieron a lo largo del proceso. A continuación explicaremos las dos consultas principales que realzamos y cómo nos ayudaron.

---

## Consulta 1: Relaciones entre entidades en Hibernate

### ¿Por qué hicimos esta pregunta?

Al diseñar las clases del modelo (`Student`, `Activity`, `Rubric`, etc.) nos encontramos con anotaciones como `@ManyToOne`, `@OneToMany` y `@JoinColumn` que no teníamos del todo claras. Sabíamos que servían para relacionar tablas, pero no sabíamos exactamente como aplicarlo, ni por qué la clave foránea aparecía en unas clases y no en otras. Necesitabamos entenderlo para poder escribir el modelo correctamente y que Hibernate generase la base de datos de forma coherente.

### ¿En qué nos ayudó?

La explicación nos permitió entender la lógica detrás de cada anotación, usando ejemplos del propio código del proyecto, lo que hizo mucho más fácil asimilarlo. Gracias a esto pudimos:

- Saber con seguridad en qué clase poner `@JoinColumn` (siempre en el lado `@ManyToOne`, que es el que tiene la FK en la BD).
- Entender el papel de `mappedBy` en `@OneToMany` y por qué sin él Hibernate crearía tablas intermedias innecesarias.
- Comprender cómo `cascade` y `orphanRemoval` nos ahorran tener que borrar manualmente los registros dependientes cuando eliminamos un alumno o una rúbrica.
- Entender por qué se usa `FetchType.LAZY` y qué problema evita en términos de rendimiento con las queries.

En resumen, nos dio la base para diseñar el modelo de datos con criterio, no solo copiando anotaciones sin saber qué hacían.

---

## Consulta 2: Diseño de la interfaz gráfica con Swing

### ¿Por qué hicimos esta pregunta?

Una vez teníamos el backend (modelo, DAOs y servicio) funcionando, tocaba construir las ventanas con Swing. Había partes que no terminabamos de entender: no entendíamos bien la diferencia entre `JFrame` y `JDialog`, los distintos Layout Managers nos resultaban confusos, y tampoco teníamos claro por qué había que usar `SwingUtilities.invokeLater` en el `main` en vez de simplemente crear la ventana directamente.

### ¿En qué nos ayudó?

La explicación nos dio una visión general de cómo encajan todas las piezas de Swing, usando como referencia las propias ventanas del proyecto. Gracias a esto pudimos:

- Entender por qué todas las ventanas secundarias son `JDialog` modal y no `JFrame`, y qué efecto tiene eso sobre la experiencia del usuario (bloquear la ventana principal mientras están abiertas).
- Elegir el Layout Manager correcto para cada situación: `BorderLayout` para la estructura general de las ventanas, `GridLayout` para los formularios simples, `GridBagLayout` para los más complejos, y `FlowLayout` para las filas de botones.
- Entender la separación entre `JTable` y `DefaultTableModel`, y por qué para refrescar la tabla basta con vaciar el modelo (`setRowCount(0)`) sin tocar el componente visual.
- Saber por qué `invokeLater` es la forma correcta de arrancar la aplicación y qué problema previene.
- Comprender el `!e.getValueIsAdjusting()` en los listeners de la tabla, que evitaba que el formulario se rellenase dos veces por cada clic.

En definitiva, nos ayudó a entender la estructura de las ventanas que ya teníamos escritas y a poder modificarlas con seguridad sin romper nada.

---

*Proyecto Final UT8 · 1.º DAWTA · IES El Rincón · Curso 2025/26 · Juan Pablo Miguel Velásquez · Iván Brito Pérez*