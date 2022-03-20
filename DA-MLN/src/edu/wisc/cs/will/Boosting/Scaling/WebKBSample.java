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

public class WebKBSample {

    private static String userDir = "";
    private static String folder = "";

    private static Log log = new Log();

    private static class Student_GroundAtom {
        public static String predicateName = "student";

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

    private static class Faculty_GroundAtom {
        public static String predicateName = "faculty";

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

    private static class CourseProf_GroundAtom {
        public static String predicateName = "courseprof";

        public String _1_course;
        public String _2_person;

        public static String getKey(String course, String person) {
            return predicateName + "(" + course + "," + person + ").";
        }

        public String getKey() {
            return getKey(_1_course, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class CourseTA_GroundAtom {
        public static String predicateName = "courseta";

        public String _1_course;
        public String _2_person;

        public static String getKey(String course, String person) {
            return predicateName + "(" + course + "," + person + ").";
        }

        public String getKey() {
            return getKey(_1_course, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Project_GroundAtom {
        public static String predicateName = "project";

        public String _1_project;
        public String _2_person;

        public static String getKey(String project, String person) {
            return predicateName + "(" + project + "," + person + ").";
        }

        public String getKey() {
            return getKey(_1_project, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class DataSet {

        public HashSet<String> course_constants = new HashSet<>();
        public HashSet<String> person_constants = new HashSet<>();
        public HashSet<String> project_constants = new HashSet<>();

        public HashMap<String, Student_GroundAtom> student_groundAtoms = new HashMap<>();
        public HashMap<String, Faculty_GroundAtom> faculty_groundAtoms = new HashMap<>();
        public HashMap<String, CourseProf_GroundAtom> courseProf_groundAtoms = new HashMap<>();
        public HashMap<String, CourseTA_GroundAtom> courseTA_groundAtoms = new HashMap<>();
        public HashMap<String, Project_GroundAtom> project_groundAtoms = new HashMap<>();

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

                // read "student"
                if (line.startsWith(Student_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create actor ground atom
                    Student_GroundAtom student_groundAtom = new Student_GroundAtom();
                    student_groundAtom._1_person = person;
                    // add actor ground atom
                    dataSet.student_groundAtoms.put(student_groundAtom.getKey(), student_groundAtom);
                }

                // read "faculty"
                if (line.startsWith(Faculty_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String person = splitLine[1];
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create actor ground atom
                    Faculty_GroundAtom faculty_groundAtom = new Faculty_GroundAtom();
                    faculty_groundAtom._1_person = person;
                    // add actor ground atom
                    dataSet.faculty_groundAtoms.put(faculty_groundAtom.getKey(), faculty_groundAtom);
                }

                // read "courseprof"
                if (line.startsWith(CourseProf_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String course = splitLine[1];
                    String person = splitLine[2];
                    // add course constant
                    dataSet.course_constants.add(course);
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create courseprof ground atom
                    CourseProf_GroundAtom courseProf_groundAtom = new CourseProf_GroundAtom();
                    courseProf_groundAtom._1_course = course;
                    courseProf_groundAtom._2_person = person;
                    // add courseprof ground atom
                    dataSet.courseProf_groundAtoms.put(courseProf_groundAtom.getKey(), courseProf_groundAtom);
                }

                // read "courseta"
                if (line.startsWith(CourseTA_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String course = splitLine[1];
                    String person = splitLine[2];
                    // add course constant
                    dataSet.course_constants.add(course);
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create courseta ground atom
                    CourseTA_GroundAtom courseTA_groundAtom = new CourseTA_GroundAtom();
                    courseTA_groundAtom._1_course = course;
                    courseTA_groundAtom._2_person = person;
                    // add courseta ground atom
                    dataSet.courseTA_groundAtoms.put(courseTA_groundAtom.getKey(), courseTA_groundAtom);
                }

                // read "project"
                if (line.startsWith(Project_GroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String project = splitLine[1];
                    String person = splitLine[2];
                    // add course constant
                    dataSet.project_constants.add(project);
                    // add person constant
                    dataSet.person_constants.add(person);
                    // create project ground atom
                    Project_GroundAtom project_groundAtom = new Project_GroundAtom();
                    project_groundAtom._1_project = project;
                    project_groundAtom._2_person = person;
                    // add project ground atom
                    dataSet.project_groundAtoms.put(project_groundAtom.getKey(), project_groundAtom);
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

        log.writeln("Course constants: " + dataSet.course_constants.size());
        log.writeln("Person constants: " + dataSet.person_constants.size());
        log.writeln("Project constants: " + dataSet.project_constants.size());

        log.writeln("Student ground atoms: " + dataSet.student_groundAtoms.size());
        log.writeln("Faculty ground atoms: " + dataSet.faculty_groundAtoms.size());
        log.writeln("CourseProf ground atoms: " + dataSet.courseProf_groundAtoms.size());
        log.writeln("CourseTA ground atoms: " + dataSet.courseTA_groundAtoms.size());
        log.writeln("Project ground atoms: " + dataSet.project_groundAtoms.size());

        return dataSet;
    }

    private static DataSet sampleDataSet(DataSet dataSet, double samplePerson, double sampleCourse, double sampleProject) {
        log.writeln("");
        log.writeln("Sample Dataset");
        log.writeln("Sample person: " + samplePerson);
        log.writeln("Sample course: " + sampleCourse);
        log.writeln("Sample project: " + sampleProject);

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

        // sample course constants
        ArrayList<String> course_constants = new ArrayList<>();
        int courseSampleSize = (int) Math.ceil(sampleCourse * (dataSet.course_constants.size()));
        ArrayList<String> courses_copy = new ArrayList<>();
        for (String course : dataSet.course_constants) courses_copy.add(course);
        for (int i = 0; i < courseSampleSize && courses_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, courses_copy.size());
            String randomCourse = courses_copy.remove(randomIndex);
            course_constants.add(randomCourse);
        }

        // sample project constants
        ArrayList<String> project_constants = new ArrayList<>();
        int projectSampleSize = (int) Math.ceil(sampleProject * (dataSet.project_constants.size()));
        ArrayList<String> projects_copy = new ArrayList<>();
        for (String project : dataSet.project_constants) projects_copy.add(project);
        for (int i = 0; i < projectSampleSize && projects_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, projects_copy.size());
            String randomProject = projects_copy.remove(randomIndex);
            project_constants.add(randomProject);
        }
        log.writeln(" done!");

        log.write("Adding relevant constants and ground atoms...");

        // ADD GROUND ATOMS

        // student
        for (Student_GroundAtom student_groundAtom : dataSet.student_groundAtoms.values()) {
            if (person_constants.contains(student_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(student_groundAtom._1_person);
                sampleDataSet.student_groundAtoms.put(student_groundAtom.getKey(), student_groundAtom);
            }
        }

        // faculty
        for (Faculty_GroundAtom faculty_groundAtom : dataSet.faculty_groundAtoms.values()) {
            if (person_constants.contains(faculty_groundAtom._1_person)) {
                sampleDataSet.person_constants.add(faculty_groundAtom._1_person);
                sampleDataSet.faculty_groundAtoms.put(faculty_groundAtom.getKey(), faculty_groundAtom);
            }
        }

        // courseprof
        for (CourseProf_GroundAtom courseProf_groundAtom : dataSet.courseProf_groundAtoms.values()) {
            if (course_constants.contains(courseProf_groundAtom._1_course) &&
                    person_constants.contains(courseProf_groundAtom._2_person)) {
                sampleDataSet.course_constants.add(courseProf_groundAtom._1_course);
                sampleDataSet.person_constants.add(courseProf_groundAtom._2_person);
                sampleDataSet.courseProf_groundAtoms.put(courseProf_groundAtom.getKey(), courseProf_groundAtom);
            }
        }

        // courseta
        for (CourseTA_GroundAtom courseTA_groundAtom : dataSet.courseTA_groundAtoms.values()) {
            if (course_constants.contains(courseTA_groundAtom._1_course) &&
                    person_constants.contains(courseTA_groundAtom._2_person)) {
                sampleDataSet.course_constants.add(courseTA_groundAtom._1_course);
                sampleDataSet.person_constants.add(courseTA_groundAtom._2_person);
                sampleDataSet.courseTA_groundAtoms.put(courseTA_groundAtom.getKey(), courseTA_groundAtom);
            }
        }

        // project
        for (Project_GroundAtom project_groundAtom : dataSet.project_groundAtoms.values()) {
            if (project_constants.contains(project_groundAtom._1_project) &&
                    person_constants.contains(project_groundAtom._2_person)) {
                sampleDataSet.project_constants.add(project_groundAtom._1_project);
                sampleDataSet.person_constants.add(project_groundAtom._2_person);
                sampleDataSet.project_groundAtoms.put(project_groundAtom.getKey(), project_groundAtom);
            }
        }

        log.writeln(" done!");

        log.writeln("");
        log.writeln("Sample Dataset created!");

        log.writeln("Person constants: " + sampleDataSet.person_constants.size() + "/" + dataSet.person_constants.size());
        log.writeln("Course constants: " + sampleDataSet.course_constants.size() + "/" + dataSet.course_constants.size());
        log.writeln("Project constants: " + sampleDataSet.project_constants.size() + "/" + dataSet.project_constants.size());

        log.writeln("Student ground atoms: " + sampleDataSet.student_groundAtoms.size() + "/" + dataSet.student_groundAtoms.size());
        log.writeln("Faculty ground atoms: " + sampleDataSet.faculty_groundAtoms.size() + "/" + dataSet.faculty_groundAtoms.size());
        log.writeln("CourseProf ground atoms: " + sampleDataSet.courseProf_groundAtoms.size() + "/" + dataSet.courseProf_groundAtoms.size());
        log.writeln("CourseTA ground atoms: " + sampleDataSet.courseTA_groundAtoms.size() + "/" + dataSet.courseTA_groundAtoms.size());
        log.writeln("Project ground atoms: " + sampleDataSet.project_groundAtoms.size() + "/" + dataSet.project_groundAtoms.size());

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
        facts.add(dataSet.student_groundAtoms);
        facts.add(dataSet.faculty_groundAtoms);
        facts.add(dataSet.courseProf_groundAtoms);
        facts.add(dataSet.courseTA_groundAtoms);
        facts.add(dataSet.project_groundAtoms);
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
            writer.write("graph WebKB {\n\n");

            // Person nodes
            log.writeln("Creating " + dataSet.person_constants.size() + " Person nodes");
            writer.write("\t// Person nodes\n");
            writer.write("\tsubgraph person {\n");
            writer.write("\t\tnode[style=filled label=\"\"]\n");
            for (String person : dataSet.person_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + person + "\"";
                if (dataSet.student_groundAtoms.containsKey(Student_GroundAtom.getKey(person))) {
                    attributes += " fillcolor=mediumseagreen";
                }
                if (dataSet.faculty_groundAtoms.containsKey(Faculty_GroundAtom.getKey(person))) {
                    attributes += " fillcolor=mediumslateblue";
                }
                writer.write("\t\t" + person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Course nodes
            log.writeln("Creating " + dataSet.course_constants.size() + " Course nodes");
            writer.write("\t// Course nodes\n");
            writer.write("\tsubgraph course {\n");
            writer.write("\t\tnode[shape=box style=filled label=\"\"]\n ");
            for (String course : dataSet.course_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + course + "\"";
                attributes += " fillcolor=lightcoral";
                writer.write("\t\t" + course);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Project nodes
            log.writeln("Creating " + dataSet.project_constants.size() + " Project nodes");
            writer.write("\t// Project nodes\n");
            writer.write("\tsubgraph project {\n");
            writer.write("\t\tnode[shape=diamond style=filled label=\"\"]\n ");
            for (String project : dataSet.project_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + project + "\"";
                attributes += " fillcolor=lightseagreen";
                writer.write("\t\t" + project);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // CourseProf edges
            log.writeln("Creating " + dataSet.courseProf_groundAtoms.size() + " CourseProf edges");
            writer.write("\t// CourseProf edges\n");
            for (CourseProf_GroundAtom courseProf_groundAtom : dataSet.courseProf_groundAtoms.values()) {
                String attributes = "";
                attributes += " penwidth=2";
                attributes += " color=mediumslateblue";
                writer.write("\t" + courseProf_groundAtom._1_course + "--" + courseProf_groundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // CourseTA edges
            log.writeln("Creating " + dataSet.courseTA_groundAtoms.size() + " CourseTA edges");
            writer.write("\t// CourseTA edges\n");
            for (CourseTA_GroundAtom courseTA_groundAtom : dataSet.courseTA_groundAtoms.values()) {
                String attributes = "";
                attributes += " penwidth=2";
                attributes += " color=mediumseagreen";
                writer.write("\t" + courseTA_groundAtom._1_course + "--" + courseTA_groundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // Project edges
            log.writeln("Creating " + dataSet.project_groundAtoms.size() + " Project edges");
            writer.write("\t// Project edges\n");
            for (Project_GroundAtom project_groundAtom : dataSet.project_groundAtoms.values()) {
                String attributes = "";
                attributes += " penwidth=2";
                attributes += " color=lightseagreen";
                writer.write("\t" + project_groundAtom._1_project + "--" + project_groundAtom._2_person);
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
            writer.write("graph WebKB {\n\n");

            // Person nodes
            log.writeln("Creating " + dataSet.person_constants.size() + " Person nodes");
            writer.write("\t// Person nodes\n");
            writer.write("\tsubgraph person {\n");
            writer.write("\t\tnode[style=filled label=\"\"]\n");
            for (String person : dataSet.person_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + person + "\"";
                if (sampleDataSet.person_constants.contains(person)) {
                    attributes += " penwidth=9";
                }
                if (dataSet.student_groundAtoms.containsKey(Student_GroundAtom.getKey(person))) {
                    attributes += " fillcolor=mediumseagreen";
                }
                if (dataSet.faculty_groundAtoms.containsKey(Faculty_GroundAtom.getKey(person))) {
                    attributes += " fillcolor=mediumslateblue";
                }
                writer.write("\t\t" + person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Course nodes
            log.writeln("Creating " + dataSet.course_constants.size() + " Course nodes");
            writer.write("\t// Course nodes\n");
            writer.write("\tsubgraph course {\n");
            writer.write("\t\tnode[shape=box style=filled label=\"\"]\n ");
            for (String course : dataSet.course_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + course + "\"";
                if (sampleDataSet.course_constants.contains(course)) {
                    attributes += " penwidth=9";
                }
                attributes += " fillcolor=lightcoral";
                writer.write("\t\t" + course);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Project nodes
            log.writeln("Creating " + dataSet.project_constants.size() + " Project nodes");
            writer.write("\t// Project nodes\n");
            writer.write("\tsubgraph project {\n");
            writer.write("\t\tnode[shape=diamond style=filled label=\"\"]\n ");
            for (String project : dataSet.project_constants) {
                String attributes = "";
                attributes += " tooltip=\"" + project + "\"";
                if (sampleDataSet.project_constants.contains(project)) {
                    attributes += " penwidth=9";
                }
                attributes += " fillcolor=lightseagreen";
                writer.write("\t\t" + project);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // CourseProf edges
            log.writeln("Creating " + dataSet.courseProf_groundAtoms.size() + " CourseProf edges");
            writer.write("\t// CourseProf edges\n");
            for (CourseProf_GroundAtom courseProf_groundAtom : dataSet.courseProf_groundAtoms.values()) {
                String attributes = "";
                if (sampleDataSet.courseProf_groundAtoms.containsKey(courseProf_groundAtom.getKey())) {
                    attributes += " penwidth=9";
                }
                attributes += " color=mediumslateblue";

                writer.write("\t" + courseProf_groundAtom._1_course + "--" + courseProf_groundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // CourseTA edges
            log.writeln("Creating " + dataSet.courseTA_groundAtoms.size() + " CourseTA edges");
            writer.write("\t// CourseTA edges\n");
            for (CourseTA_GroundAtom courseTA_groundAtom : dataSet.courseTA_groundAtoms.values()) {
                String attributes = "";
                if (sampleDataSet.courseTA_groundAtoms.containsKey(courseTA_groundAtom.getKey())) {
                    attributes += " penwidth=9";
                }
                attributes += " color=mediumseagreen";
                writer.write("\t" + courseTA_groundAtom._1_course + "--" + courseTA_groundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // Project edges
            log.writeln("Creating " + dataSet.project_groundAtoms.size() + " Project edges");
            writer.write("\t// Project edges\n");
            for (Project_GroundAtom project_groundAtom : dataSet.project_groundAtoms.values()) {
                String attributes = "";
                if (sampleDataSet.project_groundAtoms.containsKey(project_groundAtom.getKey())) {
                    attributes += " penwidth=9";
                }
                attributes += " color=lightseagreen";
                writer.write("\t" + project_groundAtom._1_project + "--" + project_groundAtom._2_person);
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
            options.addOption("sampleCourse", true, "double - movie sample ratio");
            options.addOption("sampleProject", true, "double - genre sample ratio");

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
            double sampleCourse = 1.0;
            if (cmd.hasOption("sampleCourse")) {
                sampleCourse = Double.parseDouble(cmd.getOptionValue("sampleCourse"));
            }
            double sampleProject = 1.0;
            if (cmd.hasOption("sampleProject")) {
                sampleProject = Double.parseDouble(cmd.getOptionValue("sampleProject"));
            }

            // Open logger
            log.open(userDir + folder + "/sample_log.txt");
            log.writeln("");
            log.writeln("WebKBSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + folder);

            // Create full dataset
            DataSet fullDataSet = createFullDataSet();
            writeDataSet(fullDataSet, "test");

            // Create sample dataset
            DataSet sampleDataSet = sampleDataSet(fullDataSet, samplePerson, sampleCourse, sampleProject);
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
