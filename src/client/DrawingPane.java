package client;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import interfaces.*;

/**
 * Serves as the pane on which 2D graphics will be displayed.
 */
public class DrawingPane implements GameScreen, Runnable {

    /** Image used to make changes. */
    private BufferedImage canvasImage;
    /** The main GUI that might be added to a frame or applet. */
    private JPanel gui;
    /** The color to use when calling clear, text or other
     * drawing functionality. */
    private Color currentColor = Color.BLACK;
    /** General user messages. */
    private JLabel output = new JLabel("DrawApp");

    private BufferedImage colorSample = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

    private JLabel imageLabel;

    private boolean dirty = false;
    private int strokeSize = 3;
    private Stroke stroke = new BasicStroke(
            strokeSize,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);
    private RenderingHints renderingHints;

    private Point lastPoint1;
    private Point lastPoint2;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private JTextArea guess;
    private String lastGuess;

    private JLabel scores;
    private JLabel time;
    private JLabel currentDrawer;
    private Set<String> beginningPlayers;
    private String username;

    private long startTime;

    private static State STATE;
    
    private List<JComponent> drawerComps;

    enum State {
        DRAWING,
        GUESSING,
        NO_GAME     // Game hasn't started yet or in between games
    }

    public void registerStreams(ObjectInputStream input, ObjectOutputStream output) {
    	in = input;
    	out = output;
    }

    public void setBeginningPlayers(Set<String> beginningPlayers) {
        this.beginningPlayers = beginningPlayers;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class DrawTimer implements ActionListener {
        public long timeRemaining;
        private JLabel display;

        public DrawTimer(JLabel display, long seconds) {
            this.timeRemaining = seconds;
            this.display = display;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (timeRemaining <= 0) {
                ((Timer) e.getSource()).stop();
                return;
            }
            timeRemaining--;

            String s = timeRemaining == 1 ? "Second" : "Seconds";
            display.setText(timeRemaining + " " + s + " Left");
            display.updateUI();
        }
    }

    public void run() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        for (String s : beginningPlayers) {
            String display;
            if (s.equals(username))
                display = "<b>" + Util.humanReadableUsername(s) + "</b>";
            else
                display = Util.humanReadableUsername(s);
            sb.append(display).append(": 0").append("<br>");
        }

        sb.append("</html>");
        scores.setText(sb.toString());

        DrawTimer drawTimer = null;

        try {
            while(true) {
                LobbyMessage message = (LobbyMessage) in.readObject();

                switch (message.type()) {

                    case TURN_START:
                        TurnStartAlert turnStartAlert = (TurnStartAlert) message;

                        // clear out the canvas
                        Graphics2D graphicsClear = this.canvasImage.createGraphics();
                        graphicsClear.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        graphicsClear.setRenderingHints(renderingHints);
                        graphicsClear.setColor(Color.WHITE);
                        graphicsClear.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
                        graphicsClear.dispose();
                        imageLabel.repaint();

                        if (turnStartAlert.isDrawer()) {
//                            System.out.println("I'm currently the drawer");
                            STATE = State.DRAWING;
                            guess.setText(turnStartAlert.getWord());
                            guess.setEditable(false);
                            currentDrawer.setText("You're drawing.");
                            currentDrawer.updateUI();

                            for (JComponent c : drawerComps) {
                                c.setVisible(true);
                                c.setEnabled(true);
                            }
                        } else {
//                            System.out.println("I'm currently guessing");
                            STATE = State.GUESSING;
                            guess.setText("");
                            guess.setEditable(true);
                            currentDrawer.setText("Drawing: " + Util.humanReadableUsername(turnStartAlert.getDrawerUsername()));
                            currentDrawer.updateUI();

                            for (JComponent c : drawerComps) {
                                c.setVisible(false);
                                c.setEnabled(false);
                            }
                        }

                        time.setText(turnStartAlert.getSeconds() + " Seconds Left");
                        drawTimer = new DrawTimer(time, turnStartAlert.getSeconds());
                        new Timer(1000, drawTimer).start();

                        break;

                    case DRAW_INFO:
                        DrawInfo di = (DrawInfo) message;

                        Graphics2D graphics = this.canvasImage.createGraphics();
                        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        graphics.setRenderingHints(renderingHints);


                        if (di.isClear()) {
                            graphics.setColor(Color.WHITE);
                            graphics.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
                            graphics.dispose();
                            imageLabel.repaint();
                        } else {
                            graphics.setColor(di.color);
                            graphics.setStroke(new BasicStroke(
                                    di.strokeSize,
                                    BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND,
                                    1.7f));
                            GeneralPath path = di.path;
                            graphics.draw(path);
                        }

                        graphics.dispose();
                        this.imageLabel.repaint();
                        dirty = true;
                        break;

                    case CORRECT_ANSWER:
                        System.out.println("Latency (ms): " + Util.formatTime(System.nanoTime() - startTime));
//                        System.out.println("Correct Guess!");
                        Popup p1 = PopupFactory
                                .getSharedInstance()
                                .getPopup(guess,
                                        new JLabel(" Correct! "),
                                        (int) guess.getLocationOnScreen().getX() + 10,
                                        (int) guess.getLocationOnScreen().getY() - 20);
                        p1.show();
                        java.util.Timer t1 = new java.util.Timer();
                        t1.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                p1.hide();
                            }
                        }, 2000);
                        guess.setEditable(false);
                        guess.setBackground(Color.green);
                        guess.setText(lastGuess);
                        break;

                    case INCORRECT_ANSWER:
                        System.out.println("Latency (ms): " + Util.formatTime(System.nanoTime() - startTime));
//                        System.out.println("Incorrect guess :(");
                        Popup p2 = PopupFactory
                                .getSharedInstance()
                                .getPopup(guess,
                                        new JLabel(" Incorrect "),
                                        (int) guess.getLocationOnScreen().getX() + 10,
                                        (int) guess.getLocationOnScreen().getY() - 20);
                        p2.show();
                        java.util.Timer t2 = new java.util.Timer();
                        t2.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                p2.hide();
                            }
                        }, 2000);
                        guess.setBackground(new Color(250, 180, 180));
                        break;

                    case TURN_END:
