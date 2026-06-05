package collection.view;

import collection.model.Track;
import collection.viewmodel.LibraryViewModel;
import collection.viewmodel.PlayerViewModel;
import collection.viewmodel.TrackFormViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.Comparator;
import java.util.List;

// contrôleur de la vue principale
public class MainController {

    // récupère les composants de l'interface
    @FXML private ListView<Track> trackListView; // liste des titres
    @FXML private TextField searchField; // barre de recherche
    @FXML private Label countLabel; // nombre de titres
    @FXML private ComboBox<String> sortComboBox; // liste déroulante de choix du tri
    @FXML private Button sortOrderBtn; // bouton d'ordre du tri

    // récupère les composants du panneau de détails
    @FXML private Label detailTitle, detailArtist, detailAlbum, detailGenre, detailYear, detailDuration;

    // récupère les composants du lecteur MP3
    @FXML private Label playerTitle; // nom du titre en cours
    @FXML private Label playerArtist; // nom de l'artiste en cours
    @FXML private Label timeLabel; // temps écoulé / durée totale
    @FXML private ImageView playerImage; // image de couverture
    @FXML private Button playBtn; // bouton play/pause
    @FXML private Button stopBtn; // bouton stop
    @FXML private ProgressBar progressBar; // barre de progression

    private LibraryViewModel libraryViewModel; // viewModel de la librairie
    private PlayerViewModel playerViewModel; // viewModel du lecteur mp3
    private boolean isSortingAscending = true; // sens du tri (ascendant par défaut)

    @FXML // appelé automatiquement après chargement de l'interface
    public void initialize() {
        libraryViewModel = new LibraryViewModel();
        playerViewModel  = new PlayerViewModel();

        // branche la liste sur la liste observable du ViewModel
        trackListView.setItems(libraryViewModel.getTracks());

        // binding
        playerTitle.textProperty().bind(playerViewModel.titleTextProperty());
        playerArtist.textProperty().bind(playerViewModel.artistTextProperty());
        timeLabel.textProperty().bind(playerViewModel.timeLabelTextProperty());
        progressBar.progressProperty().bind(playerViewModel.progressProperty());
        playBtn.textProperty().bind(playerViewModel.playBtnTextProperty());
        playerImage.imageProperty().bind(playerViewModel.coverImageProperty());

        // à chaque sélection d'un titre dans la liste
        trackListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    showDetail(newVal); // affiche les détails du titre
                    playerViewModel.load(newVal); // charge le titre dans le lecteur
                });

        // à chaque entrée dans la barre de recherche
        searchField.textProperty().addListener((obs, old, newVal) -> {
            libraryViewModel.filter(newVal); // filtre les titres affichés
            updateCount();  // met à jour le compteur
        });

        // ajout des options de tri à la liste déroulante
        sortComboBox.getItems().addAll("Titre", "Artiste", "Genre", "Année", "Durée");

        // à chaque changement de critère de tri
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) applySortFromCombo(newVal); // applique le nouveau tri
        });

        updateCount(); // initialise le compteur de titres
    }

    // affiche les détails du titre sélectionné
    private void showDetail(Track t) {
        // si aucun titre n'est sélectionné
        if (t == null) {
            // réinitialise tous les labels
            detailTitle.setText("");
            detailArtist.setText("");
            detailAlbum.setText("");
            detailGenre.setText("");
            detailYear.setText("");
            detailDuration.setText("");
        } else {
            // sinon les affiche
            detailTitle.setText(t.getTitle());
            detailArtist.setText("Artiste : " + t.getArtistName());
            detailAlbum.setText("Album : " + t.getAlbumTitle());
            detailGenre.setText("Genre : " + t.getGenre());
            detailYear.setText("Année : " + t.getYear());
            detailDuration.setText("Durée : " + t.getFormattedDuration());
        }
    }

    // met à jour le compteur de titres
    private void updateCount() {
        countLabel.setText(libraryViewModel.getTracks().size() + " titre(s)");
    }

    @FXML // au clic sur le bouton d'ordre du tri
    private void onToggleSortOrder() {
        isSortingAscending = !isSortingAscending; // inverse le sens du tri
        sortOrderBtn.setText(isSortingAscending ? "↑" : "↓"); // met à jour le texte du bouton
        String selected = sortComboBox.getValue();
        if (selected != null) applySortFromCombo(selected); // réapplique le tri avec le nouveau sens
    }

    // construit le comparateur selon le critère choisi et l'envoie au ViewModel
    private void applySortFromCombo(String choice) {
        Comparator<Track> comp = switch (choice) {

            // par titre/artiste/genre : ignore la casse
            case "Titre" -> Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Artiste" -> Comparator.comparing(Track::getArtistName, String.CASE_INSENSITIVE_ORDER);
            case "Genre" -> Comparator.comparing(Track::getGenre, String.CASE_INSENSITIVE_ORDER);

            // par année/durée : tri numérique classique
            case "Année" -> Comparator.comparingInt(Track::getYear);
            case "Durée" -> Comparator.comparingInt(Track::getDuration);
            default -> null;
        };

        libraryViewModel.sort(comp, isSortingAscending); // délègue le tri au viewModel
    }

    @FXML // au clic sur "Ajouter un titre"
    private void onAdd() {
        openForm(null); // null = formulaire d'ajout
    }

    @FXML // au clic sur "Modifier"
    private void onEdit() {
        Track t = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre sélectionné
        if (t != null) {
            openForm(t); // ouvre le formulaire de modification
            showDetail(t); // met à jour les détails après fermeture
        }
    }

    @FXML // au clic sur "Supprimer"
    private void onDelete() {
        Track t = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre sélectionné
        if (t != null) {
            libraryViewModel.removeTrack(t); // supprime le titre
            trackListView.getSelectionModel().clearSelection(); // désélectionne le titre
            updateCount(); // met à jour le compteur de titres
        }
    }

    // ouvre le formulaire d'ajout/modification dans une nouvelle fenêtre
    private void openForm(Track track) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/collection/view/track-form.fxml"));
            Parent root = loader.load();

            // transmet un TrackFormViewModel au contrôleur du formulaire
            TrackFormController controller = loader.getController();
            controller.init(new TrackFormViewModel(libraryViewModel, track));

            Stage stage = new Stage();
            stage.setTitle(track == null ? "Ajouter un titre" : "Modifier un titre");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait(); // bloque la fenêtre principale jusqu'à la fermeture du formulaire

            updateCount(); // rafraîchit le compteur après fermeture
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML // au clic sur le bouton play/pause
    private void onPlayOrPause() {
        playerViewModel.playOrPause(trackListView.getSelectionModel().getSelectedItem()); // délègue au ViewModel
    }

    @FXML // au clic sur le bouton stop
    private void onStop() {
        playerViewModel.stop(); // délègue au ViewModel
    }

    @FXML // au clic sur "Importer"
    private void onImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer une librairie");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));

        Window mainWindow = trackListView.getScene().getWindow(); // remonte à la fenêtre principale
        File file = fileChooser.showOpenDialog(mainWindow);

        if (file != null) {
            List<Track> tracksFromFile = collection.util.JsonManager.loadFrom(file.getAbsolutePath());
            libraryViewModel.importTracks(tracksFromFile); // délègue l'import au viewModel
            updateCount();

            // alerte de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Importation");
            alert.setHeaderText(null);
            alert.setContentText("Librairie importée avec succès !");
            alert.showAndWait();
        }
    }

    @FXML // au clic sur "Exporter"
    private void onExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la bibliothèque");
        fileChooser.setInitialFileName("library.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));

        Window mainWindow = trackListView.getScene().getWindow(); // remonte à la fenêtre principale
        File file = fileChooser.showSaveDialog(mainWindow);

        if (file != null) {
            List<Track> tracksFromLibrary = libraryViewModel.exportTracks(); // récupère les titres via le viewModel
            collection.util.JsonManager.saveTo(file.getAbsolutePath(), tracksFromLibrary); // sauvegarde

            // alerte de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportation");
            alert.setHeaderText(null);
            alert.setContentText("Librairie exportée avec succès !");
            alert.showAndWait();
        }
    }
}