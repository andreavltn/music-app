package collection.viewmodel;

import collection.model.Track;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

// viewModel du lecteur mp3
public class PlayerViewModel {

    private MediaPlayer mediaPlayer; // lecteur média

    // propriétés observables
    private final StringProperty titleText = new SimpleStringProperty("");
    private final StringProperty artistText = new SimpleStringProperty("");
    private final StringProperty timeLabelText = new SimpleStringProperty("0:00 / 0:00");
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty playBtnText = new SimpleStringProperty("▶");
    private final ObjectProperty<Image> coverImage = new SimpleObjectProperty<>(null);

    public StringProperty titleTextProperty() {
        return titleText;
    }

    public StringProperty artistTextProperty() {
        return artistText;
    }

    public StringProperty timeLabelTextProperty() {
        return timeLabelText;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public StringProperty playBtnTextProperty() {
        return playBtnText;
    }

    public ObjectProperty<Image> coverImageProperty() {
        return coverImage;
    }

    // appelé à la sélection d'un titre dans la liste
    public void load(Track track) {
        if (track == null) {
            return;
        } else {
            stop();
        }

        // met à jour le nom du titre et de l'artiste
        titleText.set(track.getTitle());
        artistText.set(track.getArtistName());

        // si le titre a une image associée
        if (track.hasImage()) {
            File img = new File(track.getImagePath());
            coverImage.set(img.exists() ? new Image(img.toURI().toString()) : null); // affiche l'image si elle existe
        } else {
            coverImage.set(null); // sinon vide, pas d'image
        }

        // si le titre a un fichier MP3 associé
        if (track.hasFile()) {
            File file = new File(track.getFilePath());
            if (file.exists()) {

                // crée un lecteur temporaire pour lire la durée totale du MP3
                Media media = new Media(file.toURI().toString());
                MediaPlayer temp = new MediaPlayer(media);
                temp.setOnReady(() -> {
                    timeLabelText.set("0:00 / " + formatTime(temp.getTotalDuration())); // affiche la durée totale
                    temp.dispose(); // libère les ressources
                });
            } else {
                timeLabelText.set("Fichier introuvable"); // message d'erreur si fichier absent (déplacé/supprimé)
            }
        }
    }

    // appelé au clic sur le bouton play/pause
    public void playOrPause(Track track) {

        // si le lecteur n'existe pas encore (premier clic sur play)
        if (mediaPlayer == null) {

            if (track == null || !track.hasFile()) return; // sécurité : rien à jouer

            File file = new File(track.getFilePath());
            if (!file.exists()) { // si le fichier est introuvable
                timeLabelText.set("Fichier introuvable"); // affiche message d'erreur
                return;
            }

            // crée le lecteur média à partir du fichier MP3
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // dès que le lecteur est prêt, affiche la durée totale
            mediaPlayer.setOnReady(() ->
                    timeLabelText.set("0:00 / " + formatTime(mediaPlayer.getTotalDuration()))
            );

            // à chaque avancée de la lecture, met à jour la barre de progression + label
            mediaPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (mediaPlayer != null) {
                    Duration total = mediaPlayer.getTotalDuration();
                    if (total != null && total.greaterThan(Duration.ZERO)) {
                        progress.set(newT.toSeconds() / total.toSeconds()); // maj barre de progression
                        timeLabelText.set(formatTime(newT) + " / " + formatTime(total)); // maj texte
                    }
                }
            });

            // quand le morceau se termine naturellement
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtnText.set("▶"); // remet le bouton en mode play
                progress.set(0); // remet la barre à 0
                mediaPlayer.dispose();
                mediaPlayer = null;
            });

            mediaPlayer.play(); // lance la lecture
            playBtnText.set("⏸"); // affiche le bouton pause

            // si le titre était en cours de lecture
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause(); // met en pause
            playBtnText.set("▶");

            // si le titre était en pause
        } else {
            mediaPlayer.play(); // reprend la lecture
            playBtnText.set("⏸");
        }
    }

    // appelé au clic sur le bouton stop
    public void stop() {
        if (mediaPlayer != null) {
            Duration total = mediaPlayer.getTotalDuration(); // récupère la durée avant de libérer
            mediaPlayer.stop(); // arrête la lecture
            mediaPlayer.dispose();
            mediaPlayer = null;
            playBtnText.set("▶");
            progress.set(0); // remet la barre de progression à 0
            timeLabelText.set("0:00 / " + formatTime(total)); // remet le timer à 0
        }
    }

    // renvoie true si le lecteur mp3 existe
    public boolean hasPlayer() {
        return mediaPlayer != null;
    }

    // formate une durée en "minutes:secondes"
    private String formatTime(Duration d) {
        int minutes = (int) d.toMinutes();
        int seconds = (int) (d.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}