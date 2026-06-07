package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.SubjectDao;
import es.ieselrincon.rubrigrade.model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana CRUD para gestionar asignaturas.
 */
public class SubjectsWindow extends JDialog {

    private final SubjectDao dao = new SubjectDao();

    private JTable tabla;
    private DefaultTableModel modelo;

    private JTextField txtCodigo       = new JTextField(20);
    private JTextField txtNombre       = new JTextField(20);
    private JTextField txtCurso        = new JTextField(20);
    private JTextArea  txtDescripcion  = new JTextArea(3, 20);

    public SubjectsWindow(JFrame parent) {
        super(parent, "Gestión de Asignaturas", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearTabla(),      BorderLayout.CENTER);
        add(crearFormulario(), BorderLayout.SOUTH);

        cargarTabla();
    }


    // TABLA

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Código", "Nombre", "Curso", "Descripción"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) rellenarFormularioDesdeFila();
        });
        return new JScrollPane(tabla);
    }


    // FORMULARIO

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la asignatura"));

        JPanel campos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; campos.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; campos.add(txtCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; campos.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; campos.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; campos.add(new JLabel("Curso académico:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; campos.add(txtCurso, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        campos.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        campos.add(new JScrollPane(txtDescripcion), gbc);

        //Botones
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


    // LÓGICA

    private void cargarTabla() {
        modelo.setRowCount(0);
        List<Subject> asignaturas = dao.findAll();
        for (Subject s : asignaturas) {
            modelo.addRow(new Object[]{
                    s.getId(),
                    s.getCode(),
                    s.getName(),
                    s.getAcademicYear(),
                    s.getDescription()
            });
        }
    }

    private void rellenarFormularioDesdeFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        txtCodigo.setText(valorOVacio(modelo.getValueAt(fila, 1)));
        txtNombre.setText(valorOVacio(modelo.getValueAt(fila, 2)));
        txtCurso .setText(valorOVacio(modelo.getValueAt(fila, 3)));
        txtDescripcion.setText(valorOVacio(modelo.getValueAt(fila, 4)));
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtCurso.setText("");
        txtDescripcion.setText("");
        txtCodigo.requestFocus();
    }

    private void guardar() {
        if (!validarFormulario()) return;
        try {
            Subject nueva = new Subject(
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim().isEmpty() ? null : txtDescripcion.getText().trim(),
                    txtCurso.getText().trim().isEmpty() ? null : txtCurso.getText().trim()
            );
            dao.save(nueva);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Asignatura guardada correctamente.");
        } catch (Exception ex) {
            error("No se pudo guardar la asignatura", ex);
        }
    }

    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona una asignatura de la tabla para modificarla.", null);
            return;
        }
        if (!validarFormulario()) return;
        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            Subject actual = dao.findById(id);
            if (actual == null) {
                error("La asignatura ya no existe en la BD.", null);
                return;
            }
            actual.setCode(txtCodigo.getText().trim());
            actual.setName(txtNombre.getText().trim());
            actual.setDescription(txtDescripcion.getText().trim().isEmpty() ? null : txtDescripcion.getText().trim());
            actual.setAcademicYear(txtCurso.getText().trim().isEmpty() ? null : txtCurso.getText().trim());
            dao.update(actual);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Asignatura modificada correctamente.");
        } catch (Exception ex) {
            error("No se pudo modificar la asignatura", ex);
        }
    }

    private void borrar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona una asignatura de la tabla para borrarla.", null);
            return;
        }
        int op = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que quieres borrar esta asignatura?\nSe borrarán también sus actividades.",
                "Confirmar borrado",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            dao.deleteById(id);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Asignatura borrada correctamente.");
        } catch (Exception ex) {
            error("No se pudo borrar la asignatura", ex);
        }
    }


    // AUXILIARES

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty()) {
            error("El código es obligatorio (ej: DAW-PROG).", null);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            error("El nombre es obligatorio.", null);
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
