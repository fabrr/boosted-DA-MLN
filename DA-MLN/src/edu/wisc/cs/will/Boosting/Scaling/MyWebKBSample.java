package edu.wisc.cs.will.Boosting.Scaling;

import edu.wisc.cs.will.Utils.condor.CondorFileReader;
import edu.wisc.cs.will.Utils.condor.CondorFileWriter;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class MyWebKBSample {

    private static boolean filterCategory = false;

    private static String userDir = "";
    private static String folder = "";

    private static Log log = new Log();

    private static class Page_object {
        public String page;

        public HashMap<String, Has_GroundAtom> has_groundAtoms = new HashMap<>();
        public HashMap<String, LinkTo_GroundAtom> linkTo_groundAtoms = new HashMap<>();
        public HashMap<String, Category_GroundAtom> category_groundAtoms = new HashMap<>();

        public static String getKey(String page) {
            return page;
        }

        public String getKey() {
            return getKey(page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Category_GroundAtom {
        public static String course = "Course";
        public static String department = "Department";
        public static String faculty = "Faculty";
        public static String person = "Person";
        public static String researchproject = "ResearchProject";
        public static String staff = "Staff";
        public static String student = "Student";

        public String category = "";
        public String _1_page;

        public String getRecursive() {
            return "r_" + getKey(category, _1_page);
        }

        public static String getKey(String category, String person) {
            return category + "(" + person + ").";
        }

        public String getKey() {
            return getKey(category, _1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Has_GroundAtom {
        public static String readName = "Has";
        public static String predicateName = "has";

        public String _1_page;
        public String _2_word;

        public static String getKey(String page, String word) {
            return predicateName + "(" + page + "," + word + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_word);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class LinkTo_GroundAtom {
        public static String readName = "LinkTo";
        public static String predicateName = "linkto";

        public String _1_page;
        public String _2_page;

        public static String getKey(String _1_page, String _2_page) {
            return predicateName + "(" + _1_page + "," + _2_page + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class DataSet {
        public HashMap<String, Page_object> page_objects = new HashMap<>();
        public HashSet<String> page_constants = new HashSet<>();
        public HashSet<String> word_constants = new HashSet<>();

        public HashMap<String, Category_GroundAtom> category_groundAtoms = new HashMap<>();
        public HashMap<String, Has_GroundAtom> has_groundAtoms = new HashMap<>();
        public HashMap<String, LinkTo_GroundAtom> linkTo_groundAtoms = new HashMap<>();
    }

    private static DataSet readDataSet(String filePath) {
        DataSet dataSet = new DataSet();

        // open read stream
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new CondorFileReader(filePath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // read GroundAtoms
        String rawLine, line;
        try {
            String splitPattern = "\\(|\\,|\\)";
            String lettersPattern = "[^a-zA-Z]";
            while ((rawLine = reader.readLine()) != null) {
                line = rawLine.replaceAll("\\s+", "");

                // read "Category"
                if (line.startsWith(Category_GroundAtom.course) ||
                        line.startsWith(Category_GroundAtom.department) ||
                        line.startsWith(Category_GroundAtom.faculty) ||
                        line.startsWith(Category_GroundAtom.person) ||
                        line.startsWith(Category_GroundAtom.researchproject) ||
                        line.startsWith(Category_GroundAtom.staff) ||
                        line.startsWith(Category_GroundAtom.student)
                ) {
                    String[] splitLine = line.split(splitPattern);
                    String category = splitLine[0].toLowerCase();
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.page_constants.add(page);
                    Category_GroundAtom category_groundAtom = new Category_GroundAtom();
                    category_groundAtom._1_page = page;
                    category_groundAtom.category = category;
                    dataSet.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);

                    Page_object page_object = dataSet.page_objects.get(Page_object.getKey(page));
                    if (page_object == null) {
                        page_object = new Page_object();
                        page_object.page = page;
                        dataSet.page_objects.put(page_object.getKey(), page_object);
                    }
                    page_object.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);

                }

                // read "Has"
                if (line.startsWith(Has_GroundAtom.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String word = splitLine[1].replaceAll(lettersPattern, "");
                    String page = splitLine[2].replaceAll(lettersPattern, "");
                    dataSet.word_constants.add(word);
                    dataSet.page_constants.add(page);
                    Has_GroundAtom has_groundAtom = new Has_GroundAtom();
                    has_groundAtom._1_page = page;
                    has_groundAtom._2_word = word;
                    dataSet.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);

                    Page_object page_object = dataSet.page_objects.get(Page_object.getKey(page));
                    if (page_object == null) {
                        page_object = new Page_object();
                        page_object.page = page;
                        dataSet.page_objects.put(page_object.getKey(), page_object);
                    }
                    page_object.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);
                }

                // read "LinkTo"
                if (line.startsWith(LinkTo_GroundAtom.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page_1 = splitLine[2].replaceAll(lettersPattern, "");
                    String page_2 = splitLine[3].replaceAll(lettersPattern, "");
                    dataSet.page_constants.add(page_1);
                    dataSet.page_constants.add(page_2);
                    LinkTo_GroundAtom linkTo_groundAtom = new LinkTo_GroundAtom();
                    linkTo_groundAtom._1_page = page_2;
                    linkTo_groundAtom._2_page = page_1;
                    dataSet.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);

                    Page_object page_object_1 = dataSet.page_objects.get(Page_object.getKey(page_1));
                    if (page_object_1 == null) {
                        page_object_1 = new Page_object();
                        page_object_1.page = page_1;
                        dataSet.page_objects.put(page_object_1.getKey(), page_object_1);
                    }
                    page_object_1.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
                    Page_object page_object_2 = dataSet.page_objects.get(Page_object.getKey(page_2));
                    if (page_object_2 == null) {
                        page_object_2 = new Page_object();
                        page_object_2.page = page_2;
                        dataSet.page_objects.put(page_object_2.getKey(), page_object_2);
                    }
                    page_object_2.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close read stream
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dataSet;
    }

    private static DataSet modifyDataSet(DataSet raw_dataSet) {
        DataSet dataSet = new DataSet();

        // filter pages
        for (Page_object page_object : raw_dataSet.page_objects.values()) {
            if (filterCategory) {
                if (page_object.category_groundAtoms.size() != 0) {
                    dataSet.page_constants.add(page_object.page);
                }
            } else {
                dataSet.page_constants.add(page_object.page);
            }
        }

        for (Category_GroundAtom category_groundAtom : raw_dataSet.category_groundAtoms.values()) {
            if (dataSet.page_constants.contains(category_groundAtom._1_page)) {
                dataSet.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);
            }
        }

        for (Has_GroundAtom has_groundAtom : raw_dataSet.has_groundAtoms.values()) {
            if (dataSet.page_constants.contains(has_groundAtom._1_page)) {
                dataSet.word_constants.add(has_groundAtom._2_word);
                dataSet.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);
            }
        }

        for (LinkTo_GroundAtom linkTo_groundAtom : raw_dataSet.linkTo_groundAtoms.values()) {
            if (dataSet.page_constants.contains(linkTo_groundAtom._1_page) && dataSet.page_constants.contains(linkTo_groundAtom._2_page)) {
                dataSet.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
            }
        }

        return dataSet;
    }

    private static DataSet createFullDataSet() {
        log.writeln("");
        log.writeln("Create Full Dataset");

        // READ DATASET
        log.writeln("");
        log.write("Reading Dataset...");

        // read db file and create dataset
        String dbFile = userDir + folder + "/db.txt";
        DataSet raw_dataSet = readDataSet(dbFile);
        log.writeln(" done!");

        log.write("Modifying Dataset...");
        DataSet dataSet = modifyDataSet(raw_dataSet);
        log.writeln(" done!");

        log.writeln("");
        log.writeln("Dataset loaded!");

        log.writeln("Page constants: " + dataSet.page_constants.size() + "/" + raw_dataSet.page_constants.size());
        log.writeln("Word constants: " + dataSet.word_constants.size() + "/" + raw_dataSet.word_constants.size());

        log.writeln("Category ground atoms: " + dataSet.category_groundAtoms.size() + "/" + raw_dataSet.category_groundAtoms.size());
        log.writeln("Has ground atoms: " + dataSet.has_groundAtoms.size() + "/" + raw_dataSet.has_groundAtoms.size());
        log.writeln("LinkTo ground atoms: " + dataSet.linkTo_groundAtoms.size() + "/" + raw_dataSet.linkTo_groundAtoms.size());

        return dataSet;
    }

    private static DataSet sampleDataSet(DataSet dataSet, double samplePage, double sampleWord) {
        log.writeln("");
        log.writeln("Sample Dataset");
        log.writeln("Sample page: " + samplePage);
        log.writeln("Sample word: " + sampleWord);

        DataSet sampleDataSet = new DataSet();

        log.writeln("");
        log.write("Sampling constants...");

        // SAMPLE CONSTANTS

        // sample page constants
        ArrayList<String> page_constants = new ArrayList<>();
        int pageSampleSize = (int) Math.ceil(samplePage * (dataSet.page_constants.size()));
        ArrayList<String> page_copy = new ArrayList<>();
        for (String page : dataSet.page_constants) page_copy.add(page);
        for (int i = 0; i < pageSampleSize && page_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, page_copy.size());
            String randomPage = page_copy.remove(randomIndex);
            page_constants.add(randomPage);
        }
        // sample word constants
        ArrayList<String> word_constants = new ArrayList<>();
        int wordSampleSize = (int) Math.ceil(sampleWord * (dataSet.word_constants.size()));
        ArrayList<String> word_copy = new ArrayList<>();
        for (String word : dataSet.word_constants) word_copy.add(word);
        for (int i = 0; i < wordSampleSize && word_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, word_copy.size());
            String randomWord = word_copy.remove(randomIndex);
            word_constants.add(randomWord);
        }

        log.writeln(" done!");

        log.write("Adding relevant constants and ground atoms...");

        // ADD GROUND ATOMS

        // category
        for (Category_GroundAtom category_groundAtom : dataSet.category_groundAtoms.values()) {
            if (page_constants.contains(category_groundAtom._1_page)) {
                sampleDataSet.page_constants.add(category_groundAtom._1_page);
                sampleDataSet.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);
            }
        }

        // has
        for (Has_GroundAtom has_groundAtom : dataSet.has_groundAtoms.values()) {
            if (page_constants.contains(has_groundAtom._1_page) &&
                    word_constants.contains(has_groundAtom._2_word)) {
                sampleDataSet.page_constants.add(has_groundAtom._1_page);
                sampleDataSet.word_constants.add(has_groundAtom._2_word);
                sampleDataSet.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);
            }
        }

        // linkto
        for (LinkTo_GroundAtom linkTo_groundAtom : dataSet.linkTo_groundAtoms.values()) {
            if (page_constants.contains(linkTo_groundAtom._1_page) &&
                    page_constants.contains(linkTo_groundAtom._2_page)) {
                sampleDataSet.page_constants.add(linkTo_groundAtom._1_page);
                sampleDataSet.page_constants.add(linkTo_groundAtom._2_page);
                sampleDataSet.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
            }
        }

        log.writeln(" done!");

        log.writeln("");
        log.writeln("Sample Dataset created!");

        log.writeln("Page constants: " + sampleDataSet.page_constants.size() + "/" + dataSet.page_constants.size());
        log.writeln("Word constants: " + sampleDataSet.word_constants.size() + "/" + dataSet.word_constants.size());

        log.writeln("Category ground atoms: " + sampleDataSet.category_groundAtoms.size() + "/" + dataSet.category_groundAtoms.size());
        log.writeln("Has ground atoms: " + sampleDataSet.has_groundAtoms.size() + "/" + dataSet.has_groundAtoms.size());
        log.writeln("LinkTo ground atoms: " + sampleDataSet.linkTo_groundAtoms.size() + "/" + dataSet.linkTo_groundAtoms.size());

        return sampleDataSet;
    }

    private static void writeGroundAtoms(String filePath, ArrayList<HashMap> groundAtomSets) {
        // open file stream
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new CondorFileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write domain sizes
        try {
            for (HashMap groundAtoms : groundAtomSets) {
                writer.newLine();
                for (Object groundAtom : groundAtoms.values()) {
                    writer.write(groundAtom.toString());
                    writer.newLine();
                    if (groundAtom instanceof Category_GroundAtom) {
                        writer.write(((Category_GroundAtom) groundAtom).getRecursive());
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close file stream
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeDataSet(DataSet dataSet, String prefix) {

        // WRITE FILES
        log.writeln("");
        log.writeln("Write Dataset");
        log.writeln("Data prefix: " + prefix);

        // facts
        String factsFile = userDir + folder + "/" + prefix + "/" + prefix + "_facts.txt";
        ArrayList<HashMap> facts = new ArrayList<>();
        facts.add(dataSet.category_groundAtoms);
        facts.add(dataSet.has_groundAtoms);
        facts.add(dataSet.linkTo_groundAtoms);
        log.write("Writing '" + prefix + "_facts.txt' file...");
        writeGroundAtoms(factsFile, facts);
        log.writeln(" done!");

        // pos
        String posFile = userDir + folder + "/" + prefix + "/" + prefix + "_pos.txt";
        ArrayList<HashMap> pos = new ArrayList<>();
        log.write("Writing '" + prefix + "_pos.txt' file...");
        writeGroundAtoms(posFile, pos);
        log.writeln(" done!");

        // neg
        String negFile = userDir + folder + "/" + prefix + "/" + prefix + "_neg.txt";
        ArrayList<HashMap> neg = new ArrayList<>();
        log.write("Writing '" + prefix + "_neg.txt' file...");
        writeGroundAtoms(negFile, neg);
        log.writeln(" done!");
    }

    public static void createGraph(DataSet dataSet, String graphName) {

        // open file stream
        String fileName = graphName + "_graph.dot";
        String graphFile = userDir + "/" + folder + "/graphs/" + fileName;
        BufferedWriter writer = null;
        try {
            File file = new File(graphFile);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new CondorFileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write domain sizes
        try {
            writer.write("graph MyWebKB {\n\n");

            writer.write("\t// Page nodes\n");

            writer.write("\tsubgraph Pages {\n");
            writer.write("\t\tnode[style=filled label=\"\"]\n");
            for (String page : dataSet.page_constants) {
                String attributes = "";
                if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("course", page))) {
                    attributes += "fillcolor=darkgreen";
                    attributes += " tooltip=\"" + page + " (course)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("department", page))) {
                    attributes += "fillcolor=blueviolet";
                    attributes += " tooltip=\"" + page + " (department)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("faculty", page))) {
                    attributes += "fillcolor=darkorange";
                    attributes += " tooltip=\"" + page + " (faculty)" + "\"";
//                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("person", page))) {
//                    attributes += "fillcolor=blue";
//                    attributes += " tooltip=\"" + page + " (course)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("researchproject", page))) {
                    attributes += "fillcolor=brown";
                    attributes += " tooltip=\"" + page + " (researchproject)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("staff", page))) {
                    attributes += "fillcolor=chartreuse3";
                    attributes += " tooltip=\"" + page + " (staff)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("student", page))) {
                    attributes += "fillcolor=cornflowerblue";
                    attributes += " tooltip=\"" + page + " (student)" + "\"";
                } else {
                    attributes += " tooltip=\"" + page + "\"";
                }
                writer.write("\t\t" + page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            writer.write("\t// LinkTo edges\n");
            for (LinkTo_GroundAtom linkTo_groundAtom : dataSet.linkTo_groundAtoms.values()) {
                String attributes = "";
                writer.write("\t" + linkTo_groundAtom._1_page + "--" + linkTo_groundAtom._2_page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close file stream
        try {
            writer.close();
        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {

            // Parse args
            Options options = new Options();

            options.addOption("dataset", true, "path to dataset folder");
            options.addOption("samplePage", true, "double - page sample ratio");
            options.addOption("createGraphs", false, "path to dataset folder");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            folder = "/" + cmd.getOptionValue("dataset");

            // Sample values
            double samplePage = 1.0;
            if (cmd.hasOption("samplePage")) {
                samplePage = Double.parseDouble(cmd.getOptionValue("samplePage"));
            }

            // Open logger
            log.open(userDir + folder + "/sample_log.txt");
            log.writeln("");
            log.writeln("MyWebKBSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + folder);

            // Create full dataset
//            filterCategory = true;
            DataSet fullDataSet = createFullDataSet();
            DataSet presampledDataSet = sampleDataSet(fullDataSet, 1.0, 1.0);
            writeDataSet(presampledDataSet, "test");

            // Create sample dataset
            DataSet sampleDataSet = sampleDataSet(presampledDataSet, samplePage, 1.0);
            writeDataSet(sampleDataSet, "train");

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
//                createGraph(fullDataSet, "full");
                createGraph(sampleDataSet, "sample");
//                createCombinedGraph(fullDataSet, sampleDataSet, "combined");
            }

            // Exit
            log.writeln("");
            log.writeln("Exiting main...");
            log.close();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
