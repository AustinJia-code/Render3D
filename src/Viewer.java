import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Viewer {
    JPanel renderPanel;
    JFrame frame;
    Container pane;
    JSlider pitchSlider, inflateSlider, headingSlider;
    Engine engine;

    public Viewer(Engine engine) {
        frame = new JFrame();
        pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);
        headingSlider.addChangeListener(e -> renderPanel.repaint());

        // slider to control vertical rotation
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        // slider to control inflation
        inflateSlider = new JSlider(0, 4, 0);
        pane.add(inflateSlider, BorderLayout.NORTH);
        inflateSlider.addChangeListener(e -> renderPanel.repaint());

        this.engine = engine;
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

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
