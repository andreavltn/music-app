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