package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.ActivityDao;
import es.ieselrincon.rubrigrade.dao.RubricDao;
import es.ieselrincon.rubrigrade.dao.SubjectDao;
import es.ieselrincon.rubrigrade.model.Activity;
import es.ieselrincon.rubrigrade.model.Rubric;
import es.ieselrincon.rubrigrade.model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * Ventana CRUD para gestionar actividades.
 * Cada actividad va asociada a una asignatura y a una rúbrica (desplegables).
 */
public class ActivitiesWindow extends JDialog {

    private final ActivityDao activityDao = new ActivityDao();
    private final SubjectDao  subjectDao  = new SubjectDao();
    private final RubricDao   rubricDao   = new RubricDao();

    // Tabla
    private JTable             tabla;
    private DefaultTableModel  modelo;

    // Formulario
    private JTextField  txtNombre   = new JTextField(20);
    private JTextField  txtMax      = new JTextField(5);
    private JTextField  txtPeso     = new JTextField(5);
    private JTextField  txtFecha    = new JTextField(10);   // yyyy-MM-dd
    private JTextArea   txtDesc     = new JTextArea(3, 20);

    // Desplegables
    private JComboBox<Subject> cmbAsignatura = new JComboBox<>();
    private JComboBox<Rubric>  cmbRubrica    = new JComboBox<>();

