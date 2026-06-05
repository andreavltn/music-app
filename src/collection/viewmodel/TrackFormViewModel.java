package collection.viewmodel;

import collection.model.Track;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

// viewModel du formulaire d'ajout/édition d'un titre
public class TrackFormViewModel {

    private final LibraryViewModel libraryViewModel; // référence au viewModel principal
    private Track track; // null = ajout d'un titre, sinon c'est une modification

    // propriétés observables
    private final StringProperty artist = new SimpleStringProperty("");
    private final StringProperty album = new SimpleStringProperty("");
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty genre = new SimpleStringProperty("");
    private final StringProperty year = new SimpleStringProperty("");
    private final StringProperty duration = new SimpleStringProperty("");
    private final StringProperty filePath = new SimpleStringProperty("");
    private final StringProperty imagePath = new SimpleStringProperty("");
    private final StringProperty error = new SimpleStringProperty(""); // message d'erreur affiché dans la vue

    // initialise le ViewModel avec les données existantes (si c'est une modification
    public TrackFormViewModel(LibraryViewModel lvm, Track track) {
        this.libraryViewModel = lvm;
        this.track = track;

        // si c'est une modification
        if (track != null) {
            // remplit les propriétés avec les données du titre
            artist.set(track.getArtistName());
            album.set(track.getAlbumTitle());
            title.set(track.getTitle());
            genre.set(track.getGenre());
            year.set(String.valueOf(track.getYear()));
            duration.set(String.valueOf(track.getDuration()));
            if (track.hasFile()) filePath.set(track.getFilePath());   // chemin MP3
            if (track.hasImage()) imagePath.set(track.getImagePath()); // chemin image
        }

        // sinon, les propriétés restent vides (ajout d'un titre)
    }

    public StringProperty artistProperty() {
        return artist;
    }
    public StringProperty albumProperty() {
        return album;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty genreProperty() {
        return genre;
    }

    public StringProperty yearProperty() {
        return year;
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public StringProperty errorProperty() {
        return error;
    }

    // renvoie la liste des artistes déjà enregistrés (pour l'autocomplétion)
    public List<String> getArtistSuggestions() {
        return libraryViewModel.getDistinctArtistNames();
    }

    // renvoie la liste des albums filtrés par artiste (pour l'autocomplétion)
    public List<String> getAlbumSuggestions(String artistQuery) {
        String lowerArtist = artistQuery == null ? "" : artistQuery.toLowerCase();
        return libraryViewModel.getDistinctAlbumTitles().stream()
                .filter(a -> lowerArtist.isBlank()
                        || libraryViewModel.getAlbumArtist(a).toLowerCase().contains(lowerArtist))
                .collect(Collectors.toList());
    }

    // appelé quand un fichier MP3 est sélectionné via le sélecteur de fichiers
    public void onFileSelected(File file) {
        if (file == null) return;
        filePath.set(file.getAbsolutePath()); // met à jour le chemin

        // crée un lecteur temporaire pour lire la durée totale du MP3
        // sans lancer la lecture, puis le libère
        Media media = new Media(file.toURI().toString());
        MediaPlayer temp = new MediaPlayer(media);
        temp.setOnReady(() -> {
            duration.set(String.valueOf((int) temp.getTotalDuration().toSeconds())); // met à jour la durée
            temp.dispose(); // libère les ressources
        });
    }

    // réinitialise le chemin du mp3
    public void clearFile()  {
        filePath.set("");
    }

    // réinitialise le chemin de l'image
    public void clearImage() {
        imagePath.set("");
    }

    // valide les champs et sauvegarde le titre dans la librairie
    public boolean save() {

        // récupère les valeurs des champs (sans espaces inutiles)
        String t = title.get().trim();
        String a = artist.get().trim();
        String alb = album.get().trim();
        String g = genre.get().trim();
        String fP = filePath.get().trim();
        String iP = imagePath.get().trim();

        // vérifie que le titre n'est pas vide
        if (t.isBlank()) {
            error.set("Le titre est obligatoire."); return false;
        }

        int y, d;

        // vérifie que l'année est un entier valide
        try {
            y = Integer.parseInt(year.get().trim());
        } catch (NumberFormatException e) {
            error.set("L'année doit être un nombre."); return false;
        }

        // vérifie que la durée est un entier valide
        try {
            d = Integer.parseInt(duration.get().trim());
        } catch (NumberFormatException e) {
            error.set("La durée doit être un nombre."); return false;
        }

        // si c'est un ajout (track==null), crée un nouveau titre
        if (track == null) {
            Track newTrack = new Track(libraryViewModel.createTrackId(),
                    t, a, alb, g, y, d);
            if (!fP.isBlank()) newTrack.setMp3Path(fP); // associe le fichier MP3 (si existant)
            if (!iP.isBlank()) newTrack.setImagePath(iP); // associe l'image (si existante)
            libraryViewModel.addTrack(newTrack); // ajoute le titre à la librairie

            // sinon (track!=null), met à jour le titre existant
        } else {
            track.setTitle(t);
            track.setArtistName(a);
            track.setAlbumTitle(alb);
            track.setGenre(g);
            track.setYear(y);
            track.setDuration(d);
            track.setMp3Path(fP.isBlank() ? null : fP); // retire le MP3 si le champ est vide
            track.setImagePath(iP.isBlank() ? null : iP); // retire l'image si le champ est vide

            libraryViewModel.updateTrack(track); // met à jour dans la librairie
        }

        return true; // sauvegarde réussie
    }
}