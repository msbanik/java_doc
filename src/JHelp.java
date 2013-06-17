/**
 * JHelp
 *
 * @author Madhusudan Banik (mbani002@fiu.edu)
 * @version 1.0
 **/

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JHelp {

    private static String[] classNames;
    private static String[] qualifiedClassNames;
    private static Map<String, String[]> docMap = new HashMap<String, String[]>();
    private static Map<String, List<Integer>> methodsMap;

    private static String docRoot, mname, queryString, spokenText = "";
    private static List<String> objMethods, classNameList, methodNameList = null;
    private static List<Integer> ids = null;

    private static final int classCount = 3793;
    private static int nextNumber = -1;
    private static String klass = "java.lang.Object";

    enum OP {
        SAY, OPEN, NEXT, PREV, DOCS, HISTORY, QUIT, EMPTY, CLASS_SEARCH, METHOD_SEARCH
    }

    public static OP getOP(String input) {
        if (input.equals("s")) {
            return OP.SAY;
        } else if (input.equals("o")) {
            return OP.OPEN;
        } else if (input.equals("j")) {
            return OP.NEXT;
        } else if (input.equals("k")) {
            return OP.PREV;
        } else if (input.equals("d") || Character.isDigit(input.charAt(0))) {
            return OP.DOCS;
        } else if (input.equals("h")) {
            return OP.HISTORY;
        } else if (input.equals("q")) {
            return OP.QUIT;
        } else if (input.startsWith(".")) {
            return OP.METHOD_SEARCH;
        } else if (input.equals("")) {
            return OP.EMPTY;
        }
        return OP.CLASS_SEARCH;
    }

    public static void loadResources() throws IOException, ClassNotFoundException {
        String line;
        int i;

        Properties properties = new Properties();
        properties.load(new FileInputStream("/Users/mbani002/code/IdeaProjects/jhelp/src/config"));

        String resRoot = properties.getProperty("resRoot");
        docRoot = properties.getProperty("docRoot");

        line = properties.getProperty("objMethods");
        objMethods = Arrays.asList(line.split(","));


        //Reading the class list file
        File file = new File(resRoot + "all_classes");
        BufferedReader br = new BufferedReader(new FileReader(file));

        classNames = new String[classCount];
        i = 0;
        line = br.readLine();
        while (line != null) {
            classNames[i++] = line;
            line = br.readLine();
        }
        br.close();

        file = new File(resRoot + "all_classes_qualified_name");
        br = new BufferedReader(new FileReader(file));
        qualifiedClassNames = new String[classCount];
        line = br.readLine();
        i = 0;
        while (line != null) {
            qualifiedClassNames[i++] = line;
            line = br.readLine();
        }
        br.close();

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(resRoot + "all_methods"));

        @SuppressWarnings("unchecked")
        Map<String, List<Integer>> _methodsMap = (Map<String, List<Integer>>) inputStream.readObject();
        methodsMap = _methodsMap;
        inputStream.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        loadResources();

        clearConsole();
        System.out.println("Type -h or ? for help on help!");

        Scanner scanner = new Scanner(System.in);
        String input, prevInput = "";
        Runtime runtime = Runtime.getRuntime();
        OP op;

        while (true) {
            //Console symbol
            System.out.print("\033[1m>>\033[0m ");
            input = scanner.nextLine();

            if (input.startsWith("_")) {
                klass = klass.substring(klass.lastIndexOf('.') + 1);
                input = input.replace("_", klass + ".");
            } else if (input.startsWith("!!")) {
                input = prevInput + input.substring(2);
            }

            op = getOP(input);
            switch (op) {
                case METHOD_SEARCH:
                    searchMethod(input.substring(1));
                    break;
                case SAY:
                    runtime.exec("say " + spokenText);
                    break;
                case OPEN:
                    String url = docRoot + klass.substring(2).replace(".", "/") + ".html";
                    runtime.exec("open " + url);
                    break;
                case NEXT:
                    showNext(methodNameList, klass);
                    break;
                case PREV:
                    showPrev(methodNameList, klass);
                    break;
                case DOCS:
                    showDocs(input);
                    break;
                case EMPTY:
                    showNext(methodNameList, klass);
                    break;
                case CLASS_SEARCH:
                    searchClass(input);
                    break;
                case QUIT:
                    System.exit(1);
            }

            prevInput = input;
            System.out.println();
        }
    }

    public static void showDocs(String input) throws IOException {
        if (input.charAt(input.length() - 1) == 'h') {
            nextNumber = Integer.parseInt(input.substring(0, input.length() - 1));
            if (methodNameList != null) {
                mname = methodNameList.get(nextNumber);
                getDoc(klass, mname);
            }
        } else if (input.charAt(input.length() - 1) == 'd') {
            if (!input.equals("d")) {
                klass = qualifiedClassNames[Integer.parseInt(input.substring(0, input.length() - 1))];
            }
            getDoc(klass, "Class_Doc");
        } else {
            klass = qualifiedClassNames[ids.get(Integer.parseInt(input))];
            methodNameList = digClass(klass, "");
            prettyPrint(methodNameList, "");
        }
    }

    public static List<Integer> search(String query, String[] cname) {
        queryString = query;
        List<Integer> result = new ArrayList<Integer>();
        Pattern pattern = null;

        for (int i = 0; i < query.length(); i++) {
            if (Character.isUpperCase(query.charAt(i))) {
                pattern = Pattern.compile(query);
                break;
            }
        }
        if (pattern == null)
            pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

        int i = 0;
        for (String name : cname) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                if (name.equals(query)) {
                    result.clear();
                    result.add(i);
                    return result;
                }
                result.add(i);
            }
            i++;
        }
        return result;
    }

    public static void searchClass(String input) throws IOException {
        String[] searchKey = processInput(input);
        ids = new ArrayList<Integer>();
        classNameList = new ArrayList<String>();

        clearConsole();
        System.out.println("Search result for : " + input + "\n");

        boolean packageListingEnable = false;
        //package:class.method
        if (searchKey[0].length() > 2) {
            ids = searchPackage(searchKey[0] + ".*" + searchKey[1], qualifiedClassNames);
            for (Integer r : ids) {
                classNameList.add(qualifiedClassNames[r]);
            }
            prettyPrint(classNameList, searchKey[1]);
            packageListingEnable = true;
            return;
        } else {
            packageListingEnable = !searchKey[0].equals("");
        }

        //search for class
        ids = search(searchKey[1], classNames);
        //exact match for class
        if (ids.size() == 1) {
            klass = qualifiedClassNames[ids.get(0)];
            methodNameList = digClass(klass, searchKey[2]);
            if (methodNameList == null) {
                System.out.println("Class: " + klass + " not found!");
                return;
            }
            //print method names
            //exact match for method
            if (methodNameList.size() == 1) {
                mname = methodNameList.get(0);
                getDoc(klass, mname);
                return;
            }
            prettyPrint(methodNameList, searchKey[2]);
            nextNumber = -1;
            return;
        }

        String[] fname = classNames;
        if (packageListingEnable) {
            fname = qualifiedClassNames;
        }
        classNameList.clear();
        for (Integer r : ids) {
            classNameList.add(fname[r]);
        }
        prettyPrint(classNameList, searchKey[1]);

    }

    public static void searchMethod(String line) {
        List<String> result = new ArrayList<String>();
        Pattern pattern = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
        for (String name : methodsMap.keySet()) {
            if (name.equals(line)) {
                result.clear();
                result.add(line);
                break;
            }
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                result.add(name);
            }
        }
        if (result.size() == 1) {
            List<Integer> integers = methodsMap.get(result.get(0));
            result.clear();
            for (Integer integer : integers) {
                result.add(classNames[integer]);
            }
            line = "";
        }
        Collections.sort(result);
        prettyPrint(result, line);
    }

    public static List<Integer> searchPackage(String query, String[] qname) {
        queryString = query;
        List<Integer> result = new ArrayList<Integer>();
        Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        int i = 0;
        for (String name : qname) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                result.add(i);
            }
            i++;
        }
        Collections.sort(result);
        return result;
    }

    public static void showPrev(List<String> mresults, String klass) throws IOException {
        String mname;
        if (--nextNumber == -1) {
            nextNumber = mresults.size() - 1;
        }
        mname = mresults.get(nextNumber);
        getDoc(klass, mname);
    }

    public static void showNext(List<String> mresults, String klass) throws IOException {
        if (mresults == null || klass == null) {
            return;
        }
        String mname;
        if (++nextNumber == mresults.size()) {
            nextNumber = 0;
            System.out.println("Rotate ...");
        }
        mname = mresults.get(nextNumber);
        getDoc(klass, mname);
    }

    public static String[] processInput(String line) {
        if (line.endsWith(":")) {
            line += ".";
        } else if (line.startsWith(":")) {
            line = "." + line;
        }
        String[] strings = line.split(":");
        String[] results = Arrays.copyOf(strings, 3);

        if (strings.length == 1) {
            results[0] = "";
            if (strings[0].contains(".")) {
                results[1] = strings[0].substring(0, strings[0].indexOf('.'));
                results[2] = strings[0].substring(strings[0].indexOf('.') + 1);
            } else {
                results[1] = line;
                results[2] = "";
            }
        } else if (strings.length == 2) {
            if (strings[1].contains(".")) {
                results[1] = strings[1].substring(0, strings[1].indexOf('.'));
                results[2] = strings[1].substring(strings[1].indexOf('.') + 1);
            } else {
                results[2] = "";
            }
        }

        results[0] = results[0].replace(".", "\\.");
        for (int i = 0; i < results.length; i++) {
            results[i] = results[i].replace("*", ".*");
        }
        results[1] = results[1].replace("-", ".");

        return results;
    }

    public static void getDoc(String klass, String mname) throws IOException {
        klass = klass.substring(2).replace('.', '/') + ".doc";
        String[] lines;
        if (docMap.containsKey(klass)) {
            lines = docMap.get(klass);
        } else {
            File file = new File(docRoot + klass);
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<String> l = new ArrayList<String>();
            String line;
            line = br.readLine();
            while (line != null) {
                l.add(line);
                line = br.readLine();
            }
            lines = l.toArray(new String[0]);
            docMap.put(klass, lines);
            br.close();
        }
        if (mname.equals("Class_Doc")) {
            findClassDoc(lines, klass);
        } else findMethodDoc(lines, mname);
    }

    public static void findMethodDoc(String[] lines, String mname) throws IOException {
        ArrayList<String> docStr = new ArrayList<String>();
        boolean flag = false;
        for (String line : lines) {
            if (!flag && line.trim().equals("* #### " + mname)) {
                line = mname;
                flag = true;
            }
            if (flag) {
                if (line.trim().startsWith("* #### ")) {
                    break;
                }
                docStr.add(line);
            }
        }
        //Header
        clearConsole();
        if (docStr.size() == 0) {
            System.out.println("Doc not found for \033[1m" + mname + "\033[0m!");
            return;
        }
        prettyPrintDocs(docStr);
    }

    public static void findClassDoc(String[] lines, String klass) throws IOException {
        ArrayList<String> docStr = new ArrayList<String>();
        boolean flag = false;
        for (String line : lines) {
            if (!flag && (line.trim().equals("* * *") || line.trim().equals("* * * *"))) {
                line = klass;
                flag = true;
            }
            if (flag) {
                if (line.trim().startsWith("*     * ###")) {
                    break;
                }
                // System.out.println(line);
                docStr.add(line);
            }
        }
        //Header
        clearConsole();
        if (docStr.size() == 0) {
            System.out.println("Doc not found for \033[1m" + klass + "\033[0m!");
            return;
        }
        prettyPrintDocs(docStr);
    }

    public static void prettyPrintDocs(ArrayList<String> docStr) throws IOException {
        StringBuilder sb = new StringBuilder();

        System.out.println("Help on " + docStr.get(0));
        System.out.println("\n\033[1m" + docStr.get(2).trim() + "\033[0m");
        String line;
        boolean flag = true;
        for (int i = 3; i < docStr.size(); i++) {
            line = docStr.get(i);
            if (line.contains("Parameters:")) {
                line = line.replace("Parameters:", "\033[1mParameters:\033[0m");
                flag = false;
            } else if (line.contains("Specified by:")) {
                line = line.replace("Specified by:", "\033[1mSpecified by:\033[0m");
                flag = false;
            } else if (line.contains("Returns:")) {
                line = line.replace("Returns:", "\033[1mReturns:\033[0m");
                flag = false;
            } else if (line.contains("Throws:"))
                line = line.replace("Throws:", "\033[1mThrows:\033[0m");
            else if (line.contains("**Overrides:**"))
                flag = false;
            else if (line.contains("Since:")) {
                line = line.replace("Since:", "\033[1mSince:\033[0m");
                flag = false;
            } else if (line.contains("See Also:")) {
                line = line.replace("See Also:", "\033[1mSee Also:\033[0m");
                flag = false;
            }

            System.out.println(" " + line);
            if (flag)
                sb.append(line);

        }
        spokenText = sb.toString();
    }

    public static void prettyPrint(List<String> results, String query) {
        String printExp;
        if (!query.equals("")) {
            Pattern pattern = Pattern.compile("(" + query + ")", Pattern.CASE_INSENSITIVE);
            String match = query;
            Matcher matcher;

            ArrayList<String> strings = new ArrayList<String>(results.size());
            for (String temp : results) {
                matcher = pattern.matcher(temp);
                if (matcher.find()) {
                    match = matcher.group(1);
                }
                strings.add(temp.replaceFirst(match, "\033[1m" + match + "\033[0m"));
            }
            results = strings;
        }

        int ncols, height, rem, slck = 0;

        int width = 25;
        for (String result : results) {
            if (result.length() > width) {
                width = result.length();
            }
        }

        if (width > 25) {
            width += 5;
            ncols = 2;
            printExp = "%2d: %-" + width + "s %2d: %-" + width + "s \n";
        } else {
            ncols = 3;
            printExp = "%2d: %-25s %2d: %-25s %2d: %-25s \n";
        }

        int len = results.size();
        slck = height = len / ncols;
        rem = len % ncols;
        if (rem == 1) {
            height++;
        } else if (rem == 2) {
            height++;
            slck++;
        }

        int i = 0;
        if (width > 25) {
            while (len > 1) {
                System.out.printf(printExp, i, results.get(i), i + height, results.get(i + height));
                i++;
                len -= 2;
            }
        } else {
            while (len > 2) {
                System.out.printf(printExp, i, results.get(i), i + height, results.get(i + height), i
                        + height + slck, results.get(i + height + slck));
                i++;
                len -= 3;
            }
        }
        while (len-- > 0) {
            System.out.printf("%2d: %-" + width + "s ", i, results.get(i));
            i += height;
        }
        System.out.println();
    }

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static List<String> digClass(String qname, String filter) {
        boolean flag = false;
        Pattern pattern = null;
        Matcher matcher;
        String name;
        if (!filter.equals("")) {
            pattern = Pattern.compile("(" + filter + ")", Pattern.CASE_INSENSITIVE);
            flag = true;
        }
        Class klass;
        try {
            klass = Class.forName(qname.substring(2));
        } catch (ClassNotFoundException e) {
            return null;
        }
        System.out.println(klass.toString());
        Method[] methods = klass.getMethods();
        Set<String> set = new HashSet<String>();
        for (Method method : methods) {
            name = method.getName();
            if (!objMethods.contains(name)) {
                if (flag) {
                    if (name.equals(filter)) {
                        ArrayList<String> strings = new ArrayList<String>();
                        strings.add(name);
                        return strings;
                    }
                    matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        set.add(name);
                    }
                } else {
                    set.add(name);
                }
            }
        }

        ArrayList<String> names = new ArrayList<String>(set);
        Collections.sort(names);
        return names;
    }

}
