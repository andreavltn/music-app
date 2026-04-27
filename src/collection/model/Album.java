package collection.model;

import java.util.ArrayList;
import java.util.List;

// représente un album contenant des titres, associé à un artiste
public class Album {

    private int id;
    private String title;
    private int year;
    private Artist artist;
    private List<Track> tracks;

    public Album(int id, String title, int year) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.tracks = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Artist getArtist() {
        return artist;
    }
    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    // ajoute un titre à l'album
    public void addTrack(Track track) {
        if (!tracks.contains(track)) {
            tracks.add(track);
            track.setAlbum(this);
        }
    }

    // supprime un titre de l'album
    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        return id == ((Album) o).id;
    }

    @Override
    public String toString() {
        return title + " (" + year + ")";
    }
}