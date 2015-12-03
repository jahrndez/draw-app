package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class Lobby implements GameScreen {
    private JPanel gui;

	@Override
	public JComponent getGui() {
		if (gui == null) {
            gui = new JPanel(new GridBagLayout());
        	GridBagConstraints gbc = new GridBagConstraints();
            
            JPanel buttons = new JPanel();
            gui.add(buttons, gbc);
        }

        return gui;
    }

}
