package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    /**
     * GET /internal/platforms/verify <br>
     * TODO: Replace with actual data
     * @return Verification for an api_key and an api_secret
     */
    @PostMapping("/platforms/verify")
    public ResponseEntity<?> postVerifyPlatform(
            @RequestBody(required = false) PlatformVerificationRequest verificationRequest
    ) {
        if (verificationRequest != null && verificationRequest.getApi_key().equals("test-key") && verificationRequest.getApi_secret().equals("test-secret")) {
            return ResponseEntity.ok(new PlatformVerificationResponse(
                    true,
                    "platform-abc-123",
                    "ARKA Technologies"
            ));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PlatformVerificationError(
                        new ArrayList<>(),
                        "string",
                        "string",
                        "string"
                ));
    }
}
