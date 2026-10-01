package bushfireAlert.com.example.myapp.model;

public class BookmarkItem {
    public String name;
    public double lat;
    public double lon;
    public String temp = "--°C";
    public String humidity = "--%";
    public String lastUpdated = "";

    public BookmarkItem() {
        // Required for Firebase
    }

    public BookmarkItem(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
