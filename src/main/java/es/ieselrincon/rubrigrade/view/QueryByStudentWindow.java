package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.StudentActivityGradeDao;
import es.ieselrincon.rubrigrade.dao.StudentCriterionScoreDao;
import es.ieselrincon.rubrigrade.dao.StudentDao;
import es.ieselrincon.rubrigrade.model.Student;
import es.ieselrincon.rubrigrade.model.StudentActivityGrade;
import es.ieselrincon.rubrigrade.model.StudentCriterionScore;
import es.ieselrincon.rubrigrade.service.GradingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Consulta de notas por alumno.
 * Eliges un alumno y se muestran TODAS sus notas, además de la media global.
 * Si seleccionas una fila, abajo se ve el desglose por criterio.
 */
public class QueryByStudentWindow extends JDialog {

    private final StudentDao              studentDao = new StudentDao();
    private final StudentActivityGradeDao gradeDao   = new StudentActivityGradeDao();
    private final StudentCriterionScoreDao scoreDao  = new StudentCriterionScoreDao();
    private final GradingService          gradingService = new GradingService();

    private JComboBox<Student> cmbAlumno = new JComboBox<>();

    private JTable tablaNotas;
    private DefaultTableModel modeloNotas;

    private JTable tablaDesglose;
    private DefaultTableModel modeloDesglose;

    private JLabel lblMedia      = new JLabel("-");
    private JLabel lblPonderada  = new JLabel("-");

    public QueryByStudentWindow(JFrame parent) {
        super(parent, "Consulta de notas por alumno", true);
        setSize(850, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearPanelSeleccion(), BorderLayout.NORTH);
        add(crearPanelTablas(),    BorderLayout.CENTER);
        add(crearPanelInferior(),  BorderLayout.SOUTH);

        cargarAlumnos();
    }

    // ===================================================================
    // ARRIBA: selector de alumno
    // ===================================================================
    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Selecciona un alumno"));
        panel.add(new JLabel("Alumno:"));
        panel.add(cmbAlumno);

        cmbAlumno.addActionListener(e -> cargarNotasDelAlumno());
        return panel;
    }

    // ===================================================================
    // CENTRO: 2 tablas (notas + desglose por criterio)
    // ===================================================================
    private JSplitPane crearPanelTablas() {
        // ---- Tabla de notas ----
        String[] colsNotas = {"Asignatura", "Actividad", "Total", "Nota final", "Fecha"};
        modeloNotas = new DefaultTableModel(colsNotas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaNotas = new JTable(modeloNotas);
        tablaNotas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaNotas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarDesgloseDeFilaSeleccionada();
        });
        JPanel pNotas = new JPanel(new BorderLayout());
        pNotas.setBorder(BorderFactory.createTitledBorder("Notas del alumno"));
        pNotas.add(new JScrollPane(tablaNotas), BorderLayout.CENTER);

        // ---- Tabla de desglose ----
        String[] colsDesg = {"Criterio", "Puntuación", "Comentario"};
        modeloDesglose = new DefaultTableModel(colsDesg, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDesglose = new JTable(modeloDesglose);
        JPanel pDesg = new JPanel(new BorderLayout());
        pDesg.setBorder(BorderFactory.createTitledBorder("Desglose por criterio (selecciona una nota arriba)"));
        pDesg.add(new JScrollPane(tablaDesglose), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pNotas, pDesg);
        split.setResizeWeight(0.5);
        split.setDividerLocation(280);
        return split;
    }

    // ===================================================================
    // ABAJO: media + botón cerrar
    // ===================================================================
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel resumen = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen"));
        JLabel et1 = new JLabel("Media de notas:");
        JLabel et2 = new JLabel("Suma ponderada:");
        et1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        et2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMedia.setForeground(new Color(70, 50, 180));
        lblMedia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPonderada.setForeground(new Color(70, 50, 180));
        lblPonderada.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resumen.add(et1); resumen.add(lblMedia);
        resumen.add(et2); resumen.add(lblPonderada);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCerrar);

        panel.add(resumen, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ===================================================================
    // LÓGICA
    // ===================================================================
    private void cargarAlumnos() {
        cmbAlumno.removeAllItems();
        for (Student s : studentDao.findAll()) cmbAlumno.addItem(s);
    }

    private void cargarNotasDelAlumno() {
        modeloNotas.setRowCount(0);
        modeloDesglose.setRowCount(0);

        Student alumno = (Student) cmbAlumno.getSelectedItem();
        if (alumno == null) return;

        List<StudentActivityGrade> notas = gradeDao.findByStudentId(alumno.getId());

        double sumaMedias = 0.0;
        double sumaPonderada = 0.0;
        int cuenta = 0;

        for (StudentActivityGrade nota : notas) {
            modeloNotas.addRow(new Object[]{
                    nota.getActivity().getSubject().getName(),
                    nota.getActivity().getName(),
                    formato(nota.getTotalScore()),
                    formato(nota.getFinalGrade()),
                    nota.getEvaluatedAt() == null ? "" : nota.getEvaluatedAt().toLocalDate().toString()
            });
            if (nota.getFinalGrade() != null) {
                sumaMedias += nota.getFinalGrade();
                cuenta++;
                sumaPonderada += gradingService.calculateWeightedGrade(nota);
            }
        }

        if (cuenta == 0) {
            lblMedia.setText("- (sin notas)");
            lblPonderada.setText("-");
        } else {
            lblMedia.setText(String.format("%.2f", sumaMedias / cuenta));
            lblPonderada.setText(String.format("%.2f", sumaPonderada));
        }
    }

    private void cargarDesgloseDeFilaSeleccionada() {
        modeloDesglose.setRowCount(0);
        int fila = tablaNotas.getSelectedRow();
        if (fila < 0) return;

        Student alumno = (Student) cmbAlumno.getSelectedItem();
        if (alumno == null) return;

        // Buscamos la nota de ese alumno en esa fila
        List<StudentActivityGrade> notas = gradeDao.findByStudentId(alumno.getId());
        if (fila >= notas.size()) return;

        StudentActivityGrade nota = notas.get(fila);
        List<StudentCriterionScore> desglose = scoreDao.findByGradeId(nota.getId());

        for (StudentCriterionScore s : desglose) {
            modeloDesglose.addRow(new Object[]{
                    s.getCriterion().getName(),
                    s.getScore() + " / " + s.getCriterion().getMaxScore(),
                    s.getComment() == null ? "" : s.getComment()
            });
        }
    }

    private String formato(Double d) {
        return d == null ? "-" : String.format("%.2f", d);
    }
}
