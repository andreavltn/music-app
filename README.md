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

Un lecteur audio est intégré directement dans l'interface. Il permet de :
- Lire, mettre en pause et stopper la lecture d'un fichier MP3 associé à un titre
- Afficher l'image de couverture du titre sélectionné
- Suivre l'avancée de la lecture via une barre de progression et un affichage du temps écoulé / total

### Persistance automatique

La librairie est sauvegardée automatiquement dans un fichier `library.json` à chaque modification 
(ajout, édition, suppression, import), et rechargée au démarrage de l'application. 
Cela permet d'avoir une persistence automatique sans avoir à importer/exporter une librairie complète 
à chaque redémarrage de l'application. 
- Pour l'image et le fichier mp3 associé à un titre, leurs chemins absolus sont utilisés. Si un fichier 
est déplacé, l'application affichera le message "fichier introuvable".

## Architecture

L'application suit le modèle **MVVM** :

- `collection.model` : `Track`, `Library`
- `collection.viewmodel` : `LibraryViewModel`
- `collection.view` : `MainController`, `TrackFormController` (+ fichiers FXML)
- `collection.util` : `JsonManager` (sérialisation / désérialisation JSON)

## Outils utilisés

- Java 24.0.2
- FXML + CSS (affichage)
- Gson (sérialisation JSON)
- javafx.scene.media (lecture MP3)

## Comment lancer l'application?

Dans le terminal, depuis le répertoire contenant l'application, exécuter la commande suivante :
- `java --module-path [JAVAFX] --add-modules javafx.controls,javafx.fxml,javafx.media -jar collection-fxml.jar`
- où `[JAVAFX]` est le chemin absolu du répertoire "lib" de JavaFX sur la machine utilisée
- /!\ ne pas oublier le module javafx.media (nécessaire pour utiliser le lecteur mp3)
- la commande ci-dessus fonctionne pour Windows, éventuellement utiliser des \ au lieu des / pour les chemins d'accès sur Linux