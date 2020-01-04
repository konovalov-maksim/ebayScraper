package core.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Release {

    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("year")
    private String year;

    @Expose
    @SerializedName("genre")
    private List<String> genre = new ArrayList<>();

    @Expose
    @SerializedName("format")
    private List<String> format = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public List<String> getFormat() {
        return format;
    }

    public void setFormat(List<String> format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return title + " "
                + year + " "
                + String.join(" ", genre) + " "
                + String.join(" ", format)
                ;
    }
}
