import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class Engine {
    ArrayList<Triangle> triangles;
    public Engine(ArrayList<Triangle> triangles) {
        this.triangles = triangles;

    }

    public void setData(ArrayList<Triangle> triangles) {
        this.triangles = triangles;
    }

    public void calculate(BufferedImage img, int inflateSlider, int headingSlider, int pitchSlider, int zoomSlider) {
        ArrayList<Triangle> tris = deepCopy((List)triangles);
        for (int i = 0; i < inflateSlider; i++) {
            tris = inflate(tris);
        }

        tris = zoom(tris, zoomSlider / 100.0);

        double heading = Math.toRadians(headingSlider);
        Matrix3 headingTransform = new Matrix3(new double[] {
                Math.cos(heading), 0, -Math.sin(heading),
                0, 1, 0,
                Math.sin(heading), 0, Math.cos(heading)
        });
        double pitch = Math.toRadians(pitchSlider);
        Matrix3 pitchTransform = new Matrix3(new double[] {
                1, 0, 0,
                0, Math.cos(pitch), Math.sin(pitch),
                0, -Math.sin(pitch), Math.cos(pitch)
        });
        Matrix3 transform = headingTransform.multiply(pitchTransform);

        double[] zBuffer = new double[img.getWidth() * img.getHeight()];
        // initialize array with extremely far away depths
        for (int q = 0; q < zBuffer.length; q++) {
            zBuffer[q] = Double.NEGATIVE_INFINITY;
        }

        for (Triangle t : tris) {
            Vertex v1 = transform.transform(t.v1);
            v1.x += img.getWidth() / 2;
            v1.y += img.getHeight() / 2;
            Vertex v2 = transform.transform(t.v2);
            v2.x += img.getWidth() / 2;
            v2.y += img.getHeight() / 2;
            Vertex v3 = transform.transform(t.v3);
            v3.x += img.getWidth() / 2;
            v3.y += img.getHeight() / 2;

            Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
            Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
            Vertex norm = new Vertex(
                    ab.y * ac.z - ab.z * ac.y,
                    ab.z * ac.x - ab.x * ac.z,
                    ab.x * ac.y - ab.y * ac.x
            );
            double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
            norm.x /= normalLength;
            norm.y /= normalLength;
            norm.z /= normalLength;

            double angleCos = Math.abs(norm.z);

            int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                    double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                    double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                    if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                        double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                        int zIndex = y * img.getWidth() + x;
                        if (zBuffer[zIndex] < depth) {
                            img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                            zBuffer[zIndex] = depth;
                        }
                    }
                }
            }

        }
    }

    private static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }

    private static ArrayList<Triangle> inflate(List<Triangle> tris) {
        ArrayList<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    private static ArrayList<Triangle> zoom(List<Triangle> tris, double multiplier) {
        ArrayList<Triangle> result = new ArrayList<>();

        for (Triangle t : tris) {
            Vertex m1 = new Vertex(t.v1.x * multiplier, t.v1.y * multiplier, t.v1.z * multiplier);
            Vertex m2 = new Vertex(t.v2.x * multiplier, t.v2.y * multiplier, t.v2.z * multiplier);
            Vertex m3 = new Vertex(t.v3.x * multiplier, t.v3.y * multiplier, t.v3.z * multiplier);
            result.add(new Triangle(m1, m2, m3, t.color));
        }

        return result;
    }

    private static ArrayList<Object> deepCopy(List<Object> list) {
        ArrayList<Object> copy = new ArrayList<>();

        for (Object o : list) {
            copy.add(o);
        }

        return copy;
    }
}