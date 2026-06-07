package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.*;
import es.ieselrincon.rubrigrade.model.*;
import es.ieselrincon.rubrigrade.service.GradingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana ESTRELLA: evaluación de un alumno en una actividad.
 *
 * Flujo:
 *   1) Elegir asignatura -> se cargan sus actividades
 *   2) Elegir actividad  -> se cargan los criterios de su rúbrica
 *   3) Elegir alumno     -> si ya estaba evaluado, se cargan sus notas
 *   4) Introducir puntuaciones en la tabla
 *   5) Calcular o Guardar
 */
public class EvaluationWindow extends JDialog {

    //DAOs y servicio
    private final SubjectDao              subjectDao   = new SubjectDao();
    private final ActivityDao             activityDao  = new ActivityDao();
    private final StudentDao              studentDao   = new StudentDao();
    private final RubricCriterionDao      criterionDao = new RubricCriterionDao();
    private final StudentActivityGradeDao gradeDao     = new StudentActivityGradeDao();
    private final StudentCriterionScoreDao scoreDao    = new StudentCriterionScoreDao();
    private final GradingService          gradingService = new GradingService();

    //Componentes
    private JComboBox<Subject>  cmbAsignatura = new JComboBox<>();
    private JComboBox<Activity> cmbActividad  = new JComboBox<>();
    private JComboBox<Student>  cmbAlumno     = new JComboBox<>();

    private JTable             tabla;
    private DefaultTableModel  modelo;

    private JTextArea  txtComentarios = new JTextArea(2, 30);
    private JLabel     lblTotal       = new JLabel("0.0");
    private JLabel     lblNotaFinal   = new JLabel("0.0 / 10");

    // Bandera para evitar que los listeners se disparen mientras cargamos los desplegables.
    private boolean cargando = false;

    public EvaluationWindow(JFrame parent) {
        super(parent, "Evaluar alumno", true);
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearPanelSeleccion(), BorderLayout.NORTH);
        add(crearPanelCriterios(), BorderLayout.CENTER);
        add(crearPanelInferior(),  BorderLayout.SOUTH);

