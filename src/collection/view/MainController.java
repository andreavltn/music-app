package collection.view;

import collection.model.Track;
import collection.util.JsonManager;
import collection.viewmodel.LibraryViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Comparator;
import java.io.File;
import java.util.List;

public class MainController {

    // récupère les composants de l'interface
    @FXML private ListView<Track> trackListView; // liste des titres
    @FXML private TextField searchField; // barre de recherche
    @FXML private Label countLabel; // nombre de titres
    @FXML private ComboBox<String> sortComboBox; // liste déroulante de choix du tri
    @FXML private Button sortOrderBtn; // bouton d'ordre du tri

    // récupère les composants des détails du titre
    @FXML private Label detailTitle;
    @FXML private Label detailArtist;
    @FXML private Label detailAlbum;
    @FXML private Label detailGenre;
    @FXML private Label detailYear;
    @FXML private Label detailDuration;

    // récupère les composants du lecteur mp3
    @FXML private Label playerTitle; // nom du titre
    @FXML private Label playerArtist; // nom de l'artiste
    @FXML private Button playBtn; // bouton play
    @FXML private Button stopBtn; // bouton stop
    @FXML private ProgressBar progressBar; // barre de progression
    @FXML private Label timeLabel; // temps du lecteur

    private LibraryViewModel viewModel;
    private boolean isSortingAscending = true; // sens du tri (ascendant/descendant)
    private MediaPlayer mediaPlayer; // lecteur mp3

