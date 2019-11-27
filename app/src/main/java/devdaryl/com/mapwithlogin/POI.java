package devdaryl.com.mapwithlogin;

public class POI {

    private double latitude;
    private double longitude;
    private String name;
    private String description;
    private String type;
    private int likes;
    private int dislikes;
    //private String image;

    public POI(double lat, double lon, String nam, String des, String typ) {
        latitude = lat;
        longitude = lon;
        name = nam;
        description = des;
        type = typ;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

}
