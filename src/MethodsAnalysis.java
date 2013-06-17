import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * User: mbani002 Date: 6/4/13 : 12:39 PM
 */
public class MethodsAnalysis {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("Methods_short.txt"));
        Map<String, List<Integer>> map = (Map<String, List<Integer>>) inputStream.readObject();
        inputStream.close();

        Set<Map.Entry<String, List<Integer>>> entries = map.entrySet();
        ArrayList<Map.Entry<String, List<Integer>>> entryArrayList = new ArrayList<Map.Entry<String, List<Integer>>>(entries);

//        Collections.sort(entryArrayList, new Comparator<Map.Entry<String, List<Integer>>>() {
//            @Override
//            public int compare(Map.Entry<String, List<Integer>> o1, Map.Entry<String, List<Integer>> o2) {
//                int s1 = o1.getValue().size();
//                int s2 = o2.getValue().size();
//                return s1 < s2 ? 1 : s1 == s2 ? 0 : -1;
//            }
//        });
//
//        // print
//        for (Map.Entry<String, List<Integer>> entry : entryArrayList) {
//            System.out.printf("\n%s, %d", entry.getKey(), entry.getValue().size());
////            for (Integer integer : entry.getValue()) {
////                System.out.printf("%d, ", integer);
////            }
//        }


        Collections.sort(entryArrayList, new Comparator<Map.Entry<String, List<Integer>>>() {
            @Override
            public int compare(Map.Entry<String, List<Integer>> o1, Map.Entry<String, List<Integer>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // print
        for (Map.Entry<String, List<Integer>> entry : entryArrayList) {
            System.out.printf("\n%s", entry.getKey());
            for (Integer integer : entry.getValue()) {
                System.out.printf("%d, ", integer);
            }
        }

    }
}