    @FXML // appelé automatiquement après chargement de l'affichage
    public void initialize() {
        this.viewModel = new LibraryViewModel();

        // branche l'affichage sur la liste observable du ViewModel
        trackListView.setItems(viewModel.getTracks());

        // à chaque sélection d'un titre dans la liste
        trackListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            showDetail(newVal); // affiche les détails du titre
        });

        // à chaque entrée dans la barre de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.filter(newVal); // filtre les titres affichés
            updateCount(); // met à jour le nombre de titres
        });

        // ajout des options de tri à la liste
        sortComboBox.getItems().addAll("Titre", "Artiste", "Genre", "Année", "Durée");

        // à chaque changement de critère de tri
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySortFromCombo(newVal); // applique le tri correspondant
            }
        });

        updateCount(); // met à jour le nombre de titres
    }

    // affiche les détails d'un titre sur le panneau à droite
    private void showDetail(Track t) {

        stopPlayer(); // arrête la lecture du titre (si elle était en cours)

        // si aucun titre sélectionné
        if (t == null) {
            // détails
            detailTitle.setText("");
            detailArtist.setText("");
            detailAlbum.setText("");
            detailGenre.setText("");
            detailYear.setText("");
            detailDuration.setText("");

            // lecteur mp3
            playerTitle.setText("");
            playerArtist.setText("");
        } else {
            // sinon affiche ses informations

            // détails
            detailTitle.setText(t.getTitle());
            detailArtist.setText("Artiste : " + t.getArtistName());
            detailAlbum.setText("Album : " + t.getAlbumTitle());
            detailGenre.setText("Genre : " + t.getGenre());
            detailYear.setText("Année : " + t.getYear());
            detailDuration.setText("Durée : " + t.getFormattedDuration());

            // lecteur mp3
            playerTitle.setText(t.getTitle());
            playerArtist.setText(t.getArtistName());

            // si le titre a un mp3
            if (t.hasFile()) {

                // active les boutons
                playBtn.setDisable(false);
                stopBtn.setDisable(false);

                File file = new File(t.getFilePath()); // récupère le fichier lié
                if (file.exists()) { // si il existe

                    // ici on va créer un lecteur media temporaire pour récupérer
                    // la durée totale du mp3 associée au titre et l'afficher
                    // ensuite on le supprime car on n'en a plus besoin

                    // convertit le mp3 en URI et crée un lecteur média temporaire
                    Media media = new Media(file.toURI().toString());
                    MediaPlayer tempPlayer = new MediaPlayer(media);

                    // dès que le lecteur est prêt
                    tempPlayer.setOnReady(() -> {
                        // récupère la durée totale du titre et l'affiche
                        Duration total = tempPlayer.getTotalDuration();
                        timeLabel.setText("0:00 / " + formatTime(total));
                        tempPlayer.dispose(); // libère les ressources du lecteur
                    });

                } else { // sinon le fichier n'existe pas
                    timeLabel.setText("Fichier introuvable"); // message erreur
                }
            }

            // sinon le titre n'a pas de mp3 associé
            else { // sinon la fichier

                // désactive les boutons
                playBtn.setDisable(true);
                stopBtn.setDisable(true);
                timeLabel.setText("");
            }
        }
    }

    // méthode qui met à jour le nombre de titres
    private void updateCount() {
        int n = viewModel.getTracks().size();
        countLabel.setText(n + " titre(s)");
    }

    @FXML // au clic sur le bouton d'ordre du tri (ascendant/descendant)
    private void onToggleSortOrder() {
        isSortingAscending = !isSortingAscending; // inverse l'ordre du tri
        sortOrderBtn.setText(isSortingAscending ? "↑" : "↓"); // change le texte du bouton
        String selected = sortComboBox.getValue(); // récupère la nouvelle valeur de tri
        if (selected != null) {
            applySortFromCombo(selected); // applique le tri associé à cette valeur
        }
    }

    // construit le comparateur selon le critère choisi
    private void applySortFromCombo(String sortingChoice) {
        Comparator<Track> comp = switch (sortingChoice) {
            // par titre/artiste/genre : ignore la casse
            case "Titre" -> Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Artiste" -> Comparator.comparing(Track::getArtistName, String.CASE_INSENSITIVE_ORDER);
            case "Genre" -> Comparator.comparing(Track::getGenre, String.CASE_INSENSITIVE_ORDER);
            // sinon avec des nombres, tri classique
            case "Année" -> Comparator.comparingInt(Track::getYear);
            case "Durée" -> Comparator.comparingInt(Track::getDuration);
            default -> null;
        };
        viewModel.sort(comp, isSortingAscending); // envoie le tri au modèle
    }

    @FXML // au clic sur le bouton "Ajouter un titre"
    private void onAdd() {
        openForm(null); // ouvre le formulaire d'ajout d'un titre (car param = null)
    }

    // méthode qui ouvre le formulaire d'ajout/de modification
    // si il y a une Track en paramètre, c'est une modification du titre
    // sinon c'est l'ajout d'un nouveau titre
    private void openForm(Track track) {
        try {

            // récupère le fichier fxml et instancie le controleur
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/collection/view/track-form.fxml"));
            Parent root = loader.load();

            // transmet les données pour l'affichage du formulaire
            TrackFormController controller = loader.getController();
            controller.init(viewModel, track);

            // crée la scène
            Stage stage = new Stage(); // nouvelle fenêtre
            stage.setTitle(track == null ? "Ajouter un titre" : "Modifier un titre"); // choix du titre
            stage.setScene(new javafx.scene.Scene(root)); // ajoute la scène à la fenêtre
            stage.showAndWait(); // bloque la fenêtre principale tant que ce n'est pas fini

            updateCount(); // rafraîchit le compteur de titres après fermeture
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML // au clic sur le bouton "Modifier"
    public void onEdit() {
        Track selectedTrack = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre sélectionné
        if (selectedTrack != null) {
            openForm(selectedTrack); // ouvre le formulaire d'édition (car param != null)
            showDetail(selectedTrack); // met à jour les détails du titre
        }
    }

    @FXML // au clic sur le bouton "Supprimer"
    public void onDelete() {
        Track selected = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre
        if (selected != null) {
            viewModel.removeTrack(selected); // supprime le titre
            trackListView.getSelectionModel().clearSelection(); // aucun titre n'est sélectionné
            updateCount(); // met à jour le nombre de titres
        }
    }

    @FXML // au clic sur le bouton "Importer"
    private void onImport() {

        // ouvre une fenêtre de sélection de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer une librairie"); // titre de la fenêtre

        // seule l'extension .json est autorisée
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));

        Window mainWindow = trackListView.getScene().getWindow(); // remonte à la fenêtre principale
        File file = fileChooser.showOpenDialog(mainWindow); // ouvre le sélecteur de fichier
        // et récupère le fichier choisi

        if (file != null) { // si le fichier choisi existe
            String importPath = file.getAbsolutePath(); // récupère le chemin absolu du fichier
            List<Track> tracksFromFile = JsonManager.loadFrom(importPath); // récupère les titres du fichier
            viewModel.importTracks(tracksFromFile); // importe les titres dans la librairie
            updateCount(); // met à jour le nombre de titres
        }

        // alerte de confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Importation"); // titre de l'alerte
        alert.setHeaderText(null); // pas de texte en haut
        alert.setContentText("Librairie importée avec succès!"); // texte au centre
        alert.showAndWait(); // affiche l'alerte et attend confirmation
    }

    @FXML // au clic sur le bouton "Exporter"
    private void onExport() {

        // ouvre une fenêtre de sauvegarde de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la bibliothèque"); // titre de la fenêtre
        fileChooser.setInitialFileName("library.json");  // nom initial proposé à l'utilisateur

        // seule l'extension .json est autorisée
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));

        Window mainWindow = trackListView.getScene().getWindow(); // remonte à la fenêtre principale
        File file = fileChooser.showSaveDialog(mainWindow); // crée le chemin
        if (file != null) {
            String exportPath = file.getAbsolutePath(); // récupère le chemin absolu du fichier
            List<Track> tracksFromLibrary = viewModel.exportTracks(); // récupère les titres de la librairie
            JsonManager.saveTo(exportPath, tracksFromLibrary); // exporte les titres dans le fichier
        }

        // alerte de confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportation"); // titre de l'alerte
        alert.setHeaderText(null); // pas de texte en haut
        alert.setContentText("Librairie exportée avec succès!"); // texte au centre
        alert.showAndWait(); // affiche l'alerte et attend confirmation
    }

    @FXML // au clic sur le bouton play/pause
    private void onPlayOrPause() {

        // si on vient de lancer le titre pour la première fois
        if (mediaPlayer == null) { // (le lecteur média n'existe pas encore)

            // récupère le titre sélectionné
            Track selected = trackListView.getSelectionModel().getSelectedItem();

            // si il est nul ou qu'il n'a pas de fichier, stop
            if (selected == null || !selected.hasFile()) return;

            // récupère le fichier mp3 du titre
            File file = new File(selected.getFilePath());

            // si il n'existe pas, affiche un message d'erreur + stop
            if (!file.exists()) {
                timeLabel.setText("Fichier introuvable");
                return;
            }

            // convertit le mp3 en URI pour le media player
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // dès que le lecteur est prêt
            mediaPlayer.setOnReady(() -> {
                // récupère la durée totale du titre et l'affiche
                Duration total = mediaPlayer.getTotalDuration();
                timeLabel.setText("0:00 / " + formatTime(total));
            });

            // à la progression du temps sur le titre
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (mediaPlayer == null) return; // si le media est nul, stop (sécurité)

                Duration total = mediaPlayer.getTotalDuration(); // stocke la durée totale

                // si il reste du temps
                if (total != null && total.greaterThan(Duration.ZERO)) {

                    // calcule la nouvelle progression et met à jour la barre
                    double progression = newTime.toSeconds() / total.toSeconds();
                    progressBar.setProgress(progression);

                    // met à jour le temps écoulé sur le titre
                    timeLabel.setText(formatTime(newTime) + " / " + formatTime(total));
                }
            });

            // quand le morceau se termine
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("▶"); // met à jour le texte du bouton
                progressBar.setProgress(0); // remet la barre de progression à 0%
                mediaPlayer.dispose(); // libère les ressources du lecteur
                mediaPlayer = null; // supprime le lecteur
            });

            mediaPlayer.play(); // joue le titre
            playBtn.setText("⏸"); // met à jour le texte du bouton

        }

        // sinon si le titre était en train de jouer
        else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause(); // met le lecteur en pause
            playBtn.setText("▶"); // met à jour le texte du bouton
        }

        // sinon on relance le titre après une pause
        else {
            mediaPlayer.play(); // joue le titre
            playBtn.setText("⏸"); // met à jour le texte du bouton
        }
    }

    @FXML // au clic sur le bouton stop (le carré)
    private void onStop() {
        stopPlayer();
    }

    private void stopPlayer() {
        // si le lecteur média existe
        if (mediaPlayer != null) {
            Duration total = mediaPlayer.getTotalDuration(); // récupère la durée totale du titre
            mediaPlayer.stop(); // stoppe le lecteur
            mediaPlayer.dispose(); // libère les ressources du lecteur
            mediaPlayer = null; // supprime le lecteur
            playBtn.setText("▶"); // met à jour le texte du bouton
            progressBar.setProgress(0); // remet la barre à 0
            timeLabel.setText("0:00 / " + formatTime(total)); // remet le temps à 0
        }
    }

    // renvoie le temps formaté à partir d'une durée
    private String formatTime(Duration d) {
        int minutes = (int) d.toMinutes(); // récupère les minutes
        int seconds = (int) (d.toSeconds() % 60); // récupère les secondes
        return String.format("%d:%02d", minutes, seconds); // renvoie le texte formaté
    }

}