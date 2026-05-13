package lynx.team2.rest_api.models;

import java.util.List;

public class PlatformVerificationError {
    List<Object> loc;
    String msg;
    String type;
    String input;

    public PlatformVerificationError(List<Object> loc, String msg, String type, String input) {
        this.loc = loc;
        this.msg = msg;
        this.type = type;
        this.input = input;
    }
}
