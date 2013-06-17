import java.io.File;
import java.io.IOException;

/**
 * User: mbani002 Date: 6/13/13 : 2:08 AM
 */
public class Test2 {
    public Test2() {
        System.out.println(this.getClass().getName() + " is loaded from " +
                getClass().getProtectionDomain().getCodeSource().getLocation());
    }

    public static void main(String[] args) throws IOException {
        new Test2();
//        File file = new File(".");
//        System.out.println(file.getAbsolutePath());
//        System.out.println(file.getCanonicalPath());
//        System.out.println(file.getPath());
    }
}
