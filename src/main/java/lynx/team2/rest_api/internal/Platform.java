package lynx.team2.rest_api.internal;

public class Platform {
    private String id;
    private String name;
    private String apiKey;
    private String apiSecret;

    public Platform(String id, String name, String apiKey, String apiSecret) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
