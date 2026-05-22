package collection.viewmodel;

import collection.model.Library;
import collection.model.Track;
import collection.util.JsonManager;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // si la librairie est vide
        if (library.getTracks().isEmpty()) {
            loadTestData(); // la remplit de données test
        }

        this.filteredTracks = new FilteredList<>(library.getTracks()); // liste des titres (filtrée)
        this.sortedTracks = new SortedList<>(filteredTracks); // liste des titres (filtrée et triée)

        JsonManager.save(library.getTracks()); // sauvegarde les titres
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
        JsonManager.save(library.getTracks()); // sauvegarde les titres
    }

    // supprime un titre de la librairie
    public void removeTrack(Track track) {
        library.remove(track); // supprime le titre
        JsonManager.save(library.getTracks()); // sauvegarde les titres
    }

    // met à jour un titre de la librairie
    public void updateTrack(Track track) {
        library.update(track); // met à jour le titre
        JsonManager.save(library.getTracks()); // sauvegarde les titres
    }

    // importe une liste de titres qui remplace celle de la librairie
    public void importTracks(List<Track> tracks) {
        library.replaceAll(tracks); // remplace tous les titres de la librairie
        JsonManager.save(library.getTracks()); // sauvegarde les titres
    }

    // récupère la liste des titres de la librairie
    public List<Track> exportTracks() {
        return new ArrayList<>(library.getTracks());
    }

    // récupère la liste de tous les différents artistes enregistrés
    public List<String> getDistinctArtistNames() {
        return library.getTracks().stream()
                .map(Track::getArtistName)
                .filter(name -> !name.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // récupère la liste de tous les différents albums enregistrés
    public List<String> getDistinctAlbumTitles() {
        return library.getTracks().stream()
                .map(Track::getAlbumTitle)
                .filter(a -> !a.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // renvoie l'artiste associé à un album
    public String getAlbumArtist(String albumTitle) {
        return library.getTracks().stream()
                .filter(t -> t.getAlbumTitle().equals(albumTitle))
                .map(Track::getArtistName)
                .findFirst()
                .orElse("");
    }

    // remplit la librairie de titres (pour tester)
    private void loadTestData() {

        // 3 titres avec musique et image libres de droit
        Track t1 = new Track(1, "SampleTrack1", "Artist1", "Album1", "Genre1", 1, 148);
        t1.setMp3Path("music/sample1.mp3");
        t1.setImagePath("images/sample1.jpg");
        library.add(t1);

        Track t2 = new Track(2, "SampleTrack2", "Artist2", "Album2", "Genre1", 2, 69);
        t2.setMp3Path("music/sample2.mp3");
        t2.setImagePath("images/sample2.jpg");
        library.add(t2);

        Track t3 = new Track(3, "SampleTrack3", "Artist3", "Album3", "Genre3", 3, 157);
        t3.setMp3Path("music/sample3.mp3");
        t3.setImagePath("images/sample3.jpg");
        library.add(t3);

        // le reste pour remplir la librairie
        library.add(new Track(4, "Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", 1975, 354));
        library.add(new Track(5, "Billie Jean", "Michael Jackson", "Thriller", "Pop", 1982, 294));
        library.add(new Track(6, "Lose Yourself", "Eminem", "8 Mile", "Rap", 2002, 326));
        library.add(new Track(7, "Smells Like Teen Spirit", "Nirvana", "Nevermind", "Rock", 1991, 301));
        library.add(new Track(8, "Hotel California", "Eagles", "Hotel California", "Rock", 1977, 391));
        library.add(new Track(9, "Shape of You", "Ed Sheeran", "Divide", "Pop", 2017, 233));
        library.add(new Track(10, "Blinding Lights", "The Weeknd", "After Hours", "Synthwave", 2019, 200));
        library.add(new Track(11, "Imagine", "John Lennon", "Imagine", "Soft Rock", 1971, 183));
        library.add(new Track(12, "Wonderwall", "Oasis", "(What's the Story) Morning Glory?", "Britpop", 1995, 258));
        library.add(new Track(13, "Hey Jude", "The Beatles", "Hey Jude", "Rock", 1968, 431));
        library.add(new Track(14, "Rolling in the Deep", "Adele", "21", "Soul", 2010, 228));
        library.add(new Track(15, "One More Time", "Daft Punk", "Discovery", "Electronic", 2000, 320));
        library.add(new Track(16, "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "Rock", 1971, 482));
        library.add(new Track(17, "Bad Guy", "Billie Eilish", "When We All Fall Asleep, Where Do We Go?", "Pop", 2019, 194));
        library.add(new Track(18, "Take Five", "Dave Brubeck", "Time Out", "Jazz", 1959, 324));
        library.add(new Track(19, "HUMBLE.", "Kendrick Lamar", "DAMN.", "Rap", 2017, 177));
        library.add(new Track(20, "Viva La Vida", "Coldplay", "Viva La Vida or Death and All His Friends", "Alternative", 2008, 242));
        library.add(new Track(21, "Sweet Child O' Mine", "Guns N' Roses", "Appetite for Destruction", "Rock", 1987, 356));
        library.add(new Track(22, "Get Lucky", "Daft Punk", "Random Access Memories", "Funk", 2013, 248));
        library.add(new Track(23, "Someone Like You", "Adele", "21", "Pop", 2011, 285));
        library.add(new Track(24, "Radioactive", "Imagine Dragons", "Night Visions", "Alternative Rock", 2012, 186));
        library.add(new Track(25, "Numb", "Linkin Park", "Meteora", "Nu Metal", 2003, 185));
        library.add(new Track(26, "Stayin' Alive", "Bee Gees", "Saturday Night Fever", "Disco", 1977, 285));
        library.add(new Track(27, "Clocks", "Coldplay", "A Rush of Blood to the Head", "Alternative", 2002, 307));
        library.add(new Track(28, "Take On Me", "a-ha", "Hunting High and Low", "Synthpop", 1985, 225));
    }
}