package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.util.HibernateUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación RubriGrade.
 * Contiene la barra de menú desde la que se accede a todas las funcionalidades.
 */
public class MainWindow extends JFrame {

    public MainWindow() {
        //Configuración básica de la ventana
        setTitle("RubriGrade - Verificador de Rúbricas y Notas");
        setSize(900, 600);
        setLocationRelativeTo(null);       // Centrar en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //Componentes
        setJMenuBar(crearMenu());
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearPiePagina(),    BorderLayout.SOUTH);
    }


    // BARRA DE MENÚ
    private JMenuBar crearMenu() {
        JMenuBar menuBar = new JMenuBar();

        //  Menú GESTIÓN
        JMenu mGestion = new JMenu("Gestión");

        JMenuItem itAlumnos      = new JMenuItem("Alumnos");
        JMenuItem itAsignaturas  = new JMenuItem("Asignaturas");
        JMenuItem itRubricas     = new JMenuItem("Rúbricas y Criterios");
        JMenuItem itActividades  = new JMenuItem("Actividades");

        itAlumnos    .addActionListener(e -> abrirVentanaAlumnos());
        itAsignaturas.addActionListener(e -> abrirVentanaAsignaturas());
        itRubricas   .addActionListener(e -> abrirVentanaRubricas());
        itActividades.addActionListener(e -> abrirVentanaActividades());

        mGestion.add(itAlumnos);
        mGestion.add(itAsignaturas);
        mGestion.add(itRubricas);
        mGestion.add(itActividades);

        // Menú EVALUACIÓN
        JMenu mEvaluacion = new JMenu("Evaluación");
        JMenuItem itEvaluar = new JMenuItem("Evaluar alumno");
        itEvaluar.addActionListener(e -> abrirVentanaEvaluacion());
        mEvaluacion.add(itEvaluar);

        //  Menú CONSULTAS
        JMenu mConsultas = new JMenu("Consultas");
        JMenuItem itNotasAlumno    = new JMenuItem("Notas por alumno");
        JMenuItem itNotasActividad = new JMenuItem("Notas por actividad");

        itNotasAlumno   .addActionListener(e -> abrirConsultaNotasAlumno());
        itNotasActividad.addActionListener(e -> abrirConsultaNotasActividad());

        mConsultas.add(itNotasAlumno);
        mConsultas.add(itNotasActividad);

        // Menú SALIR
        JMenu mSalir = new JMenu("Salir");
        JMenuItem itSalir = new JMenuItem("Cerrar aplicación");
        itSalir.addActionListener(e -> salir());
        mSalir.add(itSalir);

        // Añadir todos los menús a la barra
        menuBar.add(mGestion);
        menuBar.add(mEvaluacion);
        menuBar.add(mConsultas);
        menuBar.add(mSalir);

        return menuBar;
    }

    // PANEL CENTRAL (pantalla de bienvenida)
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 251));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titulo = new JLabel("RubriGrade");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titulo.setForeground(new Color(70, 50, 180));

        JLabel subtitulo = new JLabel("Verificador de Rúbricas y Notas");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitulo.setForeground(new Color(90, 90, 90));

        JLabel ayuda = new JLabel("Selecciona una opción del menú superior para empezar");
        ayuda.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        ayuda.setForeground(new Color(120, 120, 120));

        gbc.gridy = 0; panel.add(titulo,    gbc);
        gbc.gridy = 1; panel.add(subtitulo, gbc);
        gbc.gridy = 2; panel.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 3; panel.add(ayuda,     gbc);

        return panel;
    }

    //
    // PIE DE PÁGINA
    //
    private JPanel crearPiePagina() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pie.setBackground(new Color(70, 50, 180));
        JLabel info = new JLabel("Proyecto Final UT8 · 1.º DAW · IES El Rincón · Curso 2025/26");
        info.setForeground(Color.WHITE);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pie.add(info);
        return pie;
    }

    // ACCIONES DEL MENÚ (de momento solo muestran un aviso)

    private void abrirVentanaAlumnos() {
        new StudentsWindow(this).setVisible(true);
    }

    private void abrirVentanaAsignaturas() {
        new SubjectsWindow(this).setVisible(true);
    }

    private void abrirVentanaRubricas() {
        new RubricsWindow(this).setVisible(true);
    }

    private void abrirVentanaActividades() {
        new ActivitiesWindow(this).setVisible(true);
    }

    private void abrirVentanaEvaluacion() {
        new EvaluationWindow(this).setVisible(true);
    }

    private void abrirConsultaNotasAlumno() {
        new QueryByStudentWindow(this).setVisible(true);
    }

    private void abrirConsultaNotasActividad() {
        new QueryByActivityWindow(this).setVisible(true);
    }

    private void mensajePendiente(String nombreVentana) {
        JOptionPane.showMessageDialog(
                this,
                "La ventana '" + nombreVentana + "' aún no está implementada.",
                "Próximamente",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        int op = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que quieres cerrar la aplicación?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            HibernateUtil.shutdown();
            System.exit(0);
        }
    }
}
