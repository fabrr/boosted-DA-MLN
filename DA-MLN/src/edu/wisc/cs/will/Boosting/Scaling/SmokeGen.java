package edu.wisc.cs.will.Boosting.Scaling;

import edu.wisc.cs.will.Utils.condor.CondorFileWriter;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SmokeGen {

    private static String userDir = "";
    private static String folder = "";

    private static Log log = new Log();

    private static class Group {

        public int id;
        public boolean smoking;

        public HashMap<String, Person> persons = new HashMap<>();

        public static String getKey(int id) {
            return String.valueOf(id);
        }

        public String getKey() {
            return getKey(id);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static int personNextId = 0;

    private static class Person {

        public int id;
        public boolean smoking;
        public boolean cancer;

        public Group group;
        public HashMap<String, Person> friends = new HashMap<>();

        public static String getKey(int id) {
            return String.valueOf(id);
        }

        public String getKey() {
            return getKey(id);
        }

        @Override
        public String toString() {
            return "person_" + getKey();
        }
    }

    private static class Friends {

        public Person _1_person;
        public Person _2_person;

        public static String getKey(Person _1_person, Person _2_person) {
            return String.valueOf(_1_person.id) + "-" + String.valueOf(_2_person.id);
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
        HashMap<String, Group> groups = new HashMap<>();
        HashMap<String, Person> persons = new HashMap<>();
        HashMap<String, Friends> friends = new HashMap<>();
    }

    static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static DataSet generateDataSet() {
        DataSet dataSet = new DataSet();

        // group
        int groupsCount = 11;
        int groupSize = 11;

        // smoke
        double probGroupSmoke = 0.3;
        double probSmokeInSmokeGroup = 0.7;
        double probSmokeInNotSmokeGroup = 0.1;

        // cancer
        double probCancerForSmoker = 0.5;
        double probCancerForNotSmoker = 0.1;

        // friends
        double probFriendsSameGroup = 0.8;
        double probFriendsDiffGroup = 0.1;

        // GROUPS
        int smokesGroupCount = (int) Math.ceil(groupsCount * probGroupSmoke);
        for (int i = 0; i < groupsCount; i++) {
            Group group = new Group();
            group.id = i;
            // prob of smoking group
            if (i < smokesGroupCount) {
                group.smoking = true;
            } else {
                group.smoking = false;
            }
            dataSet.groups.put(group.getKey(), group);
        }

        int x = 10;
        double y = 0.3;
        int f = (int) Math.ceil(x * y);

        // PERSONS
        for (Group group : dataSet.groups.values()) {
            for (int i = 0; i < groupSize; i++) {
                Person person = new Person();
                person.id = personNextId++;
                person.group = group;
                if (group.smoking) {
                    // prob of person smoking in smoking group
                    if (probSmokeInSmokeGroup > Math.random()) {
                        person.smoking = true;
                    } else {
                        person.smoking = false;
                    }
                } else {
                    // prob of person smoking in non smoking group
                    if (probSmokeInNotSmokeGroup > Math.random()) {
                        person.smoking = true;
                    } else {
                        person.smoking = false;
                    }
                }
                dataSet.persons.put(person.getKey(), person);
                group.persons.put(person.getKey(), person);
            }
        }
        for (Person person : dataSet.persons.values()) {
            if (person.smoking) {
                if (probCancerForSmoker > Math.random()) {
                    person.cancer = true;
                } else {
                    person.cancer = false;
                }
            } else {
                if (probCancerForNotSmoker > Math.random()) {
                    person.cancer = true;
                } else {
                    person.cancer = false;
                }
            }
        }

        // FRIENDS
        for (Person person : dataSet.persons.values()) {
            for (int id_2 = person.id + 1; id_2 < dataSet.persons.size(); id_2++) {
                Person friend = dataSet.persons.get(Person.getKey(id_2));
                if (friend == null) continue;
                if (person.group == friend.group) {
                    if (probFriendsSameGroup > Math.random()) {
                        Friends friends = new Friends();
                        friends._1_person = person;
                        friends._2_person = friend;
                        dataSet.friends.put(friends.getKey(), friends);
                    }
                } else {
                    if (probFriendsDiffGroup > Math.random()) {
                        Friends friends = new Friends();
                        friends._1_person = person;
                        friends._2_person = friend;
                        dataSet.friends.put(friends.getKey(), friends);
                    }
                }
            }


//            int friendsCount = friendsMean + getRandomInt(-friendsRange, friendsRange);
//            ArrayList<Person> friendsPoolGroup = new ArrayList<>();
//            ArrayList<Person> friendsPoolNotGroup = new ArrayList<>();
//
//            for (Group group : dataSet.groups.values()) {
//                for (Person person_pool : group.persons.values()) {
//                    if (person_pool == person) continue;
//                    if (group == person.group) {
//                        friendsPoolGroup.add(person_pool);
//                    } else {
//                        friendsPoolNotGroup.add(person_pool);
//                    }
//                }
//            }
//
//            for (int i = 0; i < friendsCount; i++) {
//                if (getRandomInt(1, 10) <= probFriendsSameGroup && friendsPoolGroup.size() != 0) {
//                    Person randomFriend = friendsPoolGroup.remove(getRandomInt(0, friendsPoolGroup.size() - 1));
//                    person.friends.put(randomFriend.getKey(), randomFriend);
//                } else {
//                    if (friendsPoolNotGroup.size() == 0) continue;
//                    ;
//                    Person randomFriend = friendsPoolNotGroup.remove(getRandomInt(0, friendsPoolNotGroup.size() - 1));
//                    person.friends.put(randomFriend.getKey(), randomFriend);
//                }
//            }

        }

        return dataSet;
    }

    public static void writeDataSet(DataSet dataSet) {
        try {
            log.writeln("");
            log.writeln("Write db.txt");

            String dbFile = userDir + folder + "/db.txt";

            // open file stream
            BufferedWriter writer = null;

            File file = new File(dbFile);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new CondorFileWriter(file));

            // write persons
            writer.newLine();
            for (Person person : dataSet.persons.values()) {
                if (person.smoking) {
                    writer.write("smokes(" + person.toString() + ").");
                    writer.newLine();
                    writer.write("r_smokes(" + person.toString() + ").");
                } else {
                    writer.write("r_not_smokes(" + person.toString() + ").");
                }
                writer.newLine();
//                if (person.cancer) {
//                    writer.write("cancer(" + person.toString() + ").");
//                } else {
//                    writer.write("not_cancer(" + person.toString() + ").");
//                }
//                writer.newLine();
            }

//            for (Person person : dataSet.persons.values()) {
//                for (Person friend : person.friends.values()) {
//                    writer.write("friends(" + person.toString() + ", " + friend.toString() + ").");
//                    writer.write("friends(" + friend.toString() + ", " + person.toString() + ").");
//                    writer.newLine();
//                }
//            }

            // write friends
            writer.newLine();
            for (Friends friends : dataSet.friends.values()) {
                writer.write("friends(" + friends._1_person.toString() + ", " + friends._2_person.toString() + ").");
                writer.newLine();
                writer.write("friends(" + friends._2_person.toString() + ", " + friends._1_person.toString() + ").");
                writer.newLine();
            }

            // close file stream
            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

            // Groups
            log.writeln("Creating " + dataSet.groups.size() + " groups and " + dataSet.persons.size() + " Person nodes");
            int smoke_groups = 0;
            for (Group group : dataSet.groups.values()){
                if (group.smoking) smoke_groups++;
            }
            log.writeln(smoke_groups + "/" + dataSet.groups.size() + " groups are labeled smoking.");
            writer.write("\t// Person nodes\n");

            for (Group group : dataSet.groups.values()) {
                writer.write("\tsubgraph cluster_" + group.id + " {\n");
                writer.write("\t\tlabel = \"Group_" + group.id + "\"\n");
                writer.write("\t\tstyle=filled\n");
                writer.write("\t\tcolor=" + (group.smoking ? "darksalmon" : "darkseagreen1") + "\n");
                writer.write("\t\tnode[style=filled]\n");
                for (Person person : group.persons.values()) {
                    String attributes = "";
                    if (person.smoking) {
                        attributes += " fillcolor=crimson";
                    } else {
                        attributes += " fillcolor=aquamarine3";
                    }
//                    if (person.cancer) {
//                        attributes += " penwidth=3";
//                        attributes += " color=gold";
//                    }
                    writer.write("\t\t" + person.id);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Friends edges
            log.writeln("Creating " + dataSet.friends.size() + " Friends edges");
            writer.write("\t// Friends edges\n");
            for (Friends friends : dataSet.friends.values()) {
                String attributes = "";
                writer.write("\t" + friends._1_person.id + "--" + friends._2_person.id);
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

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            folder = "/" + cmd.getOptionValue("dataset");

            // Open logger
            log.open(userDir + folder + "/gen_log.txt");
            log.writeln("");
            log.writeln("SmokeGen main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + folder);

            DataSet dataSet = generateDataSet();

            writeDataSet(dataSet);

            createGraph(dataSet, "gen");

            // Exit
            log.writeln("");
            log.writeln("Exiting main...");
            log.close();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
