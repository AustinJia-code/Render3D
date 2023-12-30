// Based on: http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class Viewer {
    JFrame frame;
    Container pane;
    JSlider headingSlider, pitchSlider;
    JPanel renderPanel;

    public Viewer() {
        frame = new JFrame();
        pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        headingSlider = new JSlider(0, 360, 180);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
    }

    public void render(ArrayList<Triangle> tris) {
        // panel to display render results
        renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                for (Triangle t : tris) {
                    Path2D path = new Path2D.Double();
                    path.moveTo(t.v1.x, t.v1.y);
                    path.lineTo(t.v2.x, t.v2.y);
                    path.lineTo(t.v3.x, t.v3.y);
                    path.closePath();
                    g2.draw(path);
                }
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}