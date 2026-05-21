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

    // idem pour un album
    private int createAlbumId() {
        int max = 0;
        for (Track t : tracks) { // pour chaque titre
            Album album = t.getAlbum(); // récupère l'album associé
            if (album != null && album.getId() > max) { // si l'album existe et que son id est supérieur au max
                max = t.getAlbum().getId(); // met à jour le max
            }
        }
        return max+1;
    }

    // idem pour un artiste
    public int createArtistId() {
        int max = 0;
        for (Track t : tracks) { // pour chaque titre
            Album album = t.getAlbum(); // récupère l'album associé
            if (album != null){ // si l'album existe
                Artist artist = album.getArtist(); // récupère l'artiste associé
                if (artist != null && artist.getId() > max) { // si l'artiste existe et que son id est supérieur au max
                    max = artist.getId(); // met à jour le max
                }
            }
        }
        return max+1;
    }

    // relie un titre à un artiste et un album
    public void linkTrackToArtistAndAlbum(Track track, String artistName, String albumTitle) {

        // si l'artiste/l'album est nul ou vide, "inconnu"
        if (artistName == null || artistName.isBlank()) artistName = "Inconnu";
        if (albumTitle == null || albumTitle.isBlank()) albumTitle = "Inconnu";

        // récupère l'artiste par son nom
        Artist artist = findArtistByName(artistName);
        if (artist == null) { // si il n'existe pas encore
            artist = new Artist(createArtistId(), artistName); // alors on le crée
        }

        // récupère l'album par le nom de l'album et l'artiste
        Album album = findAlbumByTitleAndArtist(albumTitle, artist);
        if (album == null) { // si il n'existe pas encore
            album = new Album(createAlbumId(), albumTitle, track.getYear()); // alors on le crée
            artist.addAlbum(album); // et on ajoute l'album à l'artiste
        }

        album.addTrack(track); // ajoute le titre à l'album
    }

    // recherche un artiste par son nom
    private Artist findArtistByName(String name) {
        for (Track t : tracks) { // pour chaque titre
            Album album = t.getAlbum(); // récupère l'album dont il fait partie
            if (album != null) { // si l'album existe
                Artist artist = album.getArtist(); // récupère l'artiste qui l'a réalisé
                if (artist != null && artist.getName().equalsIgnoreCase(name)) { // si l'artiste existe et correspond au nom
                    return artist; // alors on l'a trouvé
                }
            }
        }
        return null; // sinon on n'a rien trouvé
    }

    // recherche un album par son nom et l'artiste qui l'a réalisé
    private Album findAlbumByTitleAndArtist(String title, Artist artist) {

        // le nom d'artiste recherché
        String targetArtistName = artist.getName();

        for (Track t : tracks) { // pour chaque titre
            Album albumFound = t.getAlbum(); // récupère l'album dont il fait partie
            if (albumFound != null) { // si l'album existe
                Artist artistFound = albumFound.getArtist(); // récupère l'artiste qui l'a réalisé
                if(artistFound != null){ // si l'artiste existe

                    String foundAlbumTitle = albumFound.getTitle(); // récupère le nom de l'album trouvé
                    String foundArtistTitle = artistFound.getName(); // récupère le nom de l'artiste trouvé

                    if(foundAlbumTitle.equalsIgnoreCase(title) &&
                            foundArtistTitle.equalsIgnoreCase(targetArtistName)){ // si tout matche
                        return albumFound; // alors on l'a trouvé
                    }
                }
            }
        }
        return null; // sinon on n'a rien trouvé
    }
}
