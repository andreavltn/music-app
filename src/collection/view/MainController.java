package collection.view;

import collection.model.Track;
import collection.viewmodel.LibraryViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Comparator;

public class MainController {

    // récupère les composants de l'interface
    @FXML private ListView<Track> trackListView; // liste des titres
    @FXML private TextField searchField; // barre de recherche
    @FXML private Label countLabel; // nombre de titres
    @FXML private ComboBox<String> sortComboBox; // liste déroulante de choix du tri
    @FXML private Button sortOrderBtn; // bouton d'ordre du tri

    // récupère labels des détails du titre
    @FXML private Label detailTitle;
    @FXML private Label detailArtist;
    @FXML private Label detailAlbum;
    @FXML private Label detailGenre;
    @FXML private Label detailYear;
    @FXML private Label detailDuration;

    private LibraryViewModel viewModel;
    private boolean isSortingAscending = true; // sens du tri (ascendant/descendant)

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

        // si aucun titre sélectionné
        if (t == null) {
            detailTitle.setText("");
            detailArtist.setText("");
            detailAlbum.setText("");
            detailGenre.setText("");
            detailYear.setText("");
            detailDuration.setText("");
        } else {
            // sinon affiche ses informations
            detailTitle.setText(t.getTitle());
            detailArtist.setText("Artiste : " + t.getArtistName());
            detailAlbum.setText("Album : " + t.getAlbumTitle());
            detailGenre.setText("Genre : " + t.getGenre());
            detailYear.setText("Année : " + t.getYear());
            detailDuration.setText("Durée : " + t.getFormattedDuration());
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

    @FXML private void onAdd() {
        openForm(null); // ouvre le formulaire d'ajout d'un titre
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

    @FXML
    private void onStats() {
        // A IMPLEMENTER
    }

    @FXML
    public void onEdit() {
        Track selectedTrack = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre sélectionné
        if (selectedTrack != null) {
            openForm(selectedTrack); // ouvre le formulaire d'édition
            showDetail(selectedTrack); // met à jour les détails du titre
        }
    }

    @FXML
    public void onDelete() {
        Track selected = trackListView.getSelectionModel().getSelectedItem(); // récupère le titre
        if (selected != null) {
            viewModel.removeTrack(selected); // supprime le titre
            trackListView.getSelectionModel().clearSelection(); // aucun titre n'est sélectionné
            updateCount(); // met à jour le nombre de titres
        }
    }
}