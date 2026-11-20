# MusicApp — Gestionnaire d'une collection musicale

## Thème

MusicApp est une application permettant de gérer une collection de titres musicaux. 
L'utilisateur peut y enregistrer des morceaux, chacun accompagné de ses données (titre, artiste, album, genre, année, durée).
Il est aussi possible d'y associer un fichier MP3, qui peut être lu via le lecteur sur le panneau à droite, ainsi qu'une image
de couverture, affichée sur le même panneau.

## Fonctionnalités

### Fonctionnalités principales

- **Affichage global** : liste scrollable de tous les titres
- **Affichage détaillé** : panneau central affichant toutes les données du titre sélectionné (titre, artiste, album, genre, année, durée)
- **Ajout** : formulaire permettant d'ajouter un nouveau titre avec auto-complétion
- **Modification** : modification d'un titre existant via le même formulaire
- **Suppression** : suppression complète d'un titre sélectionné
- **Import / Export** : import et export de la librairie au format JSON
- **Filtrage** : barre de recherche pour filtrer les titres selon une entrée
- **Tri** : tri croissant ou décroissant par titre, artiste, genre, année ou durée

### Fonctionnalité supplémentaire : Lecteur MP3 intégré

Un lecteur audio est intégré directement dans l'interface. Un clic simple sur un titre affiche ses informations, 
un clic double ouvre le lecteur et lance le titre (si un mp3 est associé). Celui-ci permet de :
- Lire, mettre en pause et stopper la lecture d'un fichier MP3 associé à un titre
- Afficher l'image de couverture du titre sélectionné
- Suivre l'avancée de la lecture via une barre de progression et un affichage du temps écoulé / total.

### Persistance automatique

La librairie est sauvegardée automatiquement dans un fichier `library.json` à chaque modification 
(ajout, édition, suppression, import), et rechargée au démarrage de l'application. 
Cela permet d'avoir une persistence automatique sans avoir à importer/exporter une librairie complète 
à chaque redémarrage de l'application. 
- Pour l'image et le fichier mp3 associé à un titre, leurs chemins absolus sont utilisés. Si un fichier 
est déplacé, l'application affichera le message "fichier introuvable".

## Architecture

L'application suit le modèle **MVVM** :

- `App.java` : point d'entrée
- `collection.model` : `Track.java`, `Library.java` : le modèle de l'application
- `collection.viewmodel` : `LibraryViewModel.java`, `PlayerViewModel.java`, `TrackFormViewModel.java` : les vues du modèle
- `collection.view` : `MainController.java`, `TrackFormController.java` (+ fichiers FXML et CSS)
- `collection.util` : `JsonManager.java` (sérialisation/désérialisation JSON)

### Rôle des ViewModels

- `LibraryViewModel` : gère la liste des titres, le filtrage, le tri, le CRUD et la persistance
- `PlayerViewModel` : gère l'état du lecteur MP3 (lecture, pause, stop, progression, image de couverture) 
- et expose des propriétés observables bindées par `MainController`
- `TrackFormViewModel` : gère la logique du formulaire d'ajout/modification (validation, extraction de durée MP3, 
- création et mise à jour d'un titre)

## Outils utilisés

- Java 24.0.2
- FXML + CSS (affichage)
- Gson (sérialisation JSON)
- javafx.scene.media (lecture MP3)

## Prérequis

- Java 24 ou supérieur (JDK ou JRE)
- JavaFX SDK 24.0.2, à télécharger séparément : https://gluonhq.com/products/javafx/
- MusicApp.jar (voir l'onglet Releases)

Gson (sérialisation JSON) est déjà inclus dans le .jar, rien d'autre à installer.

## Lancement

Ouvrir un terminal dans le dossier contenant MusicApp.jar, puis :

Windows :
java --module-path "C:\chemin\vers\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -jar MusicApp.jar

Linux / macOS :
java --module-path /chemin/vers/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.media -jar MusicApp.jar

Remplacer le chemin par celui du dossier "lib" de votre JavaFX SDK.
Si un fichier library.json est présent dans le dossier de lancement, il est chargé automatiquement comme librairie.
