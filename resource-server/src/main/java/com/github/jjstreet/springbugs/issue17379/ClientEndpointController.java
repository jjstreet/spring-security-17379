package com.github.jjstreet.springbugs.issue17379;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientEndpointController {

    private final SimpleClient simpleClient;

    public ClientEndpointController(
            SimpleClient simpleClient) {
        this.simpleClient = simpleClient;
    }

    @GetMapping("/from-client")
    public String fromClient() {
        return simpleClient.getValue();
    }
}

