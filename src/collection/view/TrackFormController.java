package collection.view;

import collection.model.Track;
import collection.viewmodel.LibraryViewModel;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TrackFormController {

    // récupère les composants de l'interface
    @FXML private TextField titleField;
    @FXML private TextField genreField;
    @FXML private TextField yearField;
    @FXML private TextField durationField;
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
            titleField.setText(track.getTitle());
            genreField.setText(track.getGenre());
            yearField.setText(String.valueOf(track.getYear()));
            durationField.setText(String.valueOf(track.getDuration()));
        }

        // sinon ne fait rien (champs vides)
    }

    // méthode qui ferme la fenêtre du formulaire
    private void close() {
        Scene scene = titleField.getScene(); // récupère la scène
        Stage stage = (Stage) scene.getWindow(); // récupère la fenêtre
        stage.close(); // ferme
    }

    @FXML // à l'enregistrement du formulaire (bouton "Enregistrer")
    private void onSave() {

        // récupère et valide les champs
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();

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
            Track t = new Track(viewModel.createId(), title, genre, year, duration);
            viewModel.addTrack(t); // ajout à la librairie

        } else { // sinon c'est un formulaire de modification

            // met à jour les champs du titre en paramètre
            track.setTitle(title);
            track.setGenre(genre);
            track.setYear(year);
            track.setDuration(duration);

            // met à jour le titre dans la librairie
            viewModel.updateTrack(track);
        }

        close(); // ferme le formulaire
    }

    @FXML // à la fermeture du formulaire (bouton "Annuler")
    private void onCancel() {
        close();
    }
}