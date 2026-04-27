package collection.model;

import java.util.ArrayList;
import java.util.List;

// représente un artiste (seul ou groupe), associé à sa liste d'albums
public class Artist {

    private int id;
    private String name;
    private List<Album> albums;

    public Artist(int id, String name) {
        this.id = id;
        this.name = name;
        this.albums = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    // ajoute un album à la discographie de l'artiste
    public void addAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
            album.setArtist(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist)) return false;
        return id == ((Artist) o).id;
    }

    @Override
    public String toString() {
        return name;
    }
}