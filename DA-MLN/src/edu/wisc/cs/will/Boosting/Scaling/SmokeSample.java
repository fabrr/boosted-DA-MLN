package edu.wisc.cs.will.Boosting.Scaling;

// CLI

import org.apache.commons.cli.*;

// Condor
import edu.wisc.cs.will.Utils.condor.CondorFileReader;
import edu.wisc.cs.will.Utils.condor.CondorFileWriter;

// IO
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class SmokeSample {

    private static String userDir = "";
    private static String folder = "";

    private static Log log = new Log();

    private static class Smokes_GroundAtom {
        public static String predicateName = "smokes";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class R_Smokes_GroundAtom {
        public static String predicateName = "r_smokes";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class R_NotSmokes_GroundAtom {
        public static String predicateName = "r_not_smokes";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class NotSmokes_GroundAtom {
        public static String predicateName = "not_smokes";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Cancer_GroundAtom {
        public static String predicateName = "cancer";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class NotCancer_GroundAtom {
        public static String predicateName = "not_cancer";

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Friends_GroundAtom {
        public static String predicateName = "friends";

        public String _1_person;
        public String _2_person;

        public static String getKey(String person_1, String person_2) {
            return predicateName + "(" + person_1 + "," + person_2 + ").";
        }

        public String getKey() {
            return getKey(_1_person, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class DataSet {
        public HashSet<String> person_constants = new HashSet<>();

        public HashMap<String, Smokes_GroundAtom> smokes_groundAtoms = new HashMap<>();
        public HashMap<String, NotSmokes_GroundAtom> notSmokes_groundAtoms = new HashMap<>();

        public HashMap<String, R_Smokes_GroundAtom> r_smokes_groundAtoms = new HashMap<>();
        public HashMap<String, R_NotSmokes_GroundAtom> r_notSmokes_groundAtoms = new HashMap<>();

        public HashMap<String, Cancer_GroundAtom> cancer_groundAtoms = new HashMap<>();
        public HashMap<String, NotCancer_GroundAtom> notCancer_groundAtoms = new HashMap<>();

        public HashMap<String, Friends_GroundAtom> friends_groundAtoms = new HashMap<>();
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
            String splitPattern = "\\(|\\,|\\)\\.";
            while ((rawLine = reader.readLine()) != null) {
                line = rawLine.replaceAll("\\s+", "");

                // read "smokes"
                if (line.startsWith(Smokes_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create smokes ground atom
                    Smokes_GroundAtom smokes_groundAtom = new Smokes_GroundAtom();
                    smokes_groundAtom._1_person = person;
                    // add smokes ground atom
                    dataSet.smokes_groundAtoms.put(smokes_groundAtom.getKey(), smokes_groundAtom);
                }

                // read "not_smokes"
                if (line.startsWith(NotSmokes_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create not_smokes ground atom
                    NotSmokes_GroundAtom notSmokes_groundAtom = new NotSmokes_GroundAtom();
                    notSmokes_groundAtom._1_person = person;
                    // add not_smokes ground atom
                    dataSet.notSmokes_groundAtoms.put(notSmokes_groundAtom.getKey(), notSmokes_groundAtom);
                }

                // read "r_smokes"
                if (line.startsWith(R_Smokes_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create smokes ground atom
                    R_Smokes_GroundAtom r_smokes_groundAtom = new R_Smokes_GroundAtom();
                    r_smokes_groundAtom._1_person = person;
                    // add smokes ground atom
                    dataSet.r_smokes_groundAtoms.put(r_smokes_groundAtom.getKey(), r_smokes_groundAtom);
                }

                // read "r_not_smokes"
                if (line.startsWith(R_NotSmokes_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create not_smokes ground atom
                    R_NotSmokes_GroundAtom r_notSmokes_groundAtom = new R_NotSmokes_GroundAtom();
                    r_notSmokes_groundAtom._1_person = person;
                    // add not_smokes ground atom
                    dataSet.r_notSmokes_groundAtoms.put(r_notSmokes_groundAtom.getKey(), r_notSmokes_groundAtom);
                }

                // read "cancer"
                if (line.startsWith(Cancer_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create cancer ground atom
                    Cancer_GroundAtom cancer_groundAtom = new Cancer_GroundAtom();
                    cancer_groundAtom._1_person = person;
                    // add cancer ground atom
                    dataSet.cancer_groundAtoms.put(cancer_groundAtom.getKey(), cancer_groundAtom);
                }

                // read "not_cancer"
                if (line.startsWith(NotCancer_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create not_cancer ground atom
                    NotCancer_GroundAtom notCancer_groundAtom = new NotCancer_GroundAtom();
                    notCancer_groundAtom._1_person = person;
                    // add not_cancer ground atom
                    dataSet.notCancer_groundAtoms.put(notCancer_groundAtom.getKey(), notCancer_groundAtom);
                }

                // read "friends"
                if (line.startsWith(Friends_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person_1 = splitLine[1];
                    String person_2 = splitLine[2];
                    // add person_1 constant
                    dataSet.person_constants.add(person_1);
                    // add person_2 constant
                    dataSet.person_constants.add(person_2);
                    // create friends ground atom
                    Friends_GroundAtom friends_groundAtom = new Friends_GroundAtom();
                    friends_groundAtom._1_person = person_1;
                    friends_groundAtom._2_person = person_2;
                    // add friends ground atom
                    dataSet.friends_groundAtoms.put(friends_groundAtom.getKey(), friends_groundAtom);
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

    private static DataSet createFullDataSet() {

        log.writeln("");
        log.writeln("Create Full Dataset");

        // READ DATASET
        log.writeln("");
        log.write("Reading Dataset...");

        // read db file and create dataset
        String dbFile = userDir + folder + "/db.txt";
        DataSet dataSet = readDataSet(dbFile);
        log.writeln(" done!");

        log.writeln("");
        log.writeln("Dataset loaded!");

        log.writeln("Person constants: " + dataSet.person_constants.size());

        log.writeln("Smokes ground atoms: " + dataSet.smokes_groundAtoms.size());
        log.writeln("NotSmokes ground atoms: " + dataSet.notSmokes_groundAtoms.size());

        log.writeln("Cancer ground atoms: " + dataSet.cancer_groundAtoms.size());
        log.writeln("NotCancer ground atoms: " + dataSet.notCancer_groundAtoms.size());

        log.writeln("Friends ground atoms: " + dataSet.friends_groundAtoms.size());

        return dataSet;
    }

    private static DataSet sampleDataSet(DataSet dataSet, double samplePerson) {
        log.writeln("");
        log.writeln("Sample Dataset");
        log.writeln("Sample person: " + samplePerson);

        DataSet sampleDataSet = new DataSet();

        log.writeln("");
        log.write("Sampling constants...");

        // SAMPLE CONSTANTS

        // sample persons constants
        ArrayList<String> person_constants = new ArrayList<>();
        int personSampleSize = (int) Math.ceil(samplePerson * (dataSet.person_constants.size()));
        ArrayList<String> persons_copy = new ArrayList<>();
        for (String person : dataSet.person_constants) persons_copy.add(person);
        for (int i = 0; i < personSampleSize && persons_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, persons_copy.size());
            String randomPerson = persons_copy.remove(randomIndex);
            person_constants.add(randomPerson);
        }
        log.writeln(" done!");

        log.write("Adding relevant constants and ground atoms...");

        // ADD GROUND ATOMS

        // smokes
        for (Smokes_GroundAtom smokes_groundAtom : dataSet.smokes_groundAtoms.values()) {
            if (person_constants.contains(smokes_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(smokes_groundAtom._1_person);
                sampleDataSet.smokes_groundAtoms.put(smokes_groundAtom.getKey(), smokes_groundAtom);
            }
        }
        // not smokes
        for (NotSmokes_GroundAtom notSmokes_groundAtom : dataSet.notSmokes_groundAtoms.values()) {
            if (person_constants.contains(notSmokes_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(notSmokes_groundAtom._1_person);
                sampleDataSet.notSmokes_groundAtoms.put(notSmokes_groundAtom.getKey(), notSmokes_groundAtom);
            }
        }

        // smokes
        for (R_Smokes_GroundAtom r_smokes_groundAtom : dataSet.r_smokes_groundAtoms.values()) {
            if (person_constants.contains(r_smokes_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(r_smokes_groundAtom._1_person);
                sampleDataSet.r_smokes_groundAtoms.put(r_smokes_groundAtom.getKey(), r_smokes_groundAtom);
            }
        }
        // not smokes
        for (R_NotSmokes_GroundAtom r_notSmokes_groundAtom : dataSet.r_notSmokes_groundAtoms.values()) {
            if (person_constants.contains(r_notSmokes_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(r_notSmokes_groundAtom._1_person);
                sampleDataSet.r_notSmokes_groundAtoms.put(r_notSmokes_groundAtom.getKey(), r_notSmokes_groundAtom);
            }
        }

        // cancer
        for (Cancer_GroundAtom cancer_groundAtom : dataSet.cancer_groundAtoms.values()) {
            if (person_constants.contains(cancer_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(cancer_groundAtom._1_person);
                sampleDataSet.cancer_groundAtoms.put(cancer_groundAtom.getKey(), cancer_groundAtom);
            }
        }
        // not cancer
        for (NotCancer_GroundAtom notCancer_groundAtom : dataSet.notCancer_groundAtoms.values()) {
            if (person_constants.contains(notCancer_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(notCancer_groundAtom._1_person);
                sampleDataSet.notCancer_groundAtoms.put(notCancer_groundAtom.getKey(), notCancer_groundAtom);
            }
        }

        // friends
        for (Friends_GroundAtom friends_groundAtom : dataSet.friends_groundAtoms.values()) {
            if (person_constants.contains(friends_groundAtom._1_person) &&
                    person_constants.contains(friends_groundAtom._2_person)) {
                sampleDataSet.person_constants.add(friends_groundAtom._1_person);
                sampleDataSet.person_constants.add(friends_groundAtom._2_person);
                sampleDataSet.friends_groundAtoms.put(friends_groundAtom.getKey(), friends_groundAtom);
            }
        }

        log.writeln(" done!");

        log.writeln("");
        log.writeln("Sample Dataset created!");

        log.writeln("Person constants: " + sampleDataSet.person_constants.size() + "/" + dataSet.person_constants.size());

        log.writeln("Smokes ground atoms: " + sampleDataSet.smokes_groundAtoms.size() + "/" + dataSet.smokes_groundAtoms.size());
        log.writeln("NotSmokes ground atoms: " + sampleDataSet.notSmokes_groundAtoms.size() + "/" + dataSet.notSmokes_groundAtoms.size());

        log.writeln("Cancer ground atoms: " + sampleDataSet.cancer_groundAtoms.size() + "/" + dataSet.cancer_groundAtoms.size());
        log.writeln("NotCancer ground atoms: " + sampleDataSet.notCancer_groundAtoms.size() + "/" + dataSet.notCancer_groundAtoms.size());

        log.writeln("Friends ground atoms: " + sampleDataSet.friends_groundAtoms.size() + "/" + dataSet.friends_groundAtoms.size());

        return sampleDataSet;
    }

    public static void writeDataSet(DataSet dataSet, String prefix) {

        // WRITE FILES
        log.writeln("");
        log.writeln("Write Dataset");
        log.writeln("Data prefix: " + prefix);

        // facts
        String factsFile = userDir + folder + "/" + prefix + "/" + prefix + "_facts.txt";
        ArrayList<HashMap> facts = new ArrayList<>();
        facts.add(dataSet.smokes_groundAtoms);
        facts.add(dataSet.notSmokes_groundAtoms);
        facts.add(dataSet.r_smokes_groundAtoms);
        facts.add(dataSet.r_notSmokes_groundAtoms);
        facts.add(dataSet.cancer_groundAtoms);
        facts.add(dataSet.notCancer_groundAtoms);
        facts.add(dataSet.friends_groundAtoms);
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

        log.writeln("");
        log.writeln("Create " + graphName + " Graph");

        // open file stream
        String fileName = graphName + "_graph.dot";
        String graphFile = userDir + folder + "/graphs/" + fileName;
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
            writer.write("graph SmokeGen {\n\n");

            // Person nodes
            log.writeln("Creating " + dataSet.person_constants.size() + " Person nodes");
            writer.write("\t// Person nodes\n");
            writer.write("\tsubgraph person {\n");
            writer.write("\t\tnode[style=filled]\n");
            for (String person : dataSet.person_constants) {
                String attributes = "";
                if (dataSet.smokes_groundAtoms.containsKey(Smokes_GroundAtom.getKey(person))) {
                    attributes += " fillcolor=crimson";
                } else {
                    attributes += " fillcolor=aquamarine3";
                }
                writer.write("\t\t" + person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Friends edges
            log.writeln("Creating " + dataSet.friends_groundAtoms.size() + " Friends edges");
            writer.write("\t// Friends edges\n");
            for (Friends_GroundAtom friends_groundAtom : dataSet.friends_groundAtoms.values()) {
                if (friends_groundAtom._1_person.compareTo(friends_groundAtom._2_person) < 0) {
                    String attributes = "";
                    writer.write("\t" + friends_groundAtom._1_person + "--" + friends_groundAtom._2_person);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
            }
            writer.write("\n");

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.write("Writing '" + fileName + "' file...");

        // close file stream
        try {
            writer.close();
        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.writeln(" done!");
    }

    public static void createCombinedGraph(DataSet dataSet, DataSet sampleDataSet, String graphName) {

        log.writeln("");
        log.writeln("Create " + graphName + " Graph");

        // open file stream
        String fileName = graphName + "_graph.dot";
        String graphFile = userDir + folder + "/graphs/" + fileName;
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
            writer.write("graph SmokeGen {\n\n");

            // Person nodes
            log.writeln("Creating " + dataSet.person_constants.size() + " Person nodes");
            writer.write("\t// Person nodes\n");
            writer.write("\tsubgraph person {\n");
            writer.write("\t\tnode[style=filled]\n");
            for (String person : dataSet.person_constants) {
                String attributes = "";
                if (sampleDataSet.person_constants.contains(person)) {
                    attributes += " penwidth=5";
                    if (dataSet.smokes_groundAtoms.containsKey(Smokes_GroundAtom.getKey(person))) {
                        attributes += " fillcolor=crimson";
                    } else {
                        attributes += " fillcolor=aquamarine3";
                    }
                } else {
                    if (dataSet.smokes_groundAtoms.containsKey(Smokes_GroundAtom.getKey(person))) {
                        attributes += " fillcolor=lightcoral";
                    } else {
                        attributes += " fillcolor=lightcyan";
                    }
                }
                writer.write("\t\t" + person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Friends edges
            log.writeln("Creating " + dataSet.friends_groundAtoms.size() + " Friends edges");
            writer.write("\t// Friends edges\n");
            for (Friends_GroundAtom friends_groundAtom : dataSet.friends_groundAtoms.values()) {
                if (friends_groundAtom._1_person.compareTo(friends_groundAtom._2_person) < 0) {
                    String attributes = "";
                    if (sampleDataSet.friends_groundAtoms.containsKey(friends_groundAtom.getKey())) {
                        attributes += " penwidth=5";
                    }
                    writer.write("\t" + friends_groundAtom._1_person + "--" + friends_groundAtom._2_person);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
            }
            writer.write("\n");

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.write("Writing '" + fileName + "' file...");

        // close file stream
        try {
            writer.close();
        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.writeln(" done!");
    }

    public static void main(String[] args) {
        try {

            // Parse args
            Options options = new Options();

            options.addOption("dataset", true, "path to dataset folder");

            options.addOption("samplePerson", true, "double - person sample ratio");

            options.addOption("createGraphs", false, "path to dataset folder");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            folder = "/" + cmd.getOptionValue("dataset");

            // Sample values
            double samplePerson = 1.0;
            if (cmd.hasOption("samplePerson")) {
                samplePerson = Double.parseDouble(cmd.getOptionValue("samplePerson"));
            }

            // Open logger
            log.open(userDir + folder + "/sample_log.txt");
            log.writeln("");
            log.writeln("SmokeSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + folder);

            // Create full dataset
            DataSet fullDataSet = createFullDataSet();
            writeDataSet(fullDataSet, "test");

            // Create sample dataset
            DataSet sampleDataSet = sampleDataSet(fullDataSet, samplePerson);
            writeDataSet(sampleDataSet, "train");

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
                createGraph(fullDataSet, "full");
                createGraph(sampleDataSet, "sample");
                createCombinedGraph(fullDataSet, sampleDataSet, "combined");
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
