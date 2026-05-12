package collection.viewmodel;

import collection.model.Library;
import collection.model.Track;
import collection.util.JsonManager;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

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
            library.add(new Track(1,  "Bohemian Rhapsody",      "Rock",       1975, 354));
            library.add(new Track(2,  "Billie Jean",             "Pop",        1982, 294));
            library.add(new Track(3,  "Lose Yourself",           "Rap",        2002, 326));
            library.add(new Track(4,  "Smells Like Teen Spirit", "Rock",       1991, 301));
            library.add(new Track(5,  "Hotel California",        "Rock",       1977, 391));
            library.add(new Track(6,  "Thriller",                "Pop",        1982, 358));
            library.add(new Track(7,  "Superstition",            "Soul",       1972, 245));
            library.add(new Track(8,  "Respect",                 "Soul",       1967, 147));
            library.add(new Track(9,  "What's Going On",         "Soul",       1971, 235));
            library.add(new Track(10, "Like a Rolling Stone",    "Rock",       1965, 369));
            library.add(new Track(11, "Purple Rain",             "Pop",        1984, 520));
            library.add(new Track(12, "Killing Me Softly",       "Pop",        1973, 280));
            library.add(new Track(13, "Enter Sandman",           "Metal",      1991, 331));
            library.add(new Track(14, "One More Time",           "Electronic", 2001, 320));
            library.add(new Track(15, "Redbone",                 "Soul",       2016, 295));
        }
        // ------------------------

        this.filteredTracks = new FilteredList<>(library.getTracks()); // liste des titres (filtrée)
        this.sortedTracks = new SortedList<>(filteredTracks); // liste des titres (filtrée et triée)
    }

    public ObservableList<Track> getTracks() {
        return this.sortedTracks;
    }

    public Library getLibrary() {
        return this.library;
    }

    public int createId() {
        return this.library.createId();
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
        library.add(track);
        JsonManager.save(library.getTracks()); // sauvegarde
    }

    // supprime un titre de la librairie
    public void removeTrack(Track track) {
        library.remove(track);
        JsonManager.save(library.getTracks()); // sauvegarde
    }

    // met à jour un titre de la librairie
    public void updateTrack(Track track) {
        library.update(track);
        JsonManager.save(library.getTracks()); // sauvegarde
    }

    // importe une liste de titres qui remplace celle de la librairie
    public void importTracks(List<Track> tracks) {
        library.replaceAll(tracks);
        JsonManager.save(library.getTracks()); // sauvegarde
    }

    // récupère la liste des titres de la librairie
    public List<Track> exportTracks() {
        return library.getTracks();
    }
}