//                        System.out.println("Server has ended the turn");
                        TurnEndAlert turnEndAlert = (TurnEndAlert) message;
                        drawTimer.timeRemaining = 1;
                        List<Map.Entry> points = new ArrayList<>(turnEndAlert.getCurrentPoints().entrySet());
                        Collections.sort(points, (o1, o2) -> (Integer) o2.getValue() - (Integer) o1.getValue());
                        StringBuilder s = new StringBuilder();
                        s.append("<html>");
                        for (Map.Entry entry : points) {
                            String display;
                            if (entry.getKey().equals(username))
                                display = "<b>" + Util.humanReadableUsername((String)entry.getKey()) + "</b>";
                            else
                                display = Util.humanReadableUsername((String)entry.getKey());
                            s.append(display).append(": ").append(entry.getValue()).append("<br>");
                        }
                        s.append("</html>");

                        if (STATE == State.GUESSING) {
                            JLabel label = new JLabel("Correct word was \"" + turnEndAlert.getWord() + "\"");
                            JPanel wordPanel = new JPanel();
                            wordPanel.add(label, BorderLayout.CENTER);
                            wordPanel.setPreferredSize(new Dimension(200, 30));
                            Popup p3 = PopupFactory
                                    .getSharedInstance()
                                    .getPopup(gui,
                                            wordPanel,
                                            (int) (gui.getLocationOnScreen().getX() + gui.getWidth() - 50) / 2,
                                            (int) (gui.getLocationOnScreen().getY() + gui.getHeight()) / 2);
                            p3.show();
                            java.util.Timer t3 = new java.util.Timer();
                            t3.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    p3.hide();
                                }
                            }, 3000);
                        }

                        guess.setBackground(Color.white);
                        if (STATE == State.GUESSING)
                            out.writeObject("");
                        else
                            out.writeObject(new DrawInfo());
                        scores.setText(s.toString());
