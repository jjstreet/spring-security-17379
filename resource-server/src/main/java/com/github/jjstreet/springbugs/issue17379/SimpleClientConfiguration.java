package com.github.jjstreet.springbugs.issue17379;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class SimpleClientConfiguration {

    @Bean
    public SimpleClient simpleClient(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository, authorizedClientRepository);
        filter.setDefaultClientRegistrationId("simple");
        // register the filter with the WebClient as a filter and not through the
        // convenience method the filter provides. this will enable the bug.
        var wc = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .filter(filter)
                .build();
        return new SimpleClient(wc);
    }
}
