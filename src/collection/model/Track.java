package collection.model;

// représente un titre, associé à un seul et unique album
public class Track {

    private int id;
    private String title;
    private String genre;
    private int year;
    private int duration;
    private Album album;

    public Track(int id, String title, String genre, int year, int duration) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    // remonte à l'artiste via l'album (null si pas dans un album)
    public Artist getArtist() {
        return (album != null) ? album.getArtist() : null;
    }

    // renvoie le nom de l'artiste ("Inconnu" s'il n'y en a pas)
    public String getArtistName() {
        Artist a = getArtist();
        return (a != null) ? a.getName() : "Inconnu";
    }

    // renvoie le nom de l'album ("Inconnu" s'il n'y en a pas)
    public String getAlbumTitle() {
        return (album != null) ? album.getTitle() : "Inconnu";
    }

    // renvoie la durée du titre, formatée en (minutes:secondes)
    public String getFormattedDuration() {
        int minutes = duration/60;
        int seconds = duration%60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track)) return false;
        return id == ((Track) o).id;
    }

    @Override
    public String toString() { return title + " - " + getArtistName(); }
}