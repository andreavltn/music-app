package collection.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

// représente la librairie des titres de la collection
// encapsule une liste observable de titres
public class Library {

    private final ObservableList<Track> tracks;

    public Library() {
        this.tracks = FXCollections.observableArrayList();
    }

    // ajoute un titre à la librairie
    public void add(Track track) {
        if (track != null && !tracks.contains(track)) {
            tracks.add(track);
        }
    }

    // supprime un titre de la librairie
    public void remove(Track track) {
        tracks.remove(track);
    }

    // met à jour un titre déjà présent dans la librairie
    public void update(Track updated) {
        for (int i = 0; i < tracks.size(); i++) { // pour chaque titre
            if (tracks.get(i).getId() == updated.getId()) { // si l'id correspond
                tracks.set(i, updated); // met à jour le titre (en notifiant les observateurs)
                return;
            }
        }
    }

    // vide la librairie et la remplace par une nouvelle liste
    public void replaceAll(List<Track> newTracks) {
        tracks.setAll(newTracks);
    }

    public ObservableList<Track> getTracks() {
        return tracks;
    }

    // génère un nouvel id de titre à la suite
    public int createTrackId() {
        if (tracks.isEmpty()) return 1; // si librairie vide, id = 1
        int max = 0;
        for (Track t : tracks) { // pour chaque titre
            if (t.getId() > max) { // si l'id est supérieur au max
                max = t.getId(); // met à jour le max
            }
        }
        return max+1; // le prochain id sera max+1
        // ça garantit des id uniques. par exemple si on a id 1,2,3 et qu'on supprime l'id 2,
        // le prochain id sera 4, et donc il n'y aura jamais de doublons dans la librairie
    }
}
