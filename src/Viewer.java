import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

public class Viewer{
    JPanel renderPanel;
    JLabel dummy;
    JFrame frame;
    Container pane;
    JSlider pitchSlider, inflateSlider, headingSlider, zoomSlider;
    JComboBox menu;
    Engine engine;
    Boolean[] arrowsPressed;
    Listener listener;
    TRIParser files;

    public Viewer(Engine engine, TRIParser files) {
        this.engine = engine;
        this.files = files;

        frame = new JFrame("3D Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        listener = new Listener();
        frame.addKeyListener(listener);
        frame.addMouseWheelListener(listener);

        pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        headingSlider.setFocusable(false);

        // slider to control vertical rotation
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -180, 180, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.setFocusable(false);

        // slider to control inflation
        inflateSlider = new JSlider(0, 7, 0);
        pane.add(inflateSlider, BorderLayout.NORTH);
        inflateSlider.addChangeListener(e -> renderPanel.repaint());
        inflateSlider.setFocusable(false);

        // slider to control zoom, zoom% calculated with value * 1%
        zoomSlider = new JSlider(SwingConstants.VERTICAL, 20, 500, 100);
        pane.add(zoomSlider, BorderLayout.WEST);
        zoomSlider.addChangeListener(e -> renderPanel.repaint());
        zoomSlider.setFocusable(false);

        arrowsPressed = new Boolean[4];
        for (int i = 0; i < arrowsPressed.length; i++) {
            arrowsPressed[i] = new Boolean(false);
        }

        Action[] inflateActions = new SliderSetAction[8];
        for (int i = 0; i < inflateActions.length; i++) {
            inflateActions[i] = new SliderSetAction(i);
        }

        InputMap inputMap = inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = inflateSlider.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke('1'), "1ac");
        actionMap.put("1ac", inflateActions[0]);
        inputMap.put(KeyStroke.getKeyStroke('2'), "2ac");
        actionMap.put("2ac", inflateActions[1]);
        inputMap.put(KeyStroke.getKeyStroke('3'), "3ac");
        actionMap.put("3ac", inflateActions[2]);
        inputMap.put(KeyStroke.getKeyStroke('4'), "4ac");
        actionMap.put("4ac", inflateActions[3]);
        inputMap.put(KeyStroke.getKeyStroke('5'), "5ac");
        actionMap.put("5ac", inflateActions[4]);
        inputMap.put(KeyStroke.getKeyStroke('6'), "6ac");
        actionMap.put("6ac", inflateActions[5]);
        inputMap.put(KeyStroke.getKeyStroke('7'), "7ac");
        actionMap.put("7ac", inflateActions[6]);
        inputMap.put(KeyStroke.getKeyStroke('8'), "8ac");
        actionMap.put("8ac", inflateActions[7]);

        renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                engine.calculate(img, inflateSlider.getValue(), headingSlider.getValue(), pitchSlider.getValue(), zoomSlider.getValue());

                g2.drawImage(img, 0, 0, null);
            }
        };
        // add dropdown menu for files
        menu = new JComboBox(files.getFileNames());
        menu.setSize(150, 30);
        menu.setFocusable(false);
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    engine.setData(files.parse(menu.getSelectedIndex()));
                    renderPanel.repaint();
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        renderPanel.add(menu);
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public void update() {
        boolean exit = false;
        long counter = 0;

        while (!exit) {
            if (!listener.mouseAccountedFor) {
                incrementSlider(zoomSlider, listener.changeInRotation * 5);
                listener.mouseAccountedFor = true;
            }

            if (counter++ == 100000000) {
                if (listener.right) {
                    incrementSlider(headingSlider, 2);
                }
                if (listener.left) {
                    incrementSlider(headingSlider, -2);
                }
                if (listener.up) {
                    incrementSlider(pitchSlider, 2);
                }
                if (listener.down) {
                    incrementSlider(pitchSlider, -2);
                }
                if (listener.space) {
                    reset();
                }

                if (pitchSlider.getValue() == pitchSlider.getMaximum()) {
                    pitchSlider.setValue(pitchSlider.getMinimum());
                } else if (pitchSlider.getValue() == pitchSlider.getMinimum()) {
                    pitchSlider.setValue(pitchSlider.getMaximum());
                }

                if (headingSlider.getValue() == headingSlider.getMaximum()) {
                    headingSlider.setValue(headingSlider.getMinimum());
                } else if (headingSlider.getValue() == headingSlider.getMinimum()) {
                    headingSlider.setValue(headingSlider.getMaximum());
                }
                counter = 0;
            }
        }
    }

    private class SliderSetAction extends AbstractAction {
        int inflation;
        public SliderSetAction(int i) {
            inflation = i;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ChangeListener[] listeners = inflateSlider.getChangeListeners();
            for (ChangeListener listener : listeners) {
                inflateSlider.removeChangeListener(listener);
            }

            inflateSlider.setValue(inflation);
            renderPanel.repaint();

            for (ChangeListener listener : listeners) {
                inflateSlider.addChangeListener(listener);
            }
        }
    }

    private void incrementSlider(JSlider s, int step) {
        ChangeListener[] listeners = s.getChangeListeners();
        for (ChangeListener listener : listeners) {
            s.removeChangeListener(listener);
        }

        s.setValue(s.getValue() + step);
        renderPanel.repaint();

        for (ChangeListener listener : listeners) {
            s.addChangeListener(listener);
        }
    }

    private void reset() {
        incrementSlider(inflateSlider, -inflateSlider.getValue());
        incrementSlider(headingSlider, -headingSlider.getValue());
        incrementSlider(pitchSlider, -pitchSlider.getValue());
        incrementSlider(zoomSlider, -(zoomSlider.getValue() - 100));
    }
}
