package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.internal.PlatformService;
import lynx.team2.rest_api.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    private final PlatformService platformService;

    public InternalController(PlatformService platformService) {
        this.platformService = platformService;
    }

    /**
     * GET /internal/platforms/verify <br>
     * @return Verification for an api_key and an api_secret
     */
    @PostMapping("/platforms/verify")
    public ResponseEntity<?> postVerifyPlatform(
            @RequestBody(required = false) PlatformVerificationRequest verificationRequest
    ) {
        if (verificationRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing request body");
        }

        Platform platform = platformService.verify(verificationRequest.getApi_key(), verificationRequest.getApi_secret());

        if (platform != null) {
            return ResponseEntity.ok(new PlatformVerificationResponse(
                    true,
                    platform.getId(),
                    platform.getName()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new PlatformVerificationError(
                        new ArrayList<>(),
                        "UNAUTHORIZED",
                        "Invalid credentials",
                        "Platform verification failed"
                ));
    }
}
