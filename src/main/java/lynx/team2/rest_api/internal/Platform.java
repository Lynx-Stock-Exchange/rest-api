package lynx.team2.rest_api.internal;

public class Platform {
    private String id;
    private String name;

    public Platform(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
