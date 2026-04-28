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

    // nom du fichier de stockage de la librairie
    private static final String FILE_PATH = "library.json";
    // instance gson de la librairie
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // sauvegarder les titres
    public static void save(List<Track> tracks) {
        try (Writer writer = new FileWriter(FILE_PATH)) { // ouvre le fichier en écriture
            gson.toJson(tracks, writer); // sérialise et écrit les titres
        } catch (IOException e) {
            e.printStackTrace(); // erreur
        }
    }

    // charger les titres
    public static List<Track> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(file)) { // ouvre le fichier en lecture
            Type listType = new TypeToken<ArrayList<Track>>(){}.getType(); // contourne l'effacement du type
            return gson.fromJson(reader, listType); // renvoie la liste des titres
        } catch (IOException e) {
            e.printStackTrace(); // erreur
            return new ArrayList<>(); // liste vide si erreur
        }
    }
}