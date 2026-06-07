package es.ieselrincon.rubrigrade.view;

import es.ieselrincon.rubrigrade.dao.RubricCriterionDao;
import es.ieselrincon.rubrigrade.dao.RubricDao;
import es.ieselrincon.rubrigrade.model.Rubric;
import es.ieselrincon.rubrigrade.model.RubricCriterion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana CRUD para gestionar rúbricas y sus criterios.
 * Patrón master-detail: arriba se eligen las rúbricas, abajo aparecen sus criterios.
 */
public class RubricsWindow extends JDialog {

    private final RubricDao          rubricDao    = new RubricDao();
    private final RubricCriterionDao criterionDao = new RubricCriterionDao();

    // -------- Componentes de la zona RÚBRICAS --------
    private JTable             tablaRubricas;
    private DefaultTableModel  modeloRubricas;
    private JTextField  txtNombreR  = new JTextField(20);
    private JTextField  txtMaxR     = new JTextField(5);
    private JTextArea   txtDescR    = new JTextArea(2, 20);

    // -------- Componentes de la zona CRITERIOS --------
    private JTable             tablaCriterios;
    private DefaultTableModel  modeloCriterios;
    private JTextField  txtNombreC  = new JTextField(20);
    private JTextField  txtMaxC     = new JTextField(5);
    private JTextField  txtOrdenC   = new JTextField(5);
    private JTextArea   txtDescC    = new JTextArea(2, 20);

    public RubricsWindow(JFrame parent) {
        super(parent, "Gestión de Rúbricas y Criterios", true);
        setSize(950, 800);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Dividimos la ventana en arriba (rúbricas) y abajo (criterios)
        JPanel norte = crearPanelRubricas();
        JPanel sur   = crearPanelCriterios();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, norte, sur);
        split.setResizeWeight(0.5);
        split.setDividerLocation(380);

        add(split, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.add(btnCerrar);
        add(pie, BorderLayout.SOUTH);

        cargarRubricas();
    }

    // ===================================================================
    // PANEL DE RÚBRICAS (master)
    // ===================================================================
    private JPanel crearPanelRubricas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Rúbricas"));

