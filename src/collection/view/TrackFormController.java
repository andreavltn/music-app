package collection.view;

import collection.model.Track;
import collection.viewmodel.LibraryViewModel;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class TrackFormController {

    // récupère les composants de l'interface
    @FXML private ComboBox<String> artistComboBox;
    @FXML private ComboBox<String> albumComboBox;
    @FXML private TextField titleField;
    @FXML private TextField genreField;
    @FXML private TextField yearField;
    @FXML private TextField durationField;
    @FXML private TextField filePathField;
    @FXML private TextField imagePathField;
    @FXML private Label errorLabel;

    private LibraryViewModel viewModel;
    private Track track; // null = ajout, sinon modification
    private List<String> allArtists; // liste de tous les artistes
    private int lastFilteredSizeArtist = -1; // outil pour la combobox artiste
    private int lastFilteredSizeAlbum = -1; // idem pour la combobox album

    // méthode qui affiche la formulaire
    public void init(LibraryViewModel vm, Track track) {

        // récupère les données
        this.viewModel = vm;
        this.track = track;

        this.allArtists = viewModel.getDistinctArtistNames(); // récupère la liste des artistes
        artistComboBox.getItems().setAll(this.allArtists); // remplit la comboBox artiste

        // à chaque frappe sur artiste
        artistComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            filterArtists(newVal); // filtre la liste d'artistes
        });

        // à chaque frappe sur album
        albumComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            String currentArtist = artistComboBox.getEditor().getText().trim(); // récupère l'artiste actuel
            filterAlbums(newVal, currentArtist); // filtre la liste des albums par rapport à l'artiste
        });

        // si c'est une modification
        if (track != null) {

            // remplit les champs avec les données existantes
            artistComboBox.setValue(track.getArtistName());
            albumComboBox.setValue(track.getAlbumTitle());
            titleField.setText(track.getTitle());
            genreField.setText(track.getGenre());
            yearField.setText(String.valueOf(track.getYear()));
            durationField.setText(String.valueOf(track.getDuration()));

            // si le titre a un fichier mp3
            if (track.hasFile()) {
                filePathField.setText(track.getFilePath()); // affiche son chemin
            }

            // si le titre a une image
            if (track.hasImage()) {
                imagePathField.setText(track.getImagePath()); // affiche son chemin
            }
        }

        // sinon ne fait rien (champs vides)
    }

    // filtre les artistes selon la saisie et met à jour la comboBox artiste
    private void filterArtists(String query) {

        // récupère ce qui est saisi, en minuscules
        String lowerQuery = (query == null) ? "" : query.toLowerCase();

        // crée une liste filtrée
        List<String> filtered = allArtists.stream()
                .filter(a -> a.toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        // applique le filtre sur la comboBox
        applyFilter(artistComboBox, filtered, lowerQuery, lastFilteredSizeArtist);
        lastFilteredSizeArtist = Math.max(1, filtered.size()); // met à jour la dernière taille
    }

    // filtre les albums selon l'artiste saisi + la saisie album en cours, et met à jour la comboBox album
    private void filterAlbums(String query, String artistQuery) {

        // récupère ce qui est saisi, en minuscules
        String lowerQuery = query == null ? "" : query.toLowerCase();
        String lowerArtist = artistQuery == null ? "" : artistQuery.toLowerCase();

        // crée une liste filtrée
        List<String> filtered = viewModel.getDistinctAlbumTitles().stream()
                .filter(a -> lowerArtist.isBlank() || viewModel.getAlbumArtist(a).toLowerCase().contains(lowerArtist))
                .filter(a -> a.toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        // applique le filtre sur la comboBox
        applyFilter(albumComboBox, filtered, lowerQuery, lastFilteredSizeAlbum);
        lastFilteredSizeAlbum = Math.max(1, filtered.size()); // met à jour la dernière taille
    }

    // méthode utilitaire qui applique le filtre sur une ComboBox
    private void applyFilter(ComboBox<String> comboBox, List<String> filtered, String lowerQuery, int lastSize) {

        // applique le filtre sur la comboBox
        comboBox.getItems().setAll(filtered);

        // récupère la taille ajustée et met à jour l'affichage de la comboBox
        int newSize = Math.max(1, filtered.size());
        comboBox.setVisibleRowCount(newSize);

        // si il y a au moins un résultat
        if (!filtered.isEmpty() && !lowerQuery.isBlank()) {
            // si la taille de la liste des résultats a changé
            if (newSize != lastSize) {
                // rafraichit l'affichage de la liste
                comboBox.hide();
                comboBox.show();
            }
        } else { // sinon cache la liste (pas de résultat correspondant)
            comboBox.hide();
        }
    }

    // méthode qui ferme la fenêtre du formulaire
    private void close() {
        Scene scene = titleField.getScene(); // récupère la scène
        Stage stage = (Stage) scene.getWindow(); // récupère la fenêtre
        stage.close(); // ferme
    }

    @FXML // au clic sur "Fichier mp3"
    private void onChooseFile() {

        FileChooser fileChooser = new FileChooser(); // crée un sélecteur de fichier
        fileChooser.setTitle("Choisir un fichier MP3"); // titre de la fenêtre
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers MP3", "*.mp3") // seulement les .mp3
        );

        Window window = titleField.getScene().getWindow(); // récupère la fenêtre
        File selected = fileChooser.showOpenDialog(window); // ouvre le sélecteur de fichier
        // et récupère le fichier choisi

        if (selected != null) { // si il existe
            String path = selected.getAbsolutePath(); // récupère le chemin absolu du fichier
            filePathField.setText(path); // affiche le chemin absolu du fichier

            // ici on va créer un lecteur media temporaire pour récupérer
            // la durée totale du mp3 associée au titre et l'afficher
            // ensuite on le supprime car on n'en a plus besoin

            // convertit le mp3 en URI puis crée un lecteur média temporaire
            Media media = new Media(selected.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);

            // dès qu'il est prêt
            tempPlayer.setOnReady(() -> {
                // récupère la durée totale et met à jour le champ durée
                Duration total = tempPlayer.getTotalDuration();
                durationField.setText(String.valueOf((int) total.toSeconds()));
                tempPlayer.dispose(); // libère les ressources du lecteur
            });
        }
    }

    @FXML // au clic sur "Image"
    private void onChooseImage() {

        FileChooser fileChooser = new FileChooser(); // crée un sélecteur de fichier
        fileChooser.setTitle("Choisir une image"); // titre de la fenêtre
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg") // seulement les images
        );

        Window window = titleField.getScene().getWindow(); // récupère la fenêtre
        File selected = fileChooser.showOpenDialog(window); // ouvre le sélecteur de fichier
        // et récupère le fichier choisi

        if (selected != null) {
            imagePathField.setText(selected.getAbsolutePath()); // met à jour le chemin
        }
    }

    @FXML // à l'enregistrement du formulaire (bouton "Enregistrer")
    private void onSave() {

        // récupère et valide les champs
        String title = titleField.getText().trim();
        String artist = artistComboBox.getEditor().getText().trim();
        String album = albumComboBox.getEditor().getText().trim();
        String genre = genreField.getText().trim();
        String selectedPath = filePathField.getText().trim();
        String selectedImage = imagePathField.getText().trim();

        // si le titre est vide
        if (title.isBlank()) {
            errorLabel.setText("Le titre est obligatoire."); // erreur
            return;
        }

        int year, duration;

        // vérifie si l'année est un nombre
        try {
            year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("L'année doit être un nombre."); // erreur
            return;
        }

        // idem pour la durée
        try {
            duration = Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("La durée doit être un nombre."); // erreur
            return;
        }

        // si il n'y a pas de titre, formulaire d'ajout
        if (this.track == null) {

            // crée un nouveau titre avec les données du formulaire
            Track track = new Track(viewModel.createTrackId(), title, artist, album, genre, year, duration);

            // si un fichier mp3 a été choisi
            if (!selectedPath.isBlank()) {
                String path = copyFile(selectedPath, "music"); // copie le fichier choisi dans music/
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setMp3Path(path); // sinon met à jour le chemin du titre
            }

            // si une image a été choisie
            if (!selectedImage.isBlank()) {
                String path = copyFile(selectedImage, "images"); // copie le fichier choisi dans images/
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setImagePath(path); // sinon met à jour le chemin de l'image
            }

            viewModel.addTrack(track); // ajout à la librairie

        } else { // sinon c'est un formulaire de modification

            // met à jour les champs du titre en paramètre
            track.setArtistName(artist);
            track.setAlbumTitle(album);
            track.setTitle(title);
            track.setGenre(genre);
            track.setYear(year);
            track.setDuration(duration);

            // si un fichier mp3 a été choisi
            if (!selectedPath.isBlank()) {
                String path = copyFile(selectedPath, "music"); // copie le fichier choisi dans music
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setMp3Path(path); // sinon met à jour le chemin du titre
            }

            // si une image a été choisie
            if (!selectedImage.isBlank()) {
                String path = copyFile(selectedImage, "images"); // copie le fichier choisi dans images/
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setImagePath(path); // sinon met à jour le chemin de l'image
            }

            // met à jour le titre dans la librairie
            viewModel.updateTrack(track);
        }

        close(); // ferme le formulaire
    }

    // recopie un fichier choisi dans un dossier
    private String copyFile(String selectedPath, String folder) {
        File source = new File(selectedPath); // récupère le fichier source
        File dest = new File(folder + "/" + source.getName()); // crée le fichier destination
        try {
            // recopie le contenu du fichier source dans le fichier destination
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return folder + "/" + source.getName(); // renvoie le chemin d'accès du fichier dest
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la copie du fichier."); // erreur
            return null; // pas de chemin d'accès
        }
    }

    @FXML // à la fermeture du formulaire (bouton "Annuler")
    private void onCancel() {
        close();
    }
}