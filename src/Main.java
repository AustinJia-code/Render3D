import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        TRIParser files = new TRIParser();
        Engine engine = new Engine(files.parse(0));

        Viewer viewer = new Viewer(engine, files);
        viewer.update();
    }
}