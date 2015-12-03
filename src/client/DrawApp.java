package client;

import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
            JFrame f = new JFrame("DrawApp");

            DrawingPane drawApp = new DrawingPane();
            ActionListener create = event -> {
            	f.setContentPane(drawApp.getGui());
            	f.revalidate();
            	f.repaint();
            };
            ActionListener join = event -> {
            	f.setContentPane(drawApp.getGui());
            	f.revalidate();
            	f.repaint();
            };
            MainMenu mainMenu = new MainMenu(create, join);
            
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLocationByPlatform(true);

            f.setContentPane(drawApp.getGui());
            f.pack();
            f.setMinimumSize(f.getSize());
            
            f.setContentPane(mainMenu.getGui());

            f.setVisible(true);
        };

        SwingUtilities.invokeLater(r);
    }
}
