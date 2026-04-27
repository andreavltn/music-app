package collection.model;

import javafx.collections.ObservableList;

// calcule les statistiques des titres
public class Stats {

    private final ObservableList<Track> tracks;

    public Stats(ObservableList<Track> tracks) {
        this.tracks = tracks;
    }
}