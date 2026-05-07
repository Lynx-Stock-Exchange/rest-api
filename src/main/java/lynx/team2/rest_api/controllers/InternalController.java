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

    @PostMapping("/platforms/verify")
    public ResponseEntity<?> postVerifyPlatform(
            @RequestBody(required = false) PlatformVerificationRequest verificationRequest
    ) {
        if (verificationRequest == null
                || verificationRequest.getApi_key() == null
                || verificationRequest.getApi_secret() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PlatformVerificationError(new ArrayList<>(), "INVALID_REQUEST", "Missing credentials", "api_key and api_secret are required"));
        }

        Platform platform = platformService.verify(
                verificationRequest.getApi_key(),
                verificationRequest.getApi_secret()
        );

        if (platform == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PlatformVerificationError(new ArrayList<>(), "PLATFORM_NOT_AUTHORIZED", "Invalid API credentials", "No platform found with these credentials"));
        }

        return ResponseEntity.ok(new PlatformVerificationResponse(true, platform.getId(), platform.getName()));
    }
}
