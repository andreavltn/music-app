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

public class TrackFormController {

    // récupère les composants de l'interface
    @FXML private TextField artistField;
    @FXML private TextField albumField;
    @FXML private TextField titleField;
    @FXML private TextField genreField;
    @FXML private TextField yearField;
    @FXML private TextField durationField;
    @FXML private TextField filePathField;
    @FXML private Label errorLabel;

    private LibraryViewModel viewModel;
    private Track track; // null = ajout, sinon modification

    // méthode qui affiche la formulaire
    public void init(LibraryViewModel vm, Track track) {

        // récupère les données
        this.viewModel = vm;
        this.track = track;

        // si c'est une modification
        if (track != null) {

            // remplit les champs avec les données existantes
            artistField.setText(track.getArtistName());
            albumField.setText(track.getAlbumTitle());
            titleField.setText(track.getTitle());
            genreField.setText(track.getGenre());
            yearField.setText(String.valueOf(track.getYear()));
            durationField.setText(String.valueOf(track.getDuration()));

            if (track.hasFile()) { // si le titre a un fichier mp3
                filePathField.setText(track.getFilePath()); // affiche son chemin
            }
        }

        // sinon ne fait rien (champs vides)
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

    @FXML // à l'enregistrement du formulaire (bouton "Enregistrer")
    private void onSave() {

        // récupère et valide les champs
        String artist = artistField.getText().trim();
        String album = albumField.getText().trim();
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String selectedPath = filePathField.getText().trim();

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
            Track track = new Track(viewModel.createTrackId(), title, genre, year, duration);

            // si un fichier mp3 a été choisi
            if (!selectedPath.isBlank()) {
                String path = copyMp3(selectedPath); // copie le fichier choisi dans music
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setFilePath(path); // sinon met à jour le chemin du titre
            }

            viewModel.linkTrack(track, artist, album); // lie le titre à l'artiste et l'album
            viewModel.addTrack(track); // ajout à la librairie

        } else { // sinon c'est un formulaire de modification

            // met à jour les champs du titre en paramètre
            track.setTitle(title);
            track.setGenre(genre);
            track.setYear(year);
            track.setDuration(duration);

            // si un fichier mp3 a été choisi
            if (!selectedPath.isBlank()) {
                String path = copyMp3(selectedPath); // copie le fichier choisi dans music
                if (path == null) return; // si pas de chemin, stoppe la fonction (echec copie)
                track.setFilePath(path); // sinon met à jour le chemin du titre
            }

            // lie le titre à l'artiste et l'album
            viewModel.linkTrack(track, artist, album);

            // met à jour le titre dans la librairie
            viewModel.updateTrack(track);
        }

        close(); // ferme le formulaire
    }

    // recopie un fichier mp3 choisi dans le dossier "music/", et renvoie son chemin d'accès
    private String copyMp3(String selectedPath) {
        File source = new File(selectedPath); // récupère le fichier source
        File dest = new File("music/" + source.getName()); // crée le fichier destination
        try {
            // recopie le contenu du fichier source dans le fichier destination
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "music/" + source.getName(); // renvoie le chemin d'accès du fichier dest
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