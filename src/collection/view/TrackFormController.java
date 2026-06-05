package collection.view;

import collection.viewmodel.TrackFormViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

// contrôleur du formulaire d'ajout/modification d'un titre
public class TrackFormController {

    // récupère les composants de l'interface
    @FXML private ComboBox<String> artistComboBox; // liste déroulante artiste (avec autocomplétion)
    @FXML private ComboBox<String> albumComboBox; // liste déroulante album (avec autocomplétion)
    @FXML private TextField titleField; // champ titre
    @FXML private TextField genreField; // champ genre
    @FXML private TextField yearField;  // champ année
    @FXML private TextField durationField; // champ durée (secondes)
    @FXML private TextField filePathField; // champ chemin du fichier MP3 (lecture seule)
    @FXML private TextField imagePathField; // champ chemin de l'image (lecture seule)
    @FXML private Label errorLabel; // label d'erreur formulaire

    private TrackFormViewModel viewModel; // viewModel du formulaire
    private int lastFilteredSizeArtist = -1; // dernière taille de la liste filtrée artiste (outil)
    private int lastFilteredSizeAlbum = -1; // idem pour album (outil)

    // appelé par MainController pour initialiser le formulaire
    public void init(TrackFormViewModel vm) {
        this.viewModel = vm;

        // binding
        artistComboBox.getEditor().textProperty().bindBidirectional(vm.artistProperty());
        albumComboBox.getEditor().textProperty().bindBidirectional(vm.albumProperty());
        titleField.textProperty().bindBidirectional(vm.titleProperty());
        genreField.textProperty().bindBidirectional(vm.genreProperty());
        yearField.textProperty().bindBidirectional(vm.yearProperty());
        durationField.textProperty().bindBidirectional(vm.durationProperty());
        filePathField.textProperty().bindBidirectional(vm.filePathProperty());
        imagePathField.textProperty().bindBidirectional(vm.imagePathProperty());
        errorLabel.textProperty().bind(vm.errorProperty()); // unidirectionnel : la View lit l'erreur du ViewModel

        // charge les suggestions d'artistes dans la comboBox
        artistComboBox.getItems().setAll(vm.getArtistSuggestions());

        // à chaque saisie dans le champ artiste, filtre les titres
        artistComboBox.getEditor().textProperty().addListener((obs, old, newVal) -> {
            filterArtists(newVal);
        });

        // à chaque saisie dans le champ album, filtre les titres selon l'artiste déjà choisi
        albumComboBox.getEditor().textProperty().addListener((obs, old, newVal) -> {
            filterAlbums(newVal, artistComboBox.getEditor().getText());
        });
    }

    // filtre les suggestions d'artistes selon la saisie
    private void filterArtists(String query) {
        String lower = query == null ? "" : query.toLowerCase();
        List<String> filtered = viewModel.getArtistSuggestions().stream()
                .filter(a -> a.toLowerCase().contains(lower)).toList();
        applyFilter(artistComboBox, filtered, lower, lastFilteredSizeArtist);
        lastFilteredSizeArtist = Math.max(1, filtered.size()); // met à jour la dernière taille
    }

    // filtre les suggestions d'albums selon la saisie et l'artiste déjà saisi
    private void filterAlbums(String query, String artist) {
        String lower = query == null ? "" : query.toLowerCase();
        List<String> filtered = viewModel.getAlbumSuggestions(artist).stream()
                .filter(a -> a.toLowerCase().contains(lower)).toList();
        applyFilter(albumComboBox, filtered, lower, lastFilteredSizeAlbum);
        lastFilteredSizeAlbum = Math.max(1, filtered.size()); // met à jour la dernière taille
    }

    // méthode utilitaire : applique le filtre sur une ComboBox et rafraîchit l'affichage si nécessaire
    private void applyFilter(ComboBox<String> cb, List<String> filtered, String lower, int lastSize) {
        int newSize = Math.max(1, filtered.size());
        cb.setVisibleRowCount(newSize);

        if (!filtered.isEmpty() && !lower.isBlank()) {
            // rafraîchit la liste uniquement si sa taille a changé (pour éviter les clignotements)
            if (newSize != lastSize) { cb.hide(); cb.show(); }
        } else {
            cb.hide(); // cache la liste si aucun résultat
        }
    }

    @FXML // au clic sur "Parcourir" (fichier MP3)
    private void onChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un fichier MP3");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers MP3", "*.mp3"));
        File f = fc.showOpenDialog(titleField.getScene().getWindow());
        viewModel.onFileSelected(f); // délègue la mise à jour du chemin et de la durée au ViewModel
    }

    @FXML // au clic sur "Parcourir" (image)
    private void onChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(titleField.getScene().getWindow());
        if (f != null) viewModel.imagePathProperty().set(f.getAbsolutePath()); // met à jour la propriété du ViewModel
    }

    @FXML // au clic sur la croix à côté du fichier MP3
    private void onClearFile() {
        viewModel.clearFile();  // supprime le fichier choisi
    }

    @FXML // au clic sur la croix à côté de l'image
    private void onClearImage() {
        viewModel.clearImage(); // supprime l'image choisie
    }

    @FXML // au clic sur "Enregistrer"
    private void onSave() {
        if (viewModel.save()) close(); // délègue la validation + sauvegarde au viewModel
    }

    @FXML // au clic sur "Annuler"
    private void onCancel() {
        close(); // ferme la fenêtre du formulaire
    }

    // méthode qui ferme la fenêtre du formulaire
    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}