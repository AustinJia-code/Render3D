import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Viewer {
    JPanel renderPanel;
    JFrame frame;
    Container pane;
    JSlider pitchSlider, inflateSlider, headingSlider;
    Engine engine;
    boolean panLeft, panRight;

    public Viewer(Engine engine) {
        this.engine = engine;

        frame = new JFrame("3D Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        headingSlider.setFocusable(false);

        // slider to control vertical rotation
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.setFocusable(false);

        // slider to control inflation
        inflateSlider = new JSlider(0, 4, 0);
        pane.add(inflateSlider, BorderLayout.NORTH);
        inflateSlider.addChangeListener(e -> renderPanel.repaint());
        inflateSlider.setFocusable(false);

        Action panUpAction = new PanUpAction();
        Action panDownAction = new PanDownAction();
        Action panLeftAction = new PanLeftAction();
        Action panRightAction = new PanRightAction();
        Action[] inflateActions = new InflateAction[5];
        for (int i = 0; i < inflateActions.length; i++) {
            inflateActions[i] = new InflateAction(i);
        }
        Action zoomInAction;
        Action zoomOutAction;

        headingSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "leftAction");
        headingSlider.getActionMap().put("leftAction", panLeftAction);
        headingSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "rightAction");
        headingSlider.getActionMap().put("rightAction", panRightAction);
        pitchSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "upAction");
        pitchSlider.getActionMap().put("upAction", panUpAction);
        pitchSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "downAction");
        pitchSlider.getActionMap().put("downAction", panDownAction);
        inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('1'), "1ac");
        inflateSlider.getActionMap().put("1ac", inflateActions[0]);
        inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('2'), "2ac");
        inflateSlider.getActionMap().put("2ac", inflateActions[1]);
        inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('3'), "3ac");
        inflateSlider.getActionMap().put("3ac", inflateActions[2]);
        inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('4'), "4ac");
        inflateSlider.getActionMap().put("4ac", inflateActions[3]);
        inflateSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('5'), "5ac");
        inflateSlider.getActionMap().put("5ac", inflateActions[4]);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public void render() {
        // panel to display render results
        renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                engine.calculate(img, inflateSlider.getValue(), headingSlider.getValue(), pitchSlider.getValue());

                g2.drawImage(img, 0, 0, null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        boolean exit = false;
        while (!exit) {
            System.out.println("test");
        }
    }

    public class PanLeftAction extends AbstractAction {

        boolean move = false;

        @Override
        public void actionPerformed(ActionEvent e) {
            move = true;
            ChangeListener[] listeners = headingSlider.getChangeListeners();
            for (ChangeListener listener : listeners) {
                headingSlider.removeChangeListener(listener);
            }

            headingSlider.setValue(headingSlider.getValue() - 1);
            renderPanel.repaint();

            for (ChangeListener listener : listeners) {
                headingSlider.addChangeListener(listener);
            }
        }
    }
    public class PanRightAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            ChangeListener[] listeners = headingSlider.getChangeListeners();
            for (ChangeListener listener : listeners) {
                headingSlider.removeChangeListener(listener);
            }

            headingSlider.setValue(headingSlider.getValue() + 1);
            renderPanel.repaint();

            for (ChangeListener listener : listeners) {
                headingSlider.addChangeListener(listener);
            }
        }
    }
    public class PanUpAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            ChangeListener[] listeners = pitchSlider.getChangeListeners();
            for (ChangeListener listener : listeners) {
                pitchSlider.removeChangeListener(listener);
            }

            pitchSlider.setValue(pitchSlider.getValue() + 1);
            renderPanel.repaint();

            for (ChangeListener listener : listeners) {
                pitchSlider.addChangeListener(listener);
            }
        }
    }
    public class PanDownAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            ChangeListener[] listeners = pitchSlider.getChangeListeners();
            for (ChangeListener listener : listeners) {
                pitchSlider.removeChangeListener(listener);
            }

            pitchSlider.setValue(pitchSlider.getValue() - 1);
            renderPanel.repaint();

            for (ChangeListener listener : listeners) {
                pitchSlider.addChangeListener(listener);
            }
        }
    }
    public class InflateAction extends AbstractAction {
        int inflation;
        public InflateAction(int i) {
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
}