//                        System.out.println(s.toString());
                        STATE = State.NO_GAME;
                        break;

                    case GAME_END:
                        GameEndAlert gameEndAlert = (GameEndAlert) message;
                        StringBuilder b = new StringBuilder();
                        sb.append("<html>The game ended.<br>Winners are:<br>");
                        for (String st : gameEndAlert.getWinners()) {
                            b.append("\t").append(Util.humanReadableUsername(st)).append("<br>");
                        }

                        b.append("</html>");

                        JPanel panel = new JPanel();
                        panel.add(new JLabel(b.toString()));

                        JOptionPane.showMessageDialog(null, panel, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                        return;
                }
            }
        } catch (IOException e) {
            System.err.println("Disconnect from Server");
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    
    public JComponent getGui() {
        if (gui == null) {
            Map<Key, Object> hintsMap = new HashMap<>();
            drawerComps = new ArrayList<>();
            hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            renderingHints = new RenderingHints(hintsMap);

            setImage(new BufferedImage(960, 640, BufferedImage.TYPE_INT_RGB));
            gui = new JPanel(new BorderLayout(4, 4));
            gui.setBorder(new EmptyBorder(5, 3, 5, 3));

            JPanel imageView = new JPanel(new GridBagLayout());
            imageView.setPreferredSize(new Dimension(1000, 700));
            imageLabel = new JLabel(new ImageIcon(canvasImage));
            JScrollPane imageScroll = new JScrollPane(imageView);
            imageView.add(imageLabel);
            imageLabel.addMouseMotionListener(new ImageMouseMotionListener());
            imageLabel.addMouseListener(new ImageMouseListener());
            gui.add(imageScroll, BorderLayout.CENTER);

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            JButton colorButton = new JButton("Color");
            colorButton.setToolTipText("Choose a Color");

            ActionListener colorListener = event -> {
                Color color = JColorChooser.showDialog(
                        gui, "Choose a color", Color.BLACK);
                if (color != null) {
                    setColor(color);
                }
            };

            colorButton.addActionListener(colorListener);
            colorButton.setIcon(new ImageIcon(colorSample));
            toolBar.add(colorButton);
            drawerComps.add(colorButton);

            setColor(currentColor);

            final SpinnerNumberModel strokeModel = new SpinnerNumberModel(strokeSize, 1, 16, 1);
            JSpinner strokeSize = new JSpinner(strokeModel);

            ChangeListener strokeListener = event -> {
                Object o = strokeModel.getValue();
                Integer i = (Integer) o;
                this.strokeSize = i.intValue();
                stroke = new BasicStroke(
                		this.strokeSize,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.7f);
            };

            strokeSize.addChangeListener(strokeListener);
            strokeSize.setMaximumSize(strokeSize.getPreferredSize());
            JLabel strokeLabel = new JLabel("Stroke");
            strokeLabel.setLabelFor(strokeSize);
            strokeLabel.setDisplayedMnemonic('t');
            toolBar.add(strokeLabel);
            toolBar.add(strokeSize);
            drawerComps.add(strokeLabel);
            drawerComps.add(strokeSize);

            toolBar.addSeparator();

            ActionListener clearListener = event -> {
                int result = JOptionPane.OK_OPTION;
                if (dirty) {
                    result = JOptionPane.showConfirmDialog(
                            gui, "Erase the current painting?");
                }

                if (result == JOptionPane.OK_OPTION) {
                    clear(canvasImage, Color.WHITE);
                    try {
                        if (STATE == State.DRAWING) {
                            out.flush();
                            out.writeObject(new DrawInfo());
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

            JButton clearButton = new JButton("Clear");
            toolBar.add(clearButton);
            clearButton.addActionListener(clearListener);
            drawerComps.add(clearButton);

            gui.add(toolBar, BorderLayout.PAGE_START);

            gui.add(output, BorderLayout.PAGE_END);
            clear(colorSample, currentColor);
            clear(canvasImage, Color.WHITE);

            guess = new JTextArea();
            guess.addKeyListener(new GuessKeyListener());
            gui.add(guess, BorderLayout.PAGE_END);

            currentDrawer = new JLabel();
            JPanel drawerPanel = new JPanel(new BorderLayout());
            currentDrawer.setHorizontalAlignment(SwingConstants.RIGHT);
            drawerPanel.add(currentDrawer, BorderLayout.CENTER);
            toolBar.add(drawerPanel);

            scores = new JLabel();
            scores.setPreferredSize(new Dimension(100, 640));
            gui.add(scores, BorderLayout.EAST);

            time = new JLabel();
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(time, BorderLayout.CENTER);
            time.setHorizontalAlignment(SwingConstants.RIGHT);
            toolBar.add(panel);
        }

        return gui;
    }

    public void clear(BufferedImage bufferedImage, Color color) {
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHints(renderingHints);
        graphics.setColor(color);
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        graphics.dispose();
        imageLabel.repaint();
    }

    public void setImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = this.canvasImage.createGraphics();
        graphics.setRenderingHints(renderingHints);
        graphics.drawImage(image, 0, 0, gui);
        graphics.dispose();

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
        this.currentColor = color;
        clear(colorSample, color);
    }

    public void draw(Point point) {
        Graphics2D graphics = this.canvasImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHints(renderingHints);
        graphics.setColor(this.currentColor);
        graphics.setStroke(stroke);
        if (lastPoint1 != null && lastPoint2 != null) {
            GeneralPath path = new GeneralPath();
            path.moveTo(lastPoint2.x, lastPoint2.y);
            path.curveTo(lastPoint2.x, lastPoint2.y, lastPoint1.x, lastPoint1.y, point.x, point.y);
            graphics.draw(path);
            try {
            	if (STATE == State.DRAWING) {
                    out.flush();
        			out.writeObject(new DrawInfo(path, currentColor, strokeSize));
            	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            graphics.drawLine(point.x, point.y, point.x, point.y);
        }

        graphics.dispose();
        this.imageLabel.repaint();
        dirty = true;
        lastPoint1 = point;
        lastPoint2 = lastPoint1;
    }

    class ImageMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent event) {
            if (STATE == State.DRAWING)
                draw(event.getPoint());
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
    
    class GuessKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				String g = guess.getText();
				guess.setText("");
				arg0.consume();
				lastGuess = g;

				try {
					if (g.length() > 0) {
                        startTime = System.nanoTime();
                        out.writeObject(g);
//                        System.out.println("Sent guess " + g + " to server.");
                    }
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
                guess.setBackground(Color.white);
            }
		}

		@Override
		public void keyReleased(KeyEvent arg0) {}
		@Override
		public void keyTyped(KeyEvent arg0) {}
    	
    }

    private void reportPositionAndColor(MouseEvent me) {
        String text = "X,Y: " + (me.getPoint().x+1) + "," + (me.getPoint().y+1);
        output.setText(text);
    }
}