    public ActivitiesWindow(JFrame parent) {
        super(parent, "Gestión de Actividades", true);
        setSize(950, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(crearTabla(),       BorderLayout.CENTER);
        add(crearFormulario(),  BorderLayout.SOUTH);

        cargarComboboxes();
        cargarTabla();
    }

    // TABLA
    private JScrollPane crearTabla() {
        String[] cols = {"ID", "Nombre", "Asignatura", "Rúbrica", "Max", "Peso", "Fecha"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
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
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la actividad"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; form.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1; form.add(new JLabel("Asignatura:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; form.add(cmbAsignatura, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.gridwidth = 1; form.add(new JLabel("Rúbrica:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; form.add(cmbRubrica, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1; form.add(new JLabel("Nota máx:"), gbc);
        gbc.gridx = 1; form.add(txtMax, gbc);
        gbc.gridx = 2; form.add(new JLabel("Peso (%):"), gbc);
        gbc.gridx = 3; form.add(txtPeso, gbc);

        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Fecha (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; form.add(txtFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.gridwidth = 1;
        form.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDesc), gbc);

        // Botones
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

        panel.add(form,    BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }


    // CARGAS INICIALES

    private void cargarComboboxes() {
        cmbAsignatura.removeAllItems();
        for (Subject s : subjectDao.findAll()) cmbAsignatura.addItem(s);

        cmbRubrica.removeAllItems();
        for (Rubric r : rubricDao.findAll()) cmbRubrica.addItem(r);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Activity a : activityDao.findAll()) {
            modelo.addRow(new Object[]{
                    a.getId(),
                    a.getName(),
                    a.getSubject() == null ? "" : a.getSubject().getName(),
                    a.getRubric()  == null ? "" : a.getRubric().getName(),
                    a.getMaxScore(),
                    a.getWeight(),
                    a.getDueDate() == null ? "" : a.getDueDate().toString()
            });
        }
    }

    private void rellenarFormularioDesdeFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Integer id = (Integer) modelo.getValueAt(fila, 0);
        Activity a = activityDao.findById(id);
        if (a == null) return;

        txtNombre.setText(valorOVacio(a.getName()));
        txtMax   .setText(valorOVacio(a.getMaxScore()));
        txtPeso  .setText(valorOVacio(a.getWeight()));
        txtFecha .setText(a.getDueDate() == null ? "" : a.getDueDate().toString());
        txtDesc  .setText(valorOVacio(a.getDescription()));

        seleccionarEnCombo(cmbAsignatura, a.getSubject() == null ? null : a.getSubject().getId());
        seleccionarEnCombo(cmbRubrica,    a.getRubric()  == null ? null : a.getRubric().getId());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtNombre.setText("");
        txtMax.setText("10.0");
        txtPeso.setText("100.0");
        txtFecha.setText("");
        txtDesc.setText("");
        if (cmbAsignatura.getItemCount() > 0) cmbAsignatura.setSelectedIndex(0);
        if (cmbRubrica.getItemCount()    > 0) cmbRubrica.setSelectedIndex(0);
        txtNombre.requestFocus();
    }

    // CRUD

    private void guardar() {
        if (!validarFormulario()) return;
        try {
            Subject asig = (Subject) cmbAsignatura.getSelectedItem();
            Rubric  rub  = (Rubric)  cmbRubrica.getSelectedItem();

            Activity nueva = new Activity(
                    asig, rub,
                    txtNombre.getText().trim(),
                    parseDouble(txtMax.getText(), 10.0),
                    parseFecha(txtFecha.getText())
            );
            nueva.setWeight(parseDouble(txtPeso.getText(), 100.0));
            nueva.setDescription(txtDesc.getText().trim().isEmpty() ? null : txtDesc.getText().trim());

            activityDao.save(nueva);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Actividad guardada correctamente.");
        } catch (Exception ex) {
            error("No se pudo guardar la actividad", ex);
        }
    }

    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { error("Selecciona una actividad para modificarla.", null); return; }
        if (!validarFormulario()) return;
        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            Activity a = activityDao.findById(id);
            if (a == null) { error("La actividad ya no existe.", null); return; }

            a.setSubject((Subject) cmbAsignatura.getSelectedItem());
            a.setRubric ((Rubric)  cmbRubrica.getSelectedItem());
            a.setName(txtNombre.getText().trim());
            a.setMaxScore(parseDouble(txtMax.getText(), 10.0));
            a.setWeight(parseDouble(txtPeso.getText(), 100.0));
            a.setDueDate(parseFecha(txtFecha.getText()));
            a.setDescription(txtDesc.getText().trim().isEmpty() ? null : txtDesc.getText().trim());

            activityDao.update(a);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Actividad modificada correctamente.");
        } catch (Exception ex) {
            error("No se pudo modificar la actividad", ex);
        }
    }

    private void borrar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { error("Selecciona una actividad para borrarla.", null); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Borrar esta actividad?\nSe borrarán también las notas asociadas.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            Integer id = (Integer) modelo.getValueAt(fila, 0);
            activityDao.deleteById(id);
            cargarTabla();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Actividad borrada correctamente.");
        } catch (Exception ex) {
            error("No se pudo borrar la actividad", ex);
        }
    }

    // AUXILIARES

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            error("El nombre es obligatorio.", null);
            return false;
        }
        if (cmbAsignatura.getSelectedItem() == null) {
            error("Debes elegir una asignatura. Crea una si no existe.", null);
            return false;
        }
        if (cmbRubrica.getSelectedItem() == null) {
            error("Debes elegir una rúbrica. Crea una si no existe.", null);
            return false;
        }
        return true;
    }

    // Selecciona un item de un JComboBox buscando por id (no por referencia).

    private <T> void seleccionarEnCombo(JComboBox<T> combo, Integer id) {
        if (id == null) { combo.setSelectedIndex(-1); return; }
        for (int i = 0; i < combo.getItemCount(); i++) {
            T item = combo.getItemAt(i);
            try {
                Integer itemId = (Integer) item.getClass().getMethod("getId").invoke(item);
                if (id.equals(itemId)) { combo.setSelectedIndex(i); return; }
            } catch (Exception ignored) { }
        }
    }

    private Double parseDouble(String s, double porDefecto) {
        if (s == null || s.trim().isEmpty()) return porDefecto;
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    private LocalDate parseFecha(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return LocalDate.parse(s.trim());   // formato esperado: yyyy-MM-dd
    }

    private void error(String mensaje, Exception ex) {
        String texto = (ex == null) ? mensaje : mensaje + ":\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String valorOVacio(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}
