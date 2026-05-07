package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "platforms")
public class PlatformEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "api_secret", nullable = false)
    private String apiSecret;

    public PlatformEntity() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getApiKey() { return apiKey; }
    public String getApiSecret() { return apiSecret; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
}
