package collection.model;

// représente un titre, associé à un seul et unique album
public class Track {

    private int id;
    private String title;
    private String artistName;
    private String albumTitle;
    private String genre;
    private int year;
    private int duration;
    private String filePath;

    public Track(int id, String title, String artistName, String albumTitle, String genre, int year, int duration) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.albumTitle = albumTitle;
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

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // renvoie true si le titre à un fichier mp3 associé
    public boolean hasFile() {
        return (filePath != null && !filePath.isBlank());
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
    public String toString() {
        return title + " - " + getArtistName() + " (" + getFormattedDuration() + ")";
    }
}