package client;

import javax.swing.*;

/**
 *
 */
public class DrawApp {

    public static void main(String[] args) {
        Runnable r = () -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // use default
            }

            DrawingPane drawApp = new DrawingPane();

            JFrame f = new JFrame("DrawApp");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLocationByPlatform(true);

            f.setContentPane(drawApp.getGui());

            f.pack();
            f.setMinimumSize(f.getSize());
            f.setVisible(true);
        };

        SwingUtilities.invokeLater(r);
    }
}
