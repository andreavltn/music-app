package collection.util;

import collection.model.Track;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

// classe de gestion du gson
public class JsonManager {

    // nom du fichier de stockage de la librairie (par défaut)
    private static final String FILE_PATH = "library.json";
    // instance gson de la librairie
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // sauvegarde les titres dans le fichier de la librairie (par défaut)
    public static void save(List<Track> tracks) {
        saveTo(FILE_PATH, tracks);
    }

    // charge les titres depuis le fichier de la librairie (par défaut)
    public static List<Track> load() {
        return loadFrom(FILE_PATH);
    }

    // sauvegarde les titres dans un fichier donné
    public static void saveTo(String path, List<Track> tracks) {
        try (Writer writer = new FileWriter(path)) { // ouvre le fichier en écriture
            gson.toJson(tracks, writer); // sérialise et écrit les titres
        } catch (IOException e) {
            e.printStackTrace(); // erreur
        }
    }

    // charge les titres depuis un fichier donné
    public static List<Track> loadFrom(String path) {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<>(); // si le fichier n'existe pas, vide

        try (Reader reader = new FileReader(file)) { // ouvre le fichier en lecture
            Type listType = new TypeToken<ArrayList<Track>>(){}.getType(); // contourne l'effacement du type
            return gson.fromJson(reader, listType); // renvoie la liste des titres
        } catch (IOException e) {
            e.printStackTrace(); // erreur
            return new ArrayList<>(); // liste vide si erreur
        }
    }
}