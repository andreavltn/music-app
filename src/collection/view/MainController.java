package collection.view;

import collection.model.Track;
import collection.viewmodel.LibraryViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {

    // récupère les composants par leur fx:id
    @FXML private ListView<Track> trackListView; // liste des titres
    @FXML private TextField searchField; // barre de recherche
    @FXML private Label countLabel; // nombre de titres
    @FXML private ComboBox<String> sortComboBox; // liste déroulante de choix du tri
    @FXML private Button sortOrderBtn; // bouton d'ordre du tri

    private LibraryViewModel viewModel;

    // appelé automatiquement après chargement de l'affichage
    @FXML
    public void initialize() {
        this.viewModel = new LibraryViewModel();

        // branche l'affichage sur la liste observable du ViewModel
        trackListView.setItems(viewModel.getTracks());

        updateCount(); // met à jour le nombre de titres
    }

    // action des boutons (vide pour l'instant)
    @FXML private void onAdd() {}
    @FXML private void onStats() {}
    @FXML private void onToggleSortOrder() {}

    // méthode qui met à jour le nombre de titres
    private void updateCount() {
        int n = viewModel.getTracks().size();
        countLabel.setText(n + " titre(s)");
    }
}