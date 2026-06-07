package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.ActivityDao;
import es.ieselrincon.rubrigrade.dao.StudentActivityGradeDao;
import es.ieselrincon.rubrigrade.dao.StudentDao;
import es.ieselrincon.rubrigrade.model.Activity;
import es.ieselrincon.rubrigrade.model.Student;
import es.ieselrincon.rubrigrade.model.StudentActivityGrade;
import es.ieselrincon.rubrigrade.service.GradingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Consulta de notas por actividad.
 * Muestra todos los alumnos evaluados (con su nota) y los pendientes (sin evaluar todavía).
 * Calcula la media de la actividad.
 */
public class QueryByActivityWindow extends JDialog {

    private final ActivityDao             activityDao = new ActivityDao();
    private final StudentDao              studentDao  = new StudentDao();
    private final StudentActivityGradeDao gradeDao    = new StudentActivityGradeDao();
    private final GradingService          gradingService = new GradingService();

    private JComboBox<Activity> cmbActividad = new JComboBox<>();

    private JTable tablaEvaluados;
    private DefaultTableModel modeloEvaluados;

    private JTable tablaPendientes;
    private DefaultTableModel modeloPendientes;

    private JLabel lblMedia      = new JLabel("-");
    private JLabel lblEvaluados  = new JLabel("0");
    private JLabel lblPendientes = new JLabel("0");

    public QueryByActivityWindow(JFrame parent) {
        super(parent, "Consulta de notas por actividad", true);
        setSize(900, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearPanelSeleccion(), BorderLayout.NORTH);
        add(crearPanelTablas(),    BorderLayout.CENTER);
        add(crearPanelInferior(),  BorderLayout.SOUTH);

        cargarActividades();
    }

    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Selecciona una actividad"));
        panel.add(new JLabel("Actividad:"));
        panel.add(cmbActividad);
        cmbActividad.addActionListener(e -> cargarDatosDeActividad());
        return panel;
    }

    private JSplitPane crearPanelTablas() {
        //Tabla evaluados
        String[] colsEval = {"Alumno", "NIA", "Total", "Nota final"};
        modeloEvaluados = new DefaultTableModel(colsEval, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEvaluados = new JTable(modeloEvaluados);
        JPanel pEval = new JPanel(new BorderLayout());
        pEval.setBorder(BorderFactory.createTitledBorder("Alumnos evaluados"));
        pEval.add(new JScrollPane(tablaEvaluados), BorderLayout.CENTER);

        // Tabla pendientes
        String[] colsPend = {"Alumno", "NIA", "Email"};
        modeloPendientes = new DefaultTableModel(colsPend, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPendientes = new JTable(modeloPendientes);
        JPanel pPend = new JPanel(new BorderLayout());
        pPend.setBorder(BorderFactory.createTitledBorder("Alumnos pendientes de evaluar"));
        pPend.add(new JScrollPane(tablaPendientes), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pEval, pPend);
        split.setResizeWeight(0.5);
        split.setDividerLocation(440);
        return split;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel resumen = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen"));

        JLabel et1 = new JLabel("Media:");
        JLabel et2 = new JLabel("Evaluados:");
        JLabel et3 = new JLabel("Pendientes:");
        for (JLabel l : new JLabel[]{et1, et2, et3}) {
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        for (JLabel l : new JLabel[]{lblMedia, lblEvaluados, lblPendientes}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l.setForeground(new Color(70, 50, 180));
        }
        resumen.add(et1); resumen.add(lblMedia);
        resumen.add(et2); resumen.add(lblEvaluados);
        resumen.add(et3); resumen.add(lblPendientes);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCerrar);

        panel.add(resumen, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }


    // LÓGICA

    private void cargarActividades() {
        cmbActividad.removeAllItems();
        for (Activity a : activityDao.findAll()) cmbActividad.addItem(a);
    }

    private void cargarDatosDeActividad() {
        modeloEvaluados.setRowCount(0);
        modeloPendientes.setRowCount(0);

        Activity act = (Activity) cmbActividad.getSelectedItem();
        if (act == null) {
            lblMedia.setText("-"); lblEvaluados.setText("0"); lblPendientes.setText("0");
            return;
        }

        // Evaluados
        List<StudentActivityGrade> notas = gradeDao.findByActivityId(act.getId());
        Set<Integer> idsEvaluados = new HashSet<>();
        for (StudentActivityGrade nota : notas) {
            modeloEvaluados.addRow(new Object[]{
                    nota.getStudent().getFirstName() + " " + nota.getStudent().getLastName(),
                    nota.getStudent().getEnrollmentNumber(),
                    formato(nota.getTotalScore()),
                    formato(nota.getFinalGrade())
            });
            idsEvaluados.add(nota.getStudent().getId());
        }

        //Pendientes = todos los alumnos - los ya evaluados
        List<Student> pendientes = new ArrayList<>();
        for (Student s : studentDao.findAll()) {
            if (!idsEvaluados.contains(s.getId())) pendientes.add(s);
        }
        for (Student s : pendientes) {
            modeloPendientes.addRow(new Object[]{
                    s.getFirstName() + " " + s.getLastName(),
                    s.getEnrollmentNumber(),
                    s.getEmail()
            });
        }

        //Resumen
        double media = gradingService.calculateActivityAverage(act.getId());
        lblMedia.setText(notas.isEmpty() ? "- (sin notas)" : String.format("%.2f", media));
        lblEvaluados.setText(String.valueOf(notas.size()));
        lblPendientes.setText(String.valueOf(pendientes.size()));
    }

    private String formato(Double d) {
        return d == null ? "-" : String.format("%.2f", d);
    }
}
