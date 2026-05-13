package lynx.team2.rest_api.models;

public class PlatformVerificationResponse {
    boolean valid = false;
    String platform_id;
    String platform_name;

    public PlatformVerificationResponse(boolean valid, String platform_id, String platform_name) {
        this.valid = valid;
        this.platform_id = platform_id;
        this.platform_name = platform_name;
    }

    public boolean isValid() { return valid; }
    public String getPlatform_id() { return platform_id; }
    public String getPlatform_name() { return platform_name; }
}
