import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TRIParser {
    ArrayList<File> triFiles;
    ArrayList<Triangle> tris;

    public TRIParser() {
        triFiles = new ArrayList<>();

        File dir = new File(System.getProperty("user.dir"));
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".tri")) {
                triFiles.add(f);
            }
        }
    }

    public ArrayList<Triangle> parse(int index) throws FileNotFoundException {
        tris = new ArrayList<>();
        Scanner scan = new Scanner(triFiles.get(index));

        while (scan.hasNext()) {
            Vertex v1 = new Vertex(scan.nextInt(), scan.nextInt(), scan.nextInt());
            Vertex v2 = new Vertex(scan.nextInt(), scan.nextInt(), scan.nextInt());
            Vertex v3 = new Vertex(scan.nextInt(), scan.nextInt(), scan.nextInt());
            Color color = new Color(scan.nextInt(), scan.nextInt(), scan.nextInt());
            tris.add(new Triangle(v1, v2, v3, color));
        }

        return tris;
    }

    public ArrayList<Triangle> parse(String name) throws FileNotFoundException {
        return parse(triFiles.indexOf(name + ".txt"));
    }

    public String[] getFileNames() {
        String[] fileNames = new String[triFiles.size()];

        for (int i = 0; i < fileNames.length; i++) {
            String fullName =  triFiles.get(i).getName();
            fileNames[i] = fullName.substring(0, fullName.length() - 4);
        }

        return fileNames;
    }
}
