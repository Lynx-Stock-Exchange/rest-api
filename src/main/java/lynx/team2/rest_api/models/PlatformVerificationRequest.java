package lynx.team2.rest_api.models;

public class PlatformVerificationRequest {
    String api_key;
    String api_secret;

    public PlatformVerificationRequest(String api_key, String api_secret) {
        this.api_key = api_key;
        this.api_secret = api_secret;
    }

    public String getApi_key() { return api_key; }
    public String getApi_secret() { return api_secret; }
}
