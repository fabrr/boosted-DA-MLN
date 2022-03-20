package edu.wisc.cs.will.Boosting.Scaling;

import edu.wisc.cs.will.Utils.condor.CondorFileReader;
import edu.wisc.cs.will.Utils.condor.CondorFileWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MyWebKB {

    private static String userDir = "";
    private static String folder = "";

    private static class LinkTo {
        public static String readName = "LinkTo";
        public static String predicateName = "linkTo";

        public String _1_page;
        public String _2_page;

        public static String getKey(String page_1, String page_2) {
            return predicateName + "(" + page_1 + "," + page_2 + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Has {
        public static String readName = "Has(";
        public static String predicateName = "has";

        public String _1_page;
        public String _2_word;

        public static String getKey(String page_1, String word_2) {
            return predicateName + "(" + page_1 + "," + word_2 + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_word);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Course {
        public static String readName = "Course";
        public static String predicateName = "course";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Department {
        public static String readName = "Department";
        public static String predicateName = "department";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Faculty {
        public static String readName = "Faculty";
        public static String predicateName = "faculty";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Person {
        public static String readName = "Person";
        public static String predicateName = "person";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class ResearchProject {
        public static String readName = "ResearchProject";
        public static String predicateName = "researchproject";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Staff {
        public static String readName = "Staff";
        public static String predicateName = "staff";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class Student {
        public static String readName = "Student";
        public static String predicateName = "student";

        public String _1_page;

        public static String getKey(String page_1) {
            return predicateName + "(" + page_1 + ").";
        }

        public String getKey() {
            return getKey(_1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class DataSet {
        public HashSet<String> pages = new HashSet<>();
        public HashSet<String> words = new HashSet<>();

        public HashMap<String, LinkTo> links = new HashMap<>();
        public HashMap<String, Has> has = new HashMap<>();

        public HashMap<String, Course> courses = new HashMap<>();
        public HashMap<String, Department> departments = new HashMap<>();
        public HashMap<String, Faculty> faculties = new HashMap<>();
        public HashMap<String, Person> persons = new HashMap<>();
        public HashMap<String, ResearchProject> researchProjects = new HashMap<>();
        public HashMap<String, Staff> staffs = new HashMap<>();
        public HashMap<String, Student> students = new HashMap<>();
    }

    private static DataSet sampleDataSet(DataSet dataSet, int pageCount) {
        DataSet sampleDataSet = new DataSet();

        ArrayList<String> pages = new ArrayList<>();
        ArrayList<String> pages_copy = new ArrayList<>();
        for (String page : dataSet.pages) {
            boolean category = false;
            boolean link = false;
            for (Course course : dataSet.courses.values()) {
                if (course._1_page.equals(page)) category = true;
            }
            for (Department department : dataSet.departments.values()) {
                if (department._1_page.equals(page)) category = true;
            }
            for (Faculty faculty : dataSet.faculties.values()) {
                if (faculty._1_page.equals(page)) category = true;
            }
            for (Person person : dataSet.persons.values()) {
                if (person._1_page.equals(page)) category = true;
            }
            for (ResearchProject researchProject : dataSet.researchProjects.values()) {
                if (researchProject._1_page.equals(page)) category = true;
            }
            for (Staff staff : dataSet.staffs.values()) {
                if (staff._1_page.equals(page)) category = true;
            }
            for (Student student : dataSet.students.values()) {
                if (student._1_page.equals(page)) category = true;
            }
            for (LinkTo linkTo : dataSet.links.values()) {
                if (linkTo._1_page.equals(page)) link = true;
                if (linkTo._2_page.equals(page)) link = true;
            }
            if (category && link) pages_copy.add(page);
        }

        for (int i = 0; i < pageCount && pages_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, pages_copy.size());
            String randomPage = pages_copy.remove(randomIndex);
            pages.add(randomPage);
        }

        for (LinkTo linkTo : dataSet.links.values()) {
            if (pages.contains(linkTo._1_page) && pages.contains(linkTo._2_page)) {
                sampleDataSet.links.put(linkTo.getKey(), linkTo);
                sampleDataSet.pages.add(linkTo._1_page);
                sampleDataSet.pages.add(linkTo._2_page);
            }
        }

        for (Has has : dataSet.has.values()) {
            if (sampleDataSet.pages.contains(has._1_page)) {
                sampleDataSet.has.put(has.getKey(), has);
                sampleDataSet.words.add(has._2_word);
                sampleDataSet.pages.add(has._1_page);
            }
        }

        for (Course course : dataSet.courses.values()) {
            if (sampleDataSet.pages.contains(course._1_page)) {
                sampleDataSet.courses.put(course.getKey(), course);
                sampleDataSet.pages.add(course._1_page);
            }
        }
        for (Department department : dataSet.departments.values()) {
            if (sampleDataSet.pages.contains(department._1_page)) {
                sampleDataSet.departments.put(department.getKey(), department);
                sampleDataSet.pages.add(department._1_page);
            }
        }
        for (Faculty faculty : dataSet.faculties.values()) {
            if (sampleDataSet.pages.contains(faculty._1_page)) {
                sampleDataSet.faculties.put(faculty.getKey(), faculty);
                sampleDataSet.pages.add(faculty._1_page);
            }
        }
        for (Person person : dataSet.persons.values()) {
            if (sampleDataSet.pages.contains(person._1_page)) {
                sampleDataSet.persons.put(person.getKey(), person);
                sampleDataSet.pages.add(person._1_page);
            }
        }
        for (ResearchProject researchProject : dataSet.researchProjects.values()) {
            if (sampleDataSet.pages.contains(researchProject._1_page)) {
                sampleDataSet.researchProjects.put(researchProject.getKey(), researchProject);
                sampleDataSet.pages.add(researchProject._1_page);
            }
        }
        for (Staff staff : dataSet.staffs.values()) {
            if (sampleDataSet.pages.contains(staff._1_page)) {
                sampleDataSet.staffs.put(staff.getKey(), staff);
                sampleDataSet.pages.add(staff._1_page);
            }
        }
        for (Student student : dataSet.students.values()) {
            if (sampleDataSet.pages.contains(student._1_page)) {
                sampleDataSet.students.put(student.getKey(), student);
                sampleDataSet.pages.add(student._1_page);
            }
        }

        System.out.println();
        System.out.println(sampleDataSet.pages.size() + "/" + dataSet.pages.size() + " pages");

        System.out.println(sampleDataSet.has.size() + "/" + dataSet.has.size() + " \"" + Has.predicateName + "\" atoms");
        System.out.println(sampleDataSet.links.size() + "/" + dataSet.links.size() + " \"" + LinkTo.predicateName + "\" atoms");

        System.out.println(sampleDataSet.courses.size() + "/" + dataSet.courses.size() + " \"" + Course.predicateName + "\" atoms");
        System.out.println(sampleDataSet.departments.size() + "/" + dataSet.departments.size() + " \"" + Department.predicateName + "\" atoms");
        System.out.println(sampleDataSet.faculties.size() + "/" + dataSet.faculties.size() + " \"" + Faculty.predicateName + "\" atoms");
        System.out.println(sampleDataSet.persons.size() + "/" + dataSet.persons.size() + " \"" + Person.predicateName + "\" atoms");
        System.out.println(sampleDataSet.researchProjects.size() + "/" + dataSet.researchProjects.size() + " \"" + ResearchProject.predicateName + "\" atoms");
        System.out.println(sampleDataSet.staffs.size() + "/" + dataSet.staffs.size() + " \"" + Staff.predicateName + "\" atoms");
        System.out.println(sampleDataSet.students.size() + "/" + dataSet.students.size() + " \"" + Student.predicateName + "\" atoms");

        return sampleDataSet;
    }

    private static DataSet readDB(DataSet dataSet, String file) {

        // open read stream
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new CondorFileReader(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // read
        String rawLine, line;
        try {
            String splitPattern = "\\(|\\,|\\)";
            String lettersPattern = "[^a-zA-Z]";
            while ((rawLine = reader.readLine()) != null) {
                line = rawLine.replaceAll("\\s+", "");

                // read "LinkTo"
                if (line.startsWith(LinkTo.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page1 = splitLine[2].replaceAll(lettersPattern, "");
                    String page2 = splitLine[3].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page1);
                    dataSet.pages.add(page2);
                    LinkTo linkTo2 = new LinkTo();
                    linkTo2._1_page = page2;
                    linkTo2._2_page = page1;
                    dataSet.links.put(linkTo2.getKey(), linkTo2);
                }

                // read "Has"
                if (line.startsWith(Has.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String word = splitLine[1].replaceAll(lettersPattern, "");
                    String page = splitLine[2].replaceAll(lettersPattern, "");
                    dataSet.words.add(word);
                    dataSet.pages.add(page);
                    Has has = new Has();
                    has._1_page = page;
                    has._2_word = word;
                    dataSet.has.put(has.getKey(), has);
                }

                // read "Course"
                if (line.startsWith(Course.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Course course = new Course();
                    course._1_page = page;
                    dataSet.courses.put(course.getKey(), course);
                }
                // read "Department"
                if (line.startsWith(Department.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Department department = new Department();
                    department._1_page = page;
                    dataSet.departments.put(department.getKey(), department);
                }
                // read "Faculty"
                if (line.startsWith(Faculty.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Faculty faculty = new Faculty();
                    faculty._1_page = page;
                    dataSet.faculties.put(faculty.getKey(), faculty);
                }
                // read "Person"
                if (line.startsWith(Person.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Person person = new Person();
                    person._1_page = page;
                    dataSet.persons.put(person.getKey(), person);
                }
                // read "ResearchProject"
                if (line.startsWith(ResearchProject.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    ResearchProject researchProject = new ResearchProject();
                    researchProject._1_page = page;
                    dataSet.researchProjects.put(researchProject.getKey(), researchProject);
                }
                // read "Staff"
                if (line.startsWith(Staff.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Staff staff = new Staff();
                    staff._1_page = page;
                    dataSet.staffs.put(staff.getKey(), staff);
                }
                // read "Student"
                if (line.startsWith(Student.readName)) {
                    String[] splitLine = line.split(splitPattern);
                    String page = splitLine[1].replaceAll(lettersPattern, "");
                    dataSet.pages.add(page);
                    Student student = new Student();
                    student._1_page = page;
                    dataSet.students.put(student.getKey(), student);
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

    public static void writeDB(DataSet dataSet, String _file) {
        // open file stream
        BufferedWriter writer = null;
        try {
            File file = new File(_file);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new CondorFileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write
        try {
            for (Has has : dataSet.has.values()) {
                writer.write(has.toString());
                writer.newLine();
            }
            for (LinkTo linkTo : dataSet.links.values()) {
                writer.write(linkTo.toString());
                writer.newLine();
            }
            for (Course course : dataSet.courses.values()) {
                writer.write(course.toString());
                writer.newLine();
            }
            for (Department department : dataSet.departments.values()) {
                writer.write(department.toString());
                writer.newLine();
            }
            for (Faculty faculty : dataSet.faculties.values()) {
                writer.write(faculty.toString());
                writer.newLine();
            }
            for (Person person : dataSet.persons.values()) {
                writer.write(person.toString());
                writer.newLine();
            }
            for (ResearchProject researchProject : dataSet.researchProjects.values()) {
                writer.write(researchProject.toString());
                writer.newLine();
            }
            for (Staff staff : dataSet.staffs.values()) {
                writer.write(staff.toString());
                writer.newLine();
            }
            for (Student student : dataSet.students.values()) {
                writer.write(student.toString());
                writer.newLine();
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
            writer.write("graph SmokeGen {\n\n");

            ArrayList<String> categories = new ArrayList<>();
            categories.add("course");
            categories.add("department");
            categories.add("faculty");
            categories.add("person");
            categories.add("researchproject");
            categories.add("staff");
            categories.add("student");

            writer.write("\t// Categories nodes\n");

            writer.write("\tsubgraph Categories {\n");
            writer.write("\t\tnode[style=filled]\n");
            for (String category : categories) {
                String attributes = "";
                if (category.equals("course")) attributes += " fillcolor=chocolate2";
                if (category.equals("department")) attributes += " fillcolor=deepskyblue3";
                if (category.equals("faculty")) attributes += " fillcolor=brown3";
                if (category.equals("person")) attributes += " fillcolor=darkgoldenrod";
                if (category.equals("researchproject")) attributes += " fillcolor=orchid3";
                if (category.equals("staff")) attributes += " fillcolor=dodgerblue4";
                if (category.equals("student")) attributes += " fillcolor=forestgreen";
                writer.write("\t\t" + category);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            writer.write("\t// Page nodes\n");

            writer.write("\tsubgraph Pages {\n");
            writer.write("\t\tnode[style=filled label=\"\"]\n");
            for (String page : dataSet.pages) {
                String attributes = "";
                attributes += " tooltip=\"" + page + "\"";
                writer.write("\t\t" + page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            writer.write("\t// LinkTo edges\n");
            for (LinkTo linkTo : dataSet.links.values()) {
                String attributes = "";
                writer.write("\t" + linkTo._1_page + "--" + linkTo._2_page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            writer.write("\t// Course edges\n");
            for (Course course : dataSet.courses.values()) {
                String attributes = "";
                attributes += " color=chocolate2";
                attributes += " penwidth=5";
                writer.write("\t" + course._1_page + "--" + Course.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// Department edges\n");
            for (Department department : dataSet.departments.values()) {
                String attributes = "";
                attributes += " color=deepskyblue3";
                attributes += " penwidth=5";
                writer.write("\t" + department._1_page + "--" + Department.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// Faculty edges\n");
            for (Faculty faculty : dataSet.faculties.values()) {
                String attributes = "";
                attributes += " color=brown3";
                attributes += " penwidth=5";
                writer.write("\t" + faculty._1_page + "--" + Faculty.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// Person edges\n");
            for (Person person : dataSet.persons.values()) {
                String attributes = "";
                attributes += " color=darkgoldenrod";
                attributes += " penwidth=5";
                writer.write("\t" + person._1_page + "--" + Person.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// ResearchProject edges\n");
            for (ResearchProject researchProject : dataSet.researchProjects.values()) {
                String attributes = "";
                attributes += " color=orchid3";
                attributes += " penwidth=5";
                writer.write("\t" + researchProject._1_page + "--" + ResearchProject.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// Staff edges\n");
            for (Staff staff : dataSet.staffs.values()) {
                String attributes = "";
                attributes += " color=dodgerblue4";
                attributes += " penwidth=5";
                writer.write("\t" + staff._1_page + "--" + Staff.predicateName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");
            writer.write("\t// Student edges\n");
            for (Student student : dataSet.students.values()) {
                String attributes = "";
                attributes += " color=forestgreen";
                attributes += " penwidth=5";
                writer.write("\t" + student._1_page + "--" + Student.predicateName);
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
        System.out.println();
        System.out.println("MyWebKB main");

        userDir = System.getProperty("user.dir");
        folder = "datasets/MyWebKB/";

        String linksFile = userDir + "/" + folder + "db/common.texas.db";
        String wordsFile = userDir + "/" + folder + "db/page-words.texas.db";
        String classesFile = userDir + "/" + folder + "db/page-classes.texas.db";

        String dbFile = userDir + "/" + folder + "_db.txt";

        System.out.println("Current directory: " + userDir);
        System.out.println("Folder: " + folder);

        DataSet dataSet = new DataSet();

        System.out.println();
        System.out.print("Reading files... ");
        dataSet = readDB(dataSet, linksFile);
        dataSet = readDB(dataSet, wordsFile);
        dataSet = readDB(dataSet, classesFile);
        System.out.println("done!");

        System.out.println();
        System.out.print("Sampling database... ");
        DataSet sampleDataSet = sampleDataSet(dataSet, 900); // 803
        System.out.println("done!");

        System.out.println();
        System.out.print("Writing database... ");
        writeDB(sampleDataSet, dbFile);
        System.out.println("done!");

        System.out.println();
        System.out.print("Writing graph... ");
        createGraph(sampleDataSet, "my");
        System.out.println("done!");



        System.out.println();
        System.out.println("Exiting main...");
    }
}
