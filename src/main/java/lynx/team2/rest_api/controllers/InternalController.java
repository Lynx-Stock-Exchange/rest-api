package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.internal.PlatformService;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    private final PlatformService platformService;
    private final StateStore stateStore;

    public InternalController(PlatformService platformService, StateStore stateStore) {
        this.platformService = platformService;
        this.stateStore = stateStore;
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

    @GetMapping("/platforms/active")
    public ResponseEntity<Map<String, Object>> getActivePlatforms() {
        List<Map<String, Object>> platformList = stateStore.getAllPlatforms().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("is_active", true);
                    return map;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("platforms", platformList);
        return ResponseEntity.ok(response);
    }
}
