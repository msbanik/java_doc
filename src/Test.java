import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * User: mbani002 Date: 6/4/13 : 11:50 AM
 */
public class Test {
    static Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
    static Set<String> set = new HashSet<String>();

    public static void main(String[] args) throws IOException {
        String cname;
        int id = 0;

        BufferedReader br = new BufferedReader(new FileReader("/Users/mbani002/code/IdeaProjects/jhelp/doc/qname.txt"));
        cname = br.readLine();
        while (cname != null) {
            digClass(cname.substring(2), id);
            cname = br.readLine();
            id++;
        }
        br.close();


        // print
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            System.out.printf("\n%s: \n", entry.getKey());
            List<Integer> value = entry.getValue();
            if (value.size() > 500) {
                value.clear();
                value.add(0);
            }
        }
        // write to file
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("Methods_short.txt"));
            stream.writeObject(map);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        try {
//            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("String.txt"));
//            Map<String, List<Integer>> mapo = (Map<String, List<Integer>>) inputStream.readObject();
//            inputStream.close();
//
//            for (Map.Entry<String, List<Integer>> entry : mapo.entrySet()) {
//                System.out.printf("\n%s: \n", entry.getKey());
//                for (Integer integer : entry.getValue()) {
//                    System.out.printf("%d, ", integer);
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }


    }

    private static void digClass(String cname, int id) {
        try {
            Class klass = Class.forName(cname);
            for (Method method : klass.getMethods()) {
                set.add(method.getName());
            }
            for (String name : set) {
                List<Integer> list = map.get(name);
                if (list == null) {
                    list = new ArrayList<Integer>();
                    map.put(name, list);
                }
                list.add(id);
            }
            set.clear();
        } catch (ClassNotFoundException e) {
            System.err.println(cname);
        }
    }
}