        cargarAsignaturasYAlumnos();
    }

    // PANEL DE SELECCIÓN (los 3 desplegables arriba)

    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selección"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Asignatura:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; panel.add(cmbAsignatura, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Actividad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; panel.add(cmbActividad, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panel.add(new JLabel("Alumno:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; panel.add(cmbAlumno, gbc);

        // Listeners "en cascada"
        cmbAsignatura.addActionListener(e -> {
            if (cargando) return;
            cargarActividadesDeAsignatura();
        });
        cmbActividad.addActionListener(e -> {
            if (cargando) return;
            cargarCriteriosDeActividad();
            cargarEvaluacionExistenteSiEsCaso();
        });
        cmbAlumno.addActionListener(e -> {
            if (cargando) return;
            cargarEvaluacionExistenteSiEsCaso();
        });

        return panel;
    }


    // TABLA DE CRITERIOS

    private JScrollPane crearPanelCriterios() {
        // Columnas:
        //   0 = criterion_id (oculta, la usamos internamente para saber a qué criterio se refiere cada fila)
        //   1 = Nombre del criterio (solo lectura)
        //   2 = Puntuación máxima (solo lectura)
        //   3 = Puntuación obtenida (editable)
        //   4 = Comentario (editable)
        String[] cols = {"ID", "Criterio", "Max", "Nota obtenida", "Comentario"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;   // Solo editables nota y comentario
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(24);

        // Ocultar la columna 0 (criterion_id) — sigue en el modelo, pero no se ve
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);

        return new JScrollPane(tabla);
    }


    // PANEL INFERIOR (comentarios + totales + botones)

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Comentarios generales
        JPanel comentarios = new JPanel(new BorderLayout(5, 5));
        comentarios.setBorder(BorderFactory.createTitledBorder("Comentarios generales"));
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        comentarios.add(new JScrollPane(txtComentarios), BorderLayout.CENTER);

        //  Totales
        JPanel totales = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        totales.setBorder(BorderFactory.createTitledBorder("Resultado"));

        JLabel etTotal = new JLabel("Total bruto:");
        etTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel etFinal = new JLabel("Nota final:");
        etFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNotaFinal.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblNotaFinal.setForeground(new Color(70, 50, 180));

        totales.add(etTotal); totales.add(lblTotal);
        totales.add(etFinal); totales.add(lblNotaFinal);

        // Botones
        JButton btnCalcular   = new JButton("Calcular");
        JButton btnGuardar    = new JButton("Guardar evaluación");
        JButton btnLimpiar    = new JButton("Limpiar formulario");
        JButton btnCerrar     = new JButton("Cerrar");

        btnCalcular.addActionListener(e -> calcular(false));
        btnGuardar .addActionListener(e -> guardar());
        btnLimpiar .addActionListener(e -> limpiar());
        btnCerrar  .addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnCalcular);
        botones.add(btnGuardar);
        botones.add(btnLimpiar);
        botones.add(btnCerrar);

        panel.add(comentarios, BorderLayout.NORTH);
        panel.add(totales,     BorderLayout.CENTER);
        panel.add(botones,     BorderLayout.SOUTH);

        return panel;
    }


    // CARGAS EN CASCADA

    private void cargarAsignaturasYAlumnos() {
        cargando = true;

        cmbAsignatura.removeAllItems();
        for (Subject s : subjectDao.findAll()) cmbAsignatura.addItem(s);

        cmbAlumno.removeAllItems();
        for (Student a : studentDao.findAll()) cmbAlumno.addItem(a);

        cargando = false;

        // Disparamos manualmente la primera carga
        cargarActividadesDeAsignatura();
    }

    private void cargarActividadesDeAsignatura() {
        cargando = true;

        cmbActividad.removeAllItems();
        Subject asig = (Subject) cmbAsignatura.getSelectedItem();
        if (asig != null) {
            for (Activity a : activityDao.findBySubjectId(asig.getId())) {
                cmbActividad.addItem(a);
            }
        }
        modelo.setRowCount(0);
        lblTotal.setText("0.0");
        lblNotaFinal.setText("0.0 / 10");

        cargando = false;

        cargarCriteriosDeActividad();
        cargarEvaluacionExistenteSiEsCaso();
    }

    private void cargarCriteriosDeActividad() {
        modelo.setRowCount(0);
        Activity act = (Activity) cmbActividad.getSelectedItem();
        if (act == null) return;

        // Los criterios pertenecen a la rúbrica de la actividad
        List<RubricCriterion> criterios = criterionDao.findByRubricId(act.getRubric().getId());
        for (RubricCriterion c : criterios) {
            modelo.addRow(new Object[]{
                    c.getId(),         // 0: id oculto
                    c.getName(),       // 1: nombre
                    c.getMaxScore(),   // 2: máximo
                    "",                // 3: nota (vacía hasta que el profe la meta)
                    ""                 // 4: comentario
            });
        }
        lblTotal.setText("0.0");
        lblNotaFinal.setText("0.0 / 10");
        txtComentarios.setText("");
    }

    /**
     * Si el alumno seleccionado ya estaba evaluado en esta actividad,
     * pre-cargamos sus notas en la tabla para poder modificarlas.
     */
    private void cargarEvaluacionExistenteSiEsCaso() {
        Activity act    = (Activity) cmbActividad.getSelectedItem();
        Student  alumno = (Student)  cmbAlumno.getSelectedItem();
        if (act == null || alumno == null) return;

        StudentActivityGrade existente =
                gradeDao.findByStudentAndActivity(alumno.getId(), act.getId());
        if (existente == null) return;

        // Indexamos las puntuaciones por criterion_id para buscarlas rápido
        List<StudentCriterionScore> scores = scoreDao.findByGradeId(existente.getId());
        Map<Integer, StudentCriterionScore> porCriterio = new HashMap<>();
        for (StudentCriterionScore s : scores) {
            porCriterio.put(s.getCriterion().getId(), s);
        }

        // Rellenamos cada fila si tenemos puntuación guardada para su criterio
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            Integer critId = (Integer) modelo.getValueAt(fila, 0);
            StudentCriterionScore s = porCriterio.get(critId);
            if (s != null) {
                modelo.setValueAt(s.getScore(), fila, 3);
                modelo.setValueAt(s.getComment() == null ? "" : s.getComment(), fila, 4);
            }
        }
        txtComentarios.setText(existente.getComments() == null ? "" : existente.getComments());

        if (existente.getTotalScore() != null) lblTotal.setText(String.valueOf(existente.getTotalScore()));
        if (existente.getFinalGrade() != null) lblNotaFinal.setText(String.format("%.2f / 10", existente.getFinalGrade()));
    }


    // ACCIONES

    /**
     * Recorre la tabla, valida y calcula los totales.
     * @param silencioso  Si es true, no muestra errores al usuario (lo usa guardar() para validar antes).
     * @return Mapa criterion_id -> puntuación válido, o null si hay error.
     */
    private Map<Integer, Double> calcular(boolean silencioso) {
        // Cerrar edición pendiente: si el cursor está dentro de una celda al pulsar Calcular,
        // su valor podría no estar todavía en el modelo. Esto fuerza el commit.
        if (tabla.isEditing()) tabla.getCellEditor().stopCellEditing();

        if (modelo.getRowCount() == 0) {
            if (!silencioso) error("No hay criterios cargados. Elige una actividad.", null);
            return null;
        }

        Map<Integer, Double> scores = new HashMap<>();
        double total = 0.0;
        double maxPosible = 0.0;

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            Integer critId   = (Integer) modelo.getValueAt(fila, 0);
            String  nombre   = String.valueOf(modelo.getValueAt(fila, 1));
            Double  max      = Double.parseDouble(String.valueOf(modelo.getValueAt(fila, 2)));
            Object  notaObj  = modelo.getValueAt(fila, 3);
            String  notaTxt  = notaObj == null ? "" : String.valueOf(notaObj).trim().replace(',', '.');

            if (notaTxt.isEmpty()) {
                if (!silencioso) error("Falta puntuación en el criterio: " + nombre, null);
                return null;
            }

            double nota;
            try {
                nota = Double.parseDouble(notaTxt);
            } catch (NumberFormatException ex) {
                if (!silencioso) error("La nota de '" + nombre + "' no es un número válido.", null);
                return null;
            }
            if (nota < 0) {
                if (!silencioso) error("La nota de '" + nombre + "' no puede ser negativa.", null);
                return null;
            }
            if (nota > max) {
                if (!silencioso) error("La nota de '" + nombre + "' (" + nota
                        + ") supera el máximo (" + max + ").", null);
                return null;
            }

            scores.put(critId, nota);
            total      += nota;
            maxPosible += max;
        }

        Activity act = (Activity) cmbActividad.getSelectedItem();
        double notaActividad = act == null ? 10.0 : act.getMaxScore();
        double notaFinal = (total / maxPosible) * notaActividad;

        lblTotal.setText(String.format("%.2f", total));
        lblNotaFinal.setText(String.format("%.2f / %.0f", notaFinal, notaActividad));

        return scores;
    }

    private void guardar() {
        Activity act    = (Activity) cmbActividad.getSelectedItem();
        Student  alumno = (Student)  cmbAlumno.getSelectedItem();

        if (act == null)    { error("Elige una actividad.", null); return; }
        if (alumno == null) { error("Elige un alumno.", null); return; }

        Map<Integer, Double> scores = calcular(false);
        if (scores == null) return;   // El calcular ya mostró el error

        try {
            StudentActivityGrade nota = gradingService.evaluate(
                    alumno.getId(),
                    act.getId(),
                    scores,
                    txtComentarios.getText().trim().isEmpty() ? null : txtComentarios.getText().trim()
            );

            // Guardar también los comentarios por criterio (no los toca el GradingService)
            guardarComentariosDeCriterios(nota);

            JOptionPane.showMessageDialog(this,
                    "Evaluación guardada.\nNota final: "
                            + String.format("%.2f / %.0f", nota.getFinalGrade(), act.getMaxScore()),
                    "Evaluación guardada",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            error("No se pudo guardar la evaluación", ex);
        }
    }

    /**
     * Hace pasada extra para añadir los comentarios de cada criterio
     * (el GradingService solo guarda la puntuación, no el comentario).
     */
    private void guardarComentariosDeCriterios(StudentActivityGrade nota) {
        List<StudentCriterionScore> scores = scoreDao.findByGradeId(nota.getId());
        Map<Integer, StudentCriterionScore> porCriterio = new HashMap<>();
        for (StudentCriterionScore s : scores) porCriterio.put(s.getCriterion().getId(), s);

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            Integer critId = (Integer) modelo.getValueAt(fila, 0);
            String coment  = String.valueOf(modelo.getValueAt(fila, 4)).trim();
            StudentCriterionScore s = porCriterio.get(critId);
            if (s == null) continue;
            s.setComment(coment.isEmpty() ? null : coment);
            scoreDao.update(s);
        }
    }

    private void limpiar() {
        if (modelo.getRowCount() > 0) {
            // Volvemos a cargar criterios (deja la actividad/alumno seleccionados)
            cargarCriteriosDeActividad();
        }
        txtComentarios.setText("");
        lblTotal.setText("0.0");
        lblNotaFinal.setText("0.0 / 10");
    }


    // AUXILIARES

    private void error(String mensaje, Exception ex) {
        String texto = (ex == null) ? mensaje : mensaje + ":\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
