package client;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeListener;

/**
 * Serves as the pane on which 2D graphics will be displayed.
 */
public class DrawingPane {

    /** Image used to make changes. */
    private BufferedImage canvasImage;
    /** The main GUI that might be added to a frame or applet. */
    private JPanel gui;
    /** The color to use when calling clear, text or other
     * drawing functionality. */
    private Color color = Color.WHITE;
    /** General user messages. */
    private JLabel output = new JLabel("DrawApp");

    private BufferedImage colorSample = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

    private JLabel imageLabel;
    private int activeTool;
    public static final int DRAW_TOOL = 0;

    private boolean dirty = false;
    private Stroke stroke = new BasicStroke(
            3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);
    private RenderingHints renderingHints;

    private Point lastPoint1;
    private Point lastPoint2;

    private static State STATE;

    enum State {
        DRAWING,
        GUESSING,
        NO_GAME     // Game hasn't started yet or in between games
    }

    public JComponent getGui() {
        if (gui == null) {
            Map<Key, Object> hintsMap = new HashMap<>();
            hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            renderingHints = new RenderingHints(hintsMap);

            setImage(new BufferedImage(960, 640, BufferedImage.TYPE_INT_RGB));
            gui = new JPanel(new BorderLayout(4,4));
            gui.setBorder(new EmptyBorder(5, 3, 5, 3));

            JPanel imageView = new JPanel(new GridBagLayout());
            imageView.setPreferredSize(new Dimension(1000,700));
            imageLabel = new JLabel(new ImageIcon(canvasImage));
            JScrollPane imageScroll = new JScrollPane(imageView);
            imageView.add(imageLabel);
            imageLabel.addMouseMotionListener(new ImageMouseMotionListener());
            imageLabel.addMouseListener(new ImageMouseListener());
            gui.add(imageScroll, BorderLayout.CENTER);

            JToolBar tb = new JToolBar();
            tb.setFloatable(false);
            JButton colorButton = new JButton("Color");
            colorButton.setToolTipText("Choose a Color");

            ActionListener colorListener = event -> {
                Color c = JColorChooser.showDialog(
                        gui, "Choose a color", color);
                if (c != null) {
                    setColor(c);
                }
            };

            colorButton.addActionListener(colorListener);
            colorButton.setIcon(new ImageIcon(colorSample));
            tb.add(colorButton);

            setColor(color);

            final SpinnerNumberModel strokeModel =
                    new SpinnerNumberModel(3,1,16,1);
            JSpinner strokeSize = new JSpinner(strokeModel);

            ChangeListener strokeListener = event -> {
                Object o = strokeModel.getValue();
                Integer i = (Integer) o;
                stroke = new BasicStroke(
                        i.intValue(),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.7f);
            };

            strokeSize.addChangeListener(strokeListener);
            strokeSize.setMaximumSize(strokeSize.getPreferredSize());
            JLabel strokeLabel = new JLabel("Stroke");
            strokeLabel.setLabelFor(strokeSize);
            strokeLabel.setDisplayedMnemonic('t');
            tb.add(strokeLabel);
            tb.add(strokeSize);

            tb.addSeparator();

            ActionListener clearListener = event -> {
                int result = JOptionPane.OK_OPTION;
                if (dirty) {
                    result = JOptionPane.showConfirmDialog(
                            gui, "Erase the current painting?");
                }
                if (result == JOptionPane.OK_OPTION) {
                    clear(canvasImage);
                }
            };

            JButton clearButton = new JButton("Clear");
            tb.add(clearButton);
            clearButton.addActionListener(clearListener);

            gui.add(tb, BorderLayout.PAGE_START);

            activeTool = DRAW_TOOL;

            gui.add(output, BorderLayout.PAGE_END);
            clear(colorSample);
            clear(canvasImage);

            // TODO: Initialize to drawing for debugging purposes only. Server will determine current state
            STATE = State.DRAWING;
        }

        return gui;
    }

    /** Clears the entire image area by painting it with the current color. */
    public void clear(BufferedImage bufferedImage) {
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHints(renderingHints);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        graphics.dispose();
        imageLabel.repaint();
    }

    public void setImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.drawImage(image, 0, 0, gui);
        g.dispose();

        if (this.imageLabel != null) {
            imageLabel.setIcon(new ImageIcon(canvasImage));
            this.imageLabel.repaint();
        }

        if (gui != null) {
            gui.invalidate();
        }
    }

    /** Set the current painting color and refresh any elements needed. */
    public void setColor(Color color) {
        this.color = color;
        clear(colorSample);
    }

    public void draw(Point point) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        if (lastPoint1 != null && lastPoint2 != null) {
            GeneralPath path = new GeneralPath();
            path.moveTo(lastPoint2.x, lastPoint2.y);
            path.curveTo(lastPoint2.x, lastPoint2.y, lastPoint1.x, lastPoint1.y, point.x, point.y);
            // TODO: Send GeneralPath to server as well using ObjectOutputStream
            g.draw(path);
        } else {
            g.drawLine(point.x, point.y, point.x, point.y);
        }

        g.dispose();
        this.imageLabel.repaint();
        dirty = true;
        lastPoint1 = point;
        lastPoint2 = lastPoint1;
    }

    class ImageMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent event) {
            if (activeTool == DrawingPane.DRAW_TOOL) {
                if (STATE == State.DRAWING)
                    draw(event.getPoint());
            } else {
                JOptionPane.showMessageDialog(
                        gui,
                        "Application error.  :(",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lastPoint1 = null;
            lastPoint2 = null;
        }
    }

    class ImageMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent event) {
            reportPositionAndColor(event);
            if (STATE == State.DRAWING)
                draw(event.getPoint());
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            reportPositionAndColor(event);
        }

    }

    private void reportPositionAndColor(MouseEvent me) {
        String text = "X,Y: " + (me.getPoint().x+1) + "," + (me.getPoint().y+1);
        output.setText(text);
    }
}
