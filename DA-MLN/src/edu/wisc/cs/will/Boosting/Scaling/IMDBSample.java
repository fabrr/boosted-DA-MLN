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

public class IMDBSample {

    private static String userDir = "";
    private static String folder = "";

    private static Log log = new Log();

    private static class FemaleGroundAtom {
        public static String predicateName = "female";

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

    private static class R_Female_GroundAtom {
        public static String predicateName = "r_female";

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

    private static class R_Male_GroundAtom {
            public static String predicateName = "r_male";

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

    private static class ActorGroundAtom {
        public static String predicateName = "actor";

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

    private static class DirectorGroundAtom {
        public static String predicateName = "director";

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

    private static class GenreGroundAtom {
        public static String predicateName = "genre";

        public String _1_director;
        public String _2_genre;

        public static String getKey(String director, String genre) {
            return predicateName + "(" + director + "," + genre + ").";
        }

        public String getKey() {
            return getKey(_1_director, _2_genre);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class MovieGroundAtom {
        public static String predicateName = "movie";

        public String _1_movie;
        public String _2_person;

        public static String getKey(String movie, String person) {
            return predicateName + "(" + movie + "," + person + ").";
        }

        public String getKey() {
            return getKey(_1_movie, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class WorkedUnderGroundAtom {
        public static String predicateName = "workedunder";

        public String _1_actor;
        public String _2_director;

        public static String getKey(String actor, String director) {
            return predicateName + "(" + actor + "," + director + ").";
        }

        public String getKey() {
            return getKey(_1_actor, _2_director);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class DataSet {

        public HashSet<String> personConstants = new HashSet<>();
        public HashSet<String> movieConstants = new HashSet<>();
        public HashSet<String> genreConstants = new HashSet<>();

        public HashMap<String, FemaleGroundAtom> femaleGroundAtoms = new HashMap<>();
        public HashMap<String, ActorGroundAtom> actorGroundAtoms = new HashMap<>();
        public HashMap<String, DirectorGroundAtom> directorGroundAtoms = new HashMap<>();
        public HashMap<String, GenreGroundAtom> genreGroundAtoms = new HashMap<>();
        public HashMap<String, MovieGroundAtom> movieGroundAtoms = new HashMap<>();
        public HashMap<String, WorkedUnderGroundAtom> workedUnderGroundAtoms = new HashMap<>();

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

                // read "gender"
                if (line.startsWith("gender")) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String personName = splitLine[1];
                    String gender = splitLine[2];
                    if (gender.equals("female")) {
                        // add person constant
                        dataSet.personConstants.add(personName);
                        // create actor ground atom
                        FemaleGroundAtom femaleGroundAtom = new FemaleGroundAtom();
                        femaleGroundAtom._1_person = personName;
                        // add person to ground atom
                        dataSet.femaleGroundAtoms.put(femaleGroundAtom.getKey(), femaleGroundAtom);
                    }
                }

                // read "actor"
                if (line.startsWith(ActorGroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String actorName = splitLine[1];
                    // add person constant
                    dataSet.personConstants.add(actorName);
                    // create actor ground atom
                    ActorGroundAtom actorGroundAtom = new ActorGroundAtom();
                    actorGroundAtom._1_person = actorName;
                    // add actor ground atom
                    dataSet.actorGroundAtoms.put(actorGroundAtom.getKey(), actorGroundAtom);
                }

                // read "director"
                if (line.startsWith(DirectorGroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String directorName = splitLine[1];
                    // add person constant
                    dataSet.personConstants.add(directorName);
                    // create director ground atom
                    DirectorGroundAtom directorGroundAtom = new DirectorGroundAtom();
                    directorGroundAtom._1_person = directorName;
                    // add director ground atom
                    dataSet.directorGroundAtoms.put(directorGroundAtom.getKey(), directorGroundAtom);
                }

                // read "genre"
                if (line.startsWith(GenreGroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String directorName = splitLine[1];
                    String genreName = splitLine[2];
                    // add person constant
                    dataSet.personConstants.add(directorName);
                    // add genre constant
                    dataSet.genreConstants.add(genreName);
                    // create genre ground atom
                    GenreGroundAtom genreGroundAtom = new GenreGroundAtom();
                    genreGroundAtom._1_director = directorName;
                    genreGroundAtom._2_genre = genreName;
                    // add genre ground atom
                    dataSet.genreGroundAtoms.put(genreGroundAtom.getKey(), genreGroundAtom);
                }

                // read "movie"
                if (line.startsWith(MovieGroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String movieName = splitLine[1];
                    String personName = splitLine[2];
                    // add movie constant
                    dataSet.movieConstants.add(movieName);
                    // add person constant
                    dataSet.personConstants.add(personName);
                    // create movie ground atom
                    MovieGroundAtom movieGroundAtom = new MovieGroundAtom();
                    movieGroundAtom._1_movie = movieName;
                    movieGroundAtom._2_person = personName;
                    // add movie ground atom
                    dataSet.movieGroundAtoms.put(movieGroundAtom.getKey(), movieGroundAtom);
                }

                // read "workunder"
                if (line.startsWith(WorkedUnderGroundAtom.predicateName)) {
                    String[] splitLine = line.split(splitPattern);
                    // parse arguments
                    String actorName = splitLine[1];
                    String directorName = splitLine[2];
                    // add actor constant
                    dataSet.personConstants.add(actorName);
                    // add director constant
                    dataSet.personConstants.add(directorName);
                    // create workedunder ground atom
                    WorkedUnderGroundAtom workedUnderGroundAtom = new WorkedUnderGroundAtom();
                    workedUnderGroundAtom._1_actor = actorName;
                    workedUnderGroundAtom._2_director = directorName;
                    // add workedunder ground atom
                    dataSet.workedUnderGroundAtoms.put(workedUnderGroundAtom.getKey(), workedUnderGroundAtom);
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

    private static DataSet createFullDataSet() {

        log.writeln("");
        log.writeln("Create Full Dataset");

        // READ DATASET
        log.writeln("");
        log.writeln("Reading Dataset...");

        // read db file and create dataset
        String dbFile = userDir + folder + "/db.txt";
        DataSet dataSet = readDataSet(dbFile);

        log.writeln("Dataset loaded!");
        log.writeln("Person constants: " + dataSet.personConstants.size());
        log.writeln("Movie constants: " + dataSet.movieConstants.size());
        log.writeln("Genre constants: " + dataSet.genreConstants.size());
        log.writeln("Female ground atoms: " + dataSet.femaleGroundAtoms.size());
        log.writeln("Director ground atoms: " + dataSet.directorGroundAtoms.size());
        log.writeln("Actor ground atoms: " + dataSet.actorGroundAtoms.size());
        log.writeln("Genre ground atoms: " + dataSet.genreGroundAtoms.size());
        log.writeln("Movie ground atoms: " + dataSet.movieGroundAtoms.size());
        log.writeln("WorkedUnder ground atoms: " + dataSet.workedUnderGroundAtoms.size());

        return dataSet;
    }

    private static DataSet sampleDataSet(DataSet dataSet, double samplePerson, double sampleMovie, double sampleGenre) {
        log.writeln("");
        log.writeln("Sample Dataset");
        log.writeln("Sample person: " + samplePerson);
        log.writeln("Sample movie: " + sampleMovie);
        log.writeln("Sample genre: " + sampleGenre);

        DataSet sampleDataSet = new DataSet();

        // SAMPLE CONSTANTS

        // sample persons constants
        int personSampleSize = (int) Math.ceil(samplePerson * (dataSet.personConstants.size()));
        ArrayList<String> persons_copy = new ArrayList<>();
        for (String person : dataSet.personConstants) persons_copy.add(person);
        for (int i = 0; i < personSampleSize && persons_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, persons_copy.size());
            String randomPerson = persons_copy.remove(randomIndex);
            sampleDataSet.personConstants.add(randomPerson);
        }

        // sample movies constants
        int movieSampleSize = (int) Math.ceil(sampleMovie * (dataSet.movieConstants.size()));
        ArrayList<String> movie_copy = new ArrayList<>();
        for (String movie : dataSet.movieConstants) movie_copy.add(movie);
        for (int i = 0; i < movieSampleSize && movie_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, movie_copy.size());
            String randomMovie = movie_copy.remove(randomIndex);
            sampleDataSet.movieConstants.add(randomMovie);
        }

        // sample genre constants
        int genreSampleSize = (int) Math.ceil(sampleGenre * (dataSet.genreConstants.size()));
        ArrayList<String> genres_copy = new ArrayList<>();
        for (String genre : dataSet.genreConstants) genres_copy.add(genre);
        for (int i = 0; i < genreSampleSize && genres_copy.size() != 0; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, genres_copy.size());
            String randomGenre = genres_copy.remove(randomIndex);
            sampleDataSet.genreConstants.add(randomGenre);
        }

        log.writeln("");
        log.writeln("Sampled person constants: " + sampleDataSet.personConstants.size() + "/" + dataSet.personConstants.size());
        log.writeln("Sampled movie constants: " + sampleDataSet.movieConstants.size() + "/" + dataSet.movieConstants.size());
        log.writeln("Sampled genre constants: " + sampleDataSet.genreConstants.size() + "/" + dataSet.genreConstants.size());

        log.writeln("");
        log.writeln("Adding relations...");

        // ADD GROUND ATOMS

        // female
        for (FemaleGroundAtom femaleGroundAtom : dataSet.femaleGroundAtoms.values()) {
            if (sampleDataSet.personConstants.contains(femaleGroundAtom._1_person)) {
                sampleDataSet.femaleGroundAtoms.put(femaleGroundAtom.getKey(), femaleGroundAtom);
            }
        }

        // director
        for (DirectorGroundAtom directorGroundAtom : dataSet.directorGroundAtoms.values()) {
            if (sampleDataSet.personConstants.contains(directorGroundAtom._1_person)) {
                sampleDataSet.directorGroundAtoms.put(directorGroundAtom.getKey(), directorGroundAtom);
            }
        }

        // actor
        for (ActorGroundAtom actorGroundAtom : dataSet.actorGroundAtoms.values()) {
            if (sampleDataSet.personConstants.contains(actorGroundAtom._1_person)) {
                sampleDataSet.actorGroundAtoms.put(actorGroundAtom.getKey(), actorGroundAtom);
            }
        }

        // genre
        for (GenreGroundAtom genreGroundAtom : dataSet.genreGroundAtoms.values()) {
            if (sampleDataSet.personConstants.contains(genreGroundAtom._1_director) &&
                    sampleDataSet.genreConstants.contains(genreGroundAtom._2_genre)) {
                sampleDataSet.genreGroundAtoms.put(genreGroundAtom.getKey(), genreGroundAtom);
            }
        }

        // movie
        for (MovieGroundAtom movieGroundAtom : dataSet.movieGroundAtoms.values()) {
            if (sampleDataSet.movieConstants.contains(movieGroundAtom._1_movie) &&
                    sampleDataSet.personConstants.contains(movieGroundAtom._2_person)) {
                sampleDataSet.movieGroundAtoms.put(movieGroundAtom.getKey(), movieGroundAtom);
            }
        }

        // workedunder
        for (WorkedUnderGroundAtom workedUnderGroundAtom : dataSet.workedUnderGroundAtoms.values()) {
            if (sampleDataSet.personConstants.contains(workedUnderGroundAtom._1_actor) &&
                    sampleDataSet.personConstants.contains(workedUnderGroundAtom._2_director)) {
                sampleDataSet.workedUnderGroundAtoms.put(workedUnderGroundAtom.getKey(), workedUnderGroundAtom);
            }
        }

        log.writeln("Sample Dataset created!");
        log.writeln("Person constants: " + sampleDataSet.personConstants.size());
        log.writeln("Movie constants: " + sampleDataSet.movieConstants.size());
        log.writeln("Genre constants: " + sampleDataSet.genreConstants.size());
        log.writeln("Female ground atoms: " + sampleDataSet.femaleGroundAtoms.size());
        log.writeln("Director ground atoms: " + sampleDataSet.directorGroundAtoms.size());
        log.writeln("Actor ground atoms: " + sampleDataSet.actorGroundAtoms.size());
        log.writeln("Genre ground atoms: " + sampleDataSet.genreGroundAtoms.size());
        log.writeln("Movie ground atoms: " + sampleDataSet.movieGroundAtoms.size());
        log.writeln("WorkedUnder ground atoms: " + sampleDataSet.workedUnderGroundAtoms.size());

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
        facts.add(dataSet.femaleGroundAtoms);
        facts.add(dataSet.directorGroundAtoms);
        facts.add(dataSet.actorGroundAtoms);
        facts.add(dataSet.genreGroundAtoms);
        facts.add(dataSet.movieGroundAtoms);
        facts.add(dataSet.workedUnderGroundAtoms);
        HashMap<String, R_Female_GroundAtom> r_female_groundAtoms = new HashMap<>();
        HashMap<String, R_Male_GroundAtom> r_male_groundAtoms = new HashMap<>();
        for (String person : dataSet.personConstants){
            if (dataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(person))) {
                R_Female_GroundAtom r_female_groundAtom = new R_Female_GroundAtom();
                r_female_groundAtom._1_person = person;
                r_female_groundAtoms.put(r_female_groundAtom.getKey(),r_female_groundAtom);
            } else {
                R_Male_GroundAtom r_male_groundAtom = new R_Male_GroundAtom();
                r_male_groundAtom._1_person = person;
                r_male_groundAtoms.put(r_male_groundAtom.getKey(),r_male_groundAtom);
            }
        }
        facts.add(r_female_groundAtoms);
        facts.add(r_male_groundAtoms);
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

    public static void createFullGraph(DataSet dataSet) {

        Boolean Actors = true;
        Boolean Genres = false;

        log.writeln("");
        log.writeln("Create Full Graph");
        log.writeln("Actors = " + Actors);
        log.writeln("Genres = " + Genres);

        // open file stream
        String graphFile = userDir + folder + "/graphs/full_graph.dot";
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
            writer.write("graph IMDB {\n\n");

            // Director nodes
            log.writeln("Creating " + dataSet.directorGroundAtoms.size() + " Director nodes");
            writer.write("\t// Director nodes\n");
            writer.write("\tsubgraph directors {\n");
            writer.write("\t\tnode[shape=diamond,style=filled]\n");
            for (DirectorGroundAtom directorGroundAtom : dataSet.directorGroundAtoms.values()) {
                String attributes = "";
                String directorName = directorGroundAtom._1_person;
                if (dataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(directorName))) {
                    attributes += "fillcolor=pink";
                } else {
                    attributes += "fillcolor=lightskyblue";
                }
                writer.write("\t\t" + directorName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Actor nodes
            if (Actors) {
                log.writeln("Creating " + dataSet.actorGroundAtoms.size() + " Actor nodes");
                writer.write("\t// Actor nodes\n");
                writer.write("\tsubgraph actors {\n");
                writer.write("\t\tnode[style=filled]\n");
                for (ActorGroundAtom actorGroundAtom : dataSet.actorGroundAtoms.values()) {
                    String attributes = "";
                    String actorName = actorGroundAtom._1_person;
                    if (dataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(actorName))) {
                        attributes += "fillcolor=pink";
                    } else {
                        attributes += "fillcolor=lightskyblue";
                    }
                    writer.write("\t\t" + actorName);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Movie nodes
            log.writeln("Creating " + dataSet.movieConstants.size() + " Movie nodes");
            writer.write("\t// Movie nodes\n");
            writer.write("\tsubgraph movies {\n");
            writer.write("\t\tnode[shape=box,style=filled,fillcolor=coral]\n");
            for (String movie : dataSet.movieConstants) {
                String attributes = "";
                writer.write("\t\t" + movie);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Genre nodes
            if (Genres) {
                log.writeln("Creating " + dataSet.genreConstants.size() + " Genre nodes");
                writer.write("\t// Genre nodes\n");
                writer.write("\tsubgraph genres {\n");
                writer.write("\t\tnode[shape=parallelogram,style=filled,fillcolor=limegreen]\n");
                for (String genre : dataSet.genreConstants) {
                    String attributes = "";
                    writer.write("\t\t" + genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // movie ground atom edges
            log.writeln("Creating " + dataSet.movieGroundAtoms.size() + " movie edges");
            writer.write("\t// Movie edges\n");
            for (MovieGroundAtom movieGroundAtom : dataSet.movieGroundAtoms.values()) {
                if (!Actors) {
                    String actorKey = ActorGroundAtom.getKey(movieGroundAtom._2_person);
                    ActorGroundAtom actorGroundAtom = dataSet.actorGroundAtoms.get(actorKey);
                    if (actorGroundAtom != null) continue;
                    ;
                }
                writer.write("\t" + movieGroundAtom._1_movie + "--" + movieGroundAtom._2_person + "\n");
            }
            writer.write("\n");

            // genre ground atom edges
            if (Genres) {
                log.writeln("Creating " + dataSet.genreGroundAtoms.size() + " genre edges");
                writer.write("\t// Genre edges\n");
                for (GenreGroundAtom genreGroundAtom : dataSet.genreGroundAtoms.values()) {
                    writer.write("\t" + genreGroundAtom._1_director + "--" + genreGroundAtom._2_genre + "\n");
                }
                writer.write("\n");
            }

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.write("Writing 'graphs/full_graph.dot' file...");

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

    public static void createSampleGraph(DataSet dataSet, DataSet sampleDataSet) {

        Boolean Actors = true;
        Boolean Genres = true;

        log.writeln("");
        log.writeln("Create Sample Graph");
        log.writeln("Actors = " + Actors);
        log.writeln("Genres = " + Genres);

        // open file stream
        String graphFile = userDir + folder + "/graphs/sample_graph.dot";
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
            writer.write("graph IMDB {\n\n");

            // Director nodes
            log.writeln("Creating " + dataSet.directorGroundAtoms.size() + " Director nodes");
            writer.write("\t// Director nodes\n");
            writer.write("\tsubgraph directors {\n");
            writer.write("\t\tnode[shape=diamond style=filled]\n");
            for (DirectorGroundAtom directorGroundAtom : dataSet.directorGroundAtoms.values()) {
                String attributes = "";
                String directorName = directorGroundAtom._1_person;
                if (sampleDataSet.directorGroundAtoms.containsKey(directorGroundAtom.getKey())) {
                    attributes += " penwidth=5";
                    if (dataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(directorName))) {
                        attributes += " fillcolor=pink";
                    } else {
                        attributes += " fillcolor=lightskyblue";
                    }
                }
                writer.write("\t\t" + directorName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Actor nodes
            if (Actors) {
                log.writeln("Creating " + dataSet.actorGroundAtoms.size() + " Actor nodes");
                writer.write("\t// Actor nodes\n");
                writer.write("\tsubgraph actors {\n");
                writer.write("\t\tnode[style=filled]\n");
                for (ActorGroundAtom actorGroundAtom : dataSet.actorGroundAtoms.values()) {
                    String attributes = "";
                    String actorName = actorGroundAtom._1_person;
                    if (sampleDataSet.actorGroundAtoms.containsKey(actorGroundAtom.getKey())) {
                        attributes += " penwidth=5";
                        if (dataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(actorName))) {
                            attributes += " fillcolor=pink";
                        } else {
                            attributes += " fillcolor=lightskyblue";
                        }
                    }
                    writer.write("\t\t" + actorName);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Movie nodes
            log.writeln("Creating " + dataSet.movieConstants.size() + " Movie nodes");
            writer.write("\t// Movie nodes\n");
            writer.write("\tsubgraph movies {\n");
            writer.write("\t\tnode[shape=box style=filled]\n");
            for (String movie : dataSet.movieConstants) {
                String attributes = "";
                if (sampleDataSet.movieConstants.contains(movie)) {
                    attributes += " fillcolor=coral";
                    attributes += " penwidth=5";
                }
                writer.write("\t\t" + movie);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Genre nodes
            if (Genres) {
                log.writeln("Creating " + dataSet.genreConstants.size() + " Genre nodes");
                writer.write("\t// Genre nodes\n");
                writer.write("\tsubgraph genres {\n");
                writer.write("\t\tnode[shape=parallelogram,style=filled]\n");
                for (String genre : dataSet.genreConstants) {
                    String attributes = "";
                    if (sampleDataSet.genreConstants.contains(genre)) {
                        attributes += " fillcolor=limegreen";
                        attributes += " penwidth=5";
                    }
                    writer.write("\t\t" + genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // movie ground atom edges
            log.writeln("Creating " + dataSet.movieGroundAtoms.size() + " movie edges");
            writer.write("\t// Movie edges\n");
            for (MovieGroundAtom movieGroundAtom : dataSet.movieGroundAtoms.values()) {
                String attributes = "";
                if (!Actors) {
                    String actorKey = ActorGroundAtom.getKey(movieGroundAtom._2_person);
                    ActorGroundAtom actorGroundAtom = dataSet.actorGroundAtoms.get(actorKey);
                    if (actorGroundAtom != null) continue;
                    ;
                }
                if (sampleDataSet.movieGroundAtoms.containsKey(movieGroundAtom.getKey())) {
                    attributes += " penwidth=5";
                }
                writer.write("\t" + movieGroundAtom._1_movie + "--" + movieGroundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // genre ground atom edges
            if (Genres) {
                log.writeln("Creating " + dataSet.genreGroundAtoms.size() + " genre edges");
                writer.write("\t// Genre edges\n");
                for (GenreGroundAtom genreGroundAtom : dataSet.genreGroundAtoms.values()) {
                    String attributes = "";
                    if (sampleDataSet.genreGroundAtoms.containsKey(genreGroundAtom.getKey())) {
                        attributes += " penwidth=5";
                    }
                    writer.write("\t" + genreGroundAtom._1_director + "--" + genreGroundAtom._2_genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\n");
            }

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.write("Writing 'graphs/sample_graph.dot' file...");

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

    public static void createSampleOnlyGraph(DataSet sampleDataSet) {

        Boolean Actors = true;
        Boolean Genres = true;

        log.writeln("");
        log.writeln("Create Sample Only Graph");
        log.writeln("Actors = " + Actors);
        log.writeln("Genres = " + Genres);

        // open file stream
        String graphFile = userDir + folder + "/graphs/sample_only_graph.dot";
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
            writer.write("graph IMDB {\n\n");

            // Director nodes
            log.writeln("Creating " + sampleDataSet.directorGroundAtoms.size() + " Director nodes");
            writer.write("\t// Director nodes\n");
            writer.write("\tsubgraph directors {\n");
            writer.write("\t\tnode[shape=diamond style=filled]\n");
            for (DirectorGroundAtom directorGroundAtom : sampleDataSet.directorGroundAtoms.values()) {
                String attributes = "";
                String directorName = directorGroundAtom._1_person;
                if (sampleDataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(directorName))) {
                    attributes += " fillcolor=pink";
                } else {
                    attributes += " fillcolor=lightskyblue";
                }
                writer.write("\t\t" + directorName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Actor nodes
            if (Actors) {
                log.writeln("Creating " + sampleDataSet.actorGroundAtoms.size() + " Actor nodes");
                writer.write("\t// Actor nodes\n");
                writer.write("\tsubgraph actors {\n");
                writer.write("\t\tnode[style=filled]\n");
                for (ActorGroundAtom actorGroundAtom : sampleDataSet.actorGroundAtoms.values()) {
                    String attributes = "";
                    String actorName = actorGroundAtom._1_person;
                    if (sampleDataSet.femaleGroundAtoms.containsKey(FemaleGroundAtom.getKey(actorName))) {
                        attributes += " fillcolor=pink";
                    } else {
                        attributes += " fillcolor=lightskyblue";
                    }
                    writer.write("\t\t" + actorName);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Movie nodes
            log.writeln("Creating " + sampleDataSet.movieConstants.size() + " Movie nodes");
            writer.write("\t// Movie nodes\n");
            writer.write("\tsubgraph movies {\n");
            writer.write("\t\tnode[shape=box style=filled]\n");
            for (String movie : sampleDataSet.movieConstants) {
                String attributes = "";
                attributes += " fillcolor=coral";
                writer.write("\t\t" + movie);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Genre nodes
            if (Genres) {
                log.writeln("Creating " + sampleDataSet.genreConstants.size() + " Genre nodes");
                writer.write("\t// Genre nodes\n");
                writer.write("\tsubgraph genres {\n");
                writer.write("\t\tnode[shape=parallelogram,style=filled]\n");
                for (String genre : sampleDataSet.genreConstants) {
                    String attributes = "";
                    attributes += " fillcolor=limegreen";
                    writer.write("\t\t" + genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // movie ground atom edges
            log.writeln("Creating " + sampleDataSet.movieGroundAtoms.size() + " movie edges");
            writer.write("\t// Movie edges\n");
            for (MovieGroundAtom movieGroundAtom : sampleDataSet.movieGroundAtoms.values()) {
                String attributes = "";
                if (!Actors) {
                    String actorKey = ActorGroundAtom.getKey(movieGroundAtom._2_person);
                    ActorGroundAtom actorGroundAtom = sampleDataSet.actorGroundAtoms.get(actorKey);
                    if (actorGroundAtom != null) continue;
                }
                writer.write("\t" + movieGroundAtom._1_movie + "--" + movieGroundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // genre ground atom edges
            if (Genres) {
                log.writeln("Creating " + sampleDataSet.genreGroundAtoms.size() + " genre edges");
                writer.write("\t// Genre edges\n");
                for (GenreGroundAtom genreGroundAtom : sampleDataSet.genreGroundAtoms.values()) {
                    String attributes = "";
                    writer.write("\t" + genreGroundAtom._1_director + "--" + genreGroundAtom._2_genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\n");
            }

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        log.write("Writing 'graphs/sample_only_graph.dot' file...");

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
            options.addOption("sampleMovie", true, "double - movie sample ratio");
            options.addOption("sampleGenre", true, "double - genre sample ratio");

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
            double sampleMovie = 1.0;
            if (cmd.hasOption("sampleMovie")) {
                sampleMovie = Double.parseDouble(cmd.getOptionValue("sampleMovie"));
            }
            double sampleGenre = 1.0;
            if (cmd.hasOption("sampleGenre")) {
                sampleGenre = Double.parseDouble(cmd.getOptionValue("sampleGenre"));
            }

            // Open logger
            log.open(userDir + folder + "/sample_log.txt");
            log.writeln("");
            log.writeln("IMDBSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + folder);

            // Create full dataset
            DataSet fullDataSet = createFullDataSet();
            writeDataSet(fullDataSet, "test");

            // Create sample dataset
            DataSet sampleDataSet = sampleDataSet(fullDataSet, samplePerson, sampleMovie, sampleGenre);
            writeDataSet(sampleDataSet, "train");

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
                createFullGraph(fullDataSet);
                createSampleGraph(fullDataSet, sampleDataSet);
                createSampleOnlyGraph(sampleDataSet);
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