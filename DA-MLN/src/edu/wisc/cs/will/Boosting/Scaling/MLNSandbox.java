package edu.wisc.cs.will.Boosting.Scaling;

import java.util.ArrayList;
import java.util.HashMap;

public class MLNSandbox {

    private static class PersonB {
        public boolean smokes;
        public boolean friendsWithPersonA;
    }

    private static class World {
        public boolean personASmokes;
        public ArrayList<PersonB> friends;
    }

    public static void main(String[] args) {
        int numberOfFriends = 10;
        double weight = 1.0;

        int numberSmokingFriendsForSmoker = (int) Math.round((((double) numberOfFriends) / 3) * 2);
        int numberSmokingFriendsForNonSmoker = (int) Math.round((((double) numberOfFriends) / 3) * 1);

        ArrayList<World> allWorlds = new ArrayList<>();



        System.out.println();
    }

}
