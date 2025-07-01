package com.github.jjstreet.springbugs.issue17379;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class ProtectedEndpointController {

    private final AtomicBoolean first = new AtomicBoolean(true);

    @GetMapping("/protected/value")
    public ResponseEntity<?> protectedEndpoint() {
        if (first.get()) {
            first.set(false);
            return ResponseEntity
                    .status(401)
                    .build();
        }
        return ResponseEntity.ok("Protected");
    }

    @PostMapping("/protected/reset")
    public String reset() {
        first.set(true);
        return "OK";
    }
}
