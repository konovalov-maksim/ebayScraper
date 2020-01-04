package core.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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

    public String toLimitedString(int limit) {
        String result = this.toString();
        if (result.length() <= limit) return result;
        result = result.substring(0, limit);
        result = result.substring(0, result.lastIndexOf(" "));
        return result;
    }

    @Override
    public String toString() {
        return (title != null ? title + " " : "")
                + (year != null ? year + " " : "")
                + (genre.size() > 0 ? String.join(" ", genre).length() + " " : "")
                + (format.size() > 0 ? String.join(" ", format) : "")
                ;
    }
}