        // ---- Tabla ----
        String[] cols = {"ID", "Nombre", "Nota máxima", "Descripción"};
        modeloRubricas = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaRubricas = new JTable(modeloRubricas);
        tablaRubricas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRubricas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                rellenarFormRubrica();
                cargarCriteriosDeRubricaSeleccionada();
            }
        });

        panel.add(new JScrollPane(tablaRubricas), BorderLayout.CENTER);

        // ---- Formulario ----
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(txtNombreR, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; form.add(new JLabel("Nota máxima:"), gbc);
        gbc.gridx = 1; form.add(txtMaxR, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        txtDescR.setLineWrap(true);
        txtDescR.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescR), gbc);

        // ---- Botones ----
        JButton btnNueva     = new JButton("Nueva");
        JButton btnGuardar   = new JButton("Guardar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBorrar    = new JButton("Borrar");

        btnNueva    .addActionListener(e -> limpiarFormRubrica());
        btnGuardar  .addActionListener(e -> guardarRubrica());
        btnModificar.addActionListener(e -> modificarRubrica());
        btnBorrar   .addActionListener(e -> borrarRubrica());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnNueva); botones.add(btnGuardar);
        botones.add(btnModificar); botones.add(btnBorrar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form,    BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    // ===================================================================
    // PANEL DE CRITERIOS (detail)
    // ===================================================================
    private JPanel crearPanelCriterios() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Criterios de la rúbrica seleccionada"));

        // ---- Tabla ----
        String[] cols = {"ID", "Nombre", "Max", "Orden", "Descripción"};
        modeloCriterios = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCriterios = new JTable(modeloCriterios);
        tablaCriterios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCriterios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) rellenarFormCriterio();
        });

        panel.add(new JScrollPane(tablaCriterios), BorderLayout.CENTER);

        // ---- Formulario ----
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; form.add(txtNombreC, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        form.add(new JLabel("Nota máx:"), gbc);
        gbc.gridx = 1; form.add(txtMaxC, gbc);
        gbc.gridx = 2; form.add(new JLabel("Orden:"), gbc);
        gbc.gridx = 3; form.add(txtOrdenC, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        txtDescC.setLineWrap(true);
        txtDescC.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescC), gbc);

        // ---- Botones ----
        JButton btnNuevo     = new JButton("Nuevo criterio");
        JButton btnGuardar   = new JButton("Guardar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBorrar    = new JButton("Borrar");

        btnNuevo    .addActionListener(e -> limpiarFormCriterio());
        btnGuardar  .addActionListener(e -> guardarCriterio());
        btnModificar.addActionListener(e -> modificarCriterio());
        btnBorrar   .addActionListener(e -> borrarCriterio());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnNuevo); botones.add(btnGuardar);
        botones.add(btnModificar); botones.add(btnBorrar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form,    BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    // ===================================================================
    // LÓGICA RÚBRICAS
    // ===================================================================
    private void cargarRubricas() {
        modeloRubricas.setRowCount(0);
        for (Rubric r : rubricDao.findAll()) {
            modeloRubricas.addRow(new Object[]{
                    r.getId(), r.getName(), r.getMaxScore(), r.getDescription()
            });
        }
        modeloCriterios.setRowCount(0);   // Vaciar criterios al recargar rúbricas
    }

    private void rellenarFormRubrica() {
        int fila = tablaRubricas.getSelectedRow();
        if (fila < 0) return;
        txtNombreR.setText(valorOVacio(modeloRubricas.getValueAt(fila, 1)));
        txtMaxR   .setText(valorOVacio(modeloRubricas.getValueAt(fila, 2)));
        txtDescR  .setText(valorOVacio(modeloRubricas.getValueAt(fila, 3)));
    }

    private void limpiarFormRubrica() {
        tablaRubricas.clearSelection();
        txtNombreR.setText("");
        txtMaxR.setText("10.0");
        txtDescR.setText("");
        txtNombreR.requestFocus();
    }

    private void guardarRubrica() {
        if (txtNombreR.getText().trim().isEmpty()) {
            error("El nombre de la rúbrica es obligatorio.", null);
            return;
        }
        try {
            Rubric nueva = new Rubric(
                    txtNombreR.getText().trim(),
                    txtDescR.getText().trim().isEmpty() ? null : txtDescR.getText().trim(),
                    parseDouble(txtMaxR.getText(), 10.0)
            );
            rubricDao.save(nueva);
            cargarRubricas();
            limpiarFormRubrica();
        } catch (Exception ex) {
            error("No se pudo guardar la rúbrica", ex);
        }
    }

    private void modificarRubrica() {
        int fila = tablaRubricas.getSelectedRow();
        if (fila < 0) { error("Selecciona una rúbrica.", null); return; }
        if (txtNombreR.getText().trim().isEmpty()) {
            error("El nombre es obligatorio.", null); return;
        }
        try {
            Integer id = (Integer) modeloRubricas.getValueAt(fila, 0);
            Rubric r = rubricDao.findById(id);
            if (r == null) { error("La rúbrica ya no existe.", null); return; }
            r.setName(txtNombreR.getText().trim());
            r.setDescription(txtDescR.getText().trim().isEmpty() ? null : txtDescR.getText().trim());
            r.setMaxScore(parseDouble(txtMaxR.getText(), 10.0));
            rubricDao.update(r);
            cargarRubricas();
        } catch (Exception ex) {
            error("No se pudo modificar la rúbrica", ex);
        }
    }

    private void borrarRubrica() {
        int fila = tablaRubricas.getSelectedRow();
        if (fila < 0) { error("Selecciona una rúbrica.", null); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Borrar esta rúbrica?\nSe borrarán también sus criterios.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            Integer id = (Integer) modeloRubricas.getValueAt(fila, 0);
            rubricDao.deleteById(id);
            cargarRubricas();
            limpiarFormRubrica();
        } catch (Exception ex) {
            error("No se pudo borrar la rúbrica", ex);
        }
    }

    // ===================================================================
    // LÓGICA CRITERIOS
    // ===================================================================
    private Integer getRubricaIdSeleccionada() {
        int fila = tablaRubricas.getSelectedRow();
        if (fila < 0) return null;
        return (Integer) modeloRubricas.getValueAt(fila, 0);
    }

    private void cargarCriteriosDeRubricaSeleccionada() {
        modeloCriterios.setRowCount(0);
        Integer rubricId = getRubricaIdSeleccionada();
        if (rubricId == null) return;
        List<RubricCriterion> criterios = criterionDao.findByRubricId(rubricId);
        for (RubricCriterion c : criterios) {
            modeloCriterios.addRow(new Object[]{
                    c.getId(), c.getName(), c.getMaxScore(), c.getDisplayOrder(), c.getDescription()
            });
        }
    }

    private void rellenarFormCriterio() {
        int fila = tablaCriterios.getSelectedRow();
        if (fila < 0) return;
        txtNombreC.setText(valorOVacio(modeloCriterios.getValueAt(fila, 1)));
        txtMaxC   .setText(valorOVacio(modeloCriterios.getValueAt(fila, 2)));
        txtOrdenC .setText(valorOVacio(modeloCriterios.getValueAt(fila, 3)));
        txtDescC  .setText(valorOVacio(modeloCriterios.getValueAt(fila, 4)));
    }

    private void limpiarFormCriterio() {
        tablaCriterios.clearSelection();
        txtNombreC.setText("");
        txtMaxC.setText("1.0");
        txtOrdenC.setText("0");
        txtDescC.setText("");
        txtNombreC.requestFocus();
    }

    private void guardarCriterio() {
        Integer rubricId = getRubricaIdSeleccionada();
        if (rubricId == null) {
            error("Primero selecciona una rúbrica para añadirle un criterio.", null);
            return;
        }
        if (txtNombreC.getText().trim().isEmpty()) {
            error("El nombre del criterio es obligatorio.", null);
            return;
        }
        try {
            Rubric rubricaSeleccionada = rubricDao.findById(rubricId);
            RubricCriterion nuevo = new RubricCriterion(
                    rubricaSeleccionada,
                    txtNombreC.getText().trim(),
                    parseDouble(txtMaxC.getText(), 1.0),
                    parseDouble(txtOrdenC.getText(), 0.0).intValue()
            );
            nuevo.setDescription(txtDescC.getText().trim().isEmpty() ? null : txtDescC.getText().trim());
            criterionDao.save(nuevo);
            cargarCriteriosDeRubricaSeleccionada();
            limpiarFormCriterio();
        } catch (Exception ex) {
            error("No se pudo guardar el criterio", ex);
        }
    }

    private void modificarCriterio() {
        int fila = tablaCriterios.getSelectedRow();
        if (fila < 0) { error("Selecciona un criterio.", null); return; }
        if (txtNombreC.getText().trim().isEmpty()) {
            error("El nombre es obligatorio.", null); return;
        }
        try {
            Integer id = (Integer) modeloCriterios.getValueAt(fila, 0);
            RubricCriterion c = criterionDao.findById(id);
            if (c == null) { error("El criterio ya no existe.", null); return; }
            c.setName(txtNombreC.getText().trim());
            c.setMaxScore(parseDouble(txtMaxC.getText(), 1.0));
            c.setDisplayOrder(parseDouble(txtOrdenC.getText(), 0.0).intValue());
            c.setDescription(txtDescC.getText().trim().isEmpty() ? null : txtDescC.getText().trim());
            criterionDao.update(c);
            cargarCriteriosDeRubricaSeleccionada();
        } catch (Exception ex) {
            error("No se pudo modificar el criterio", ex);
        }
    }

    private void borrarCriterio() {
        int fila = tablaCriterios.getSelectedRow();
        if (fila < 0) { error("Selecciona un criterio.", null); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Borrar este criterio?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            Integer id = (Integer) modeloCriterios.getValueAt(fila, 0);
            criterionDao.deleteById(id);
            cargarCriteriosDeRubricaSeleccionada();
            limpiarFormCriterio();
        } catch (Exception ex) {
            error("No se pudo borrar el criterio", ex);
        }
    }

    // ===================================================================
    // AUXILIARES
    // ===================================================================
    /** Parsea un texto a Double admitiendo coma o punto. Si está vacío, devuelve el valor por defecto. */
    private Double parseDouble(String s, double porDefecto) {
        if (s == null || s.trim().isEmpty()) return porDefecto;
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    private void error(String mensaje, Exception ex) {
        String texto = (ex == null) ? mensaje : mensaje + ":\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String valorOVacio(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}
