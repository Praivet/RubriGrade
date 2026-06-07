package es.ieselrincon.rubrigrade;

import es.ieselrincon.rubrigrade.view.MainWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Intentar usar el look del sistema (Windows nativo)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Arrancar la GUI en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            MainWindow ventana = new MainWindow();
            ventana.setVisible(true);
        });
    }
}
