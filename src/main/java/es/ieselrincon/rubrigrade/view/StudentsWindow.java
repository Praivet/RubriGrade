package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.StudentDao;
import es.ieselrincon.rubrigrade.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana CRUD para gestionar alumnos.
 * Abierta como diálogo modal desde la ventana principal.
 */
public class StudentsWindow extends JDialog {

    private final StudentDao dao = new StudentDao();

    // Componentes de la tabla
    private JTable tabla;
    private DefaultTableModel modelo;

    // Componentes del formulario
    private JTextField txtNombre   = new JTextField(20);
    private JTextField txtApellido = new JTextField(20);
    private JTextField txtEmail    = new JTextField(20);
    private JTextField txtNia      = new JTextField(20);

    public StudentsWindow(JFrame parent) {
        super(parent, "Gestión de Alumnos", true);   // true = modal
        setSize(800, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearTabla(),       BorderLayout.CENTER);
        add(crearFormulario(),  BorderLayout.SOUTH);

        cargarTabla();
    }

    // ===================================================================
    // TABLA
    // ===================================================================
    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Nombre", "Apellido", "Email", "NIA"};

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // La tabla es solo de lectura (se edita por el formulario)
            }
        };

        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) rellenarFormularioDesdeFila();
        });

        return new JScrollPane(tabla);
    }

    // ===================================================================
    // FORMULARIO + BOTONES
    // ===================================================================
    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del alumno"));

        // ----- Campos del formulario -----
        JPanel campos = new JPanel(new GridLayout(4, 2, 5, 5));
        campos.add(new JLabel("Nombre:"));     campos.add(txtNombre);
        campos.add(new JLabel("Apellido:"));   campos.add(txtApellido);
        campos.add(new JLabel("Email:"));      campos.add(txtEmail);
        campos.add(new JLabel("NIA / Nº matrícula:")); campos.add(txtNia);

        // ----- Botones -----
        JButton btnNuevo     = new JButton("Nuevo");
        JButton btnGuardar   = new JButton("Guardar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBorrar    = new JButton("Borrar");
        JButton btnCerrar    = new JButton("Cerrar");

        btnNuevo    .addActionListener(e -> limpiarFormulario());
        btnGuardar  .addActionListener(e -> guardar());
        btnModificar.addActionListener(e -> modificar());
        btnBorrar   .addActionListener(e -> borrar());
        btnCerrar   .addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnModificar);
        botones.add(btnBorrar);
        botones.add(btnCerrar);

        panel.add(campos,  BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ===================================================================
    // LÓGICA
    // ===================================================================

    /** Lee todos los alumnos de la BD y los pone en la tabla. */
    private void cargarTabla() {
        modelo.setRowCount(0);   // Vaciar
        List<Student> alumnos = dao.findAll();
        for (Student a : alumnos) {
            modelo.addRow(new Object[]{
                    a.getId(),
                    a.getFirstName(),
                    a.getLastName(),
                    a.getEmail(),
                    a.getEnrollmentNumber()
            });
        }
    }

    /** Cuando se hace click en una fila, rellena el formulario con esos datos. */
    private void rellenarFormularioDesdeFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        txtNombre  .setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtApellido.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtEmail   .setText(valorOVacio(modelo.getValueAt(fila, 3)));
        txtNia     .setText(valorOVacio(modelo.getValueAt(fila, 4)));
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        txtNia.setText("");
        txtNombre.requestFocus();
    }

    /** Crea un alumno nuevo a partir de los datos del formulario. */
    private void guardar() {
        if (!validarFormulario()) return;

        try {
            Student nuevo = new Student(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim(),
                    txtNia.getText().trim().isEmpty() ? null : txtNia.getText().trim()
            );
            dao.save(nuevo);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Alumno guardado correctamente.");
        } catch (Exception ex) {
            error("No se pudo guardar el alumno", ex);
        }
    }

    /** Actualiza el alumno seleccionado en la tabla. */
    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona un alumno de la tabla para modificarlo.", null);
            return;
        }
        if (!validarFormulario()) return;

        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            Student actual = dao.findById(id);
            if (actual == null) {
                error("El alumno ya no existe en la BD.", null);
                return;
            }
            actual.setFirstName(txtNombre.getText().trim());
            actual.setLastName(txtApellido.getText().trim());
            actual.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            actual.setEnrollmentNumber(txtNia.getText().trim().isEmpty() ? null : txtNia.getText().trim());
            dao.update(actual);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Alumno modificado correctamente.");
        } catch (Exception ex) {
            error("No se pudo modificar el alumno", ex);
        }
    }

    /** Borra el alumno seleccionado en la tabla. */
    private void borrar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona un alumno de la tabla para borrarlo.", null);
            return;
        }
        int op = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que quieres borrar este alumno?\nSe borrarán también todas sus notas.",
                "Confirmar borrado",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            dao.deleteById(id);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Alumno borrado correctamente.");
        } catch (Exception ex) {
            error("No se pudo borrar el alumno", ex);
        }
    }

    // ===================================================================
    // AUXILIARES
    // ===================================================================
    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            error("El nombre es obligatorio.", null);
            return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            error("El apellido es obligatorio.", null);
            return false;
        }
        return true;
    }

    private void error(String mensaje, Exception ex) {
        String texto = (ex == null) ? mensaje : mensaje + ":\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String valorOVacio(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}
