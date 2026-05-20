package collection.viewmodel;

import collection.model.Album;
import collection.model.Artist;
import collection.model.Library;
import collection.model.Track;
import collection.util.JsonManager;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LibraryViewModel {

    private final Library library; // librairie
    private final FilteredList<Track> filteredTracks; // liste filtrée
    private final SortedList<Track> sortedTracks; // liste filtrée et triée

    private Comparator<Track> currentComparator = null; // comparateur de titres
    private boolean isSortingAscending = true; // ordre de tri (ascendant initialement)
    private String currentQuery = ""; // texte actuellement tappé dans la barre de recherche

    public LibraryViewModel() {
        this.library = new Library();
        library.replaceAll(JsonManager.load()); // charge la librairie (si elle existe)

        // TEST -------------------
        if (library.getTracks().isEmpty()) {
            Track t1 = new Track(1, "Bohemian Rhapsody", "Rock", 1975, 354);
            library.linkTrackToArtistAndAlbum(t1, "Queen", "A Night at the Opera");
            library.add(t1);

            Track t2 = new Track(2, "Billie Jean", "Pop", 1982, 294);
            library.linkTrackToArtistAndAlbum(t2, "Michael Jackson", "Thriller");
            library.add(t2);

            Track t3 = new Track(3, "Lose Yourself", "Rap", 2002, 326);
            library.linkTrackToArtistAndAlbum(t3, "Eminem", "8 Mile");
            library.add(t3);

            Track t4 = new Track(4, "Smells Like Teen Spirit", "Rock", 1991, 301);
            library.linkTrackToArtistAndAlbum(t4, "Nirvana", "Nevermind");
            library.add(t4);

            Track t5 = new Track(5, "Hotel California", "Rock", 1977, 391);
            library.linkTrackToArtistAndAlbum(t5, "Eagles", "Hotel California");
            library.add(t5);
        }
        // ------------------------

        this.filteredTracks = new FilteredList<>(library.getTracks()); // liste des titres (filtrée)
        this.sortedTracks = new SortedList<>(filteredTracks); // liste des titres (filtrée et triée)

        JsonSave();
    }

    public ObservableList<Track> getTracks() {
        return this.sortedTracks;
    }

    public Library getLibrary() {
        return this.library;
    }

    public int createTrackId() {
        return this.library.createTrackId();
    }

    // appelée à chaque nouvelle entrée dans la barre de recherche
    public void filter(String query) {

        // met à jour les champs
        if(query==null){ // si recherche vide
            this.currentQuery = ""; // filtre vide
        } else {
            this.currentQuery = query; // sinon filtre
        }

        applyFilter(); // applique le filtre sur la recherche
    }

    // applique le filtre associé au champ currentQuery
    private void applyFilter() {

        // si la recherche est vide
        if (currentQuery.isBlank()) {
            filteredTracks.setPredicate(t -> true); // affiche tout
        } else { // sinon
            String lower = currentQuery.toLowerCase(); // met la chaine en minuscules

            // si le titre/artiste/album/genre contient la chaine :
            // affiche le titre associé
            filteredTracks.setPredicate(t ->
                    t.getTitle().toLowerCase().contains(lower) ||
                            t.getArtistName().toLowerCase().contains(lower) ||
                            t.getAlbumTitle().toLowerCase().contains(lower) ||
                            t.getGenre().toLowerCase().contains(lower)
            );
        }
    }

    // appelée à chaque changement d'ordre de tri (clic sur le bouton)
    public void sort(Comparator<Track> comparator, boolean isSortingAscending) {
        // met à jour les champs
        this.currentComparator = comparator;
        this.isSortingAscending = isSortingAscending;
        applySort(); // applique le tri
    }

    // applique le tri associé au champ isSortingAscending (tri ascendant/descendant)
    private void applySort() {
        if (currentComparator == null) {
            sortedTracks.setComparator(null); // pas de tri
        } else if (isSortingAscending) {
            sortedTracks.setComparator(currentComparator); // tri ascendant
        } else {
            sortedTracks.setComparator(currentComparator.reversed()); // tri descendant (inverse)
        }
    }

    // ajoute un titre à la librairie
    public void addTrack(Track track) {
        library.add(track); // ajoute le titre
        JsonSave(); // sauvegarde
    }

    // supprime un titre de la librairie
    public void removeTrack(Track track) {
        library.remove(track); // supprime le titre
        JsonSave(); // sauvegarde
    }

    // met à jour un titre de la librairie
    public void updateTrack(Track track) {
        library.update(track); // met à jour le titre
        JsonSave(); // sauvegarde
    }

    // vide les listes internes pour éviter le bouclage infini
    private void clearLinks() {
        List<Track> tracks = library.getTracks(); // récupère les titres
        for (Track t : tracks) { // pour chaque titre
            Album album = t.getAlbum();
            if (album != null) { // si l'album existe
                album.getTracks().clear(); // vide sa liste de titres
                Artist artist = album.getArtist(); // récupère l'artiste qui l'a réalisé
                if (artist != null) { // si l'artiste existe
                    artist.getAlbums().clear(); // vide la liste des albums qu'il a réalisé
                }
            }
        }
    }

    // reconstruit les listes internes
    private void buildLinks() {
        List<Track> tracks = library.getTracks(); // récupère les titres
        for (Track t : tracks) { // pour chaque titre
            Artist artist = t.getArtist(); // récupère l'artiste qui l'a réalisé
            Album album = t.getAlbum(); // récupère l'album dont il fait partie

            // recrée tous les liens
            library.linkTrackToArtistAndAlbum(t, t.getArtistName(), t.getAlbumTitle());
        }
    }

    // importe une liste de titres qui remplace celle de la librairie
    public void importTracks(List<Track> tracks) {
        library.replaceAll(tracks); // remplace tous les titres de la librairie
        JsonSave();
    }

    // récupère la liste des titres de la librairie
    public List<Track> exportTracks() {

        ArrayList<Track> tracksToExport = new ArrayList<>(); // liste d'exportation vide
        List<Track> tracks = library.getTracks(); // liste des titres originale

        for (Track track : tracks) { // pour chaque titre

            Track cloneTrack = new Track(track.getId(), track.getTitle(), track.getGenre(),
                    track.getYear(), track.getDuration()); // crée une copie indépendante

            Album album = track.getAlbum(); // récupère l'album du titre original
            if (album != null) { // si il existe

                Album cleanAlbum = new Album(track.getId(), track.getAlbumTitle(),
                        track.getYear()); // recrée un album propre

                if (album.getArtist() != null) { // si l'artiste de l'album existe

                    Artist cleanArtist = new Artist(track.getAlbum().getArtist().getId(),
                            track.getArtistName()); // récrée un artiste propre

                    cleanAlbum.setArtist(cleanArtist); // associe l'artiste à l'album
                }
                cloneTrack.setAlbum(cleanAlbum); // associe l'album au titre
            }
            tracksToExport.add(cloneTrack); // ajoute le titre à la liste d'exportation
        }
        return tracksToExport; // renvoie la liste d'exportation propre avec listes vides
    }

    // lie un titre à un artiste et un album
    public void linkTrack(Track track, String artistName, String albumTitle) {
        this.library.linkTrackToArtistAndAlbum(track, artistName, albumTitle);
    }

    // sauvegarde sécurisée de la librairie dans "library.json"
    private void JsonSave() {
        clearLinks(); // vide les listes internes pour éviter le bouclage infini
        JsonManager.save(library.getTracks()); // sauvegarde les titres sur le json
        buildLinks(); // reconstruit les listes
    }
}