# Spring Security 17379

## Overview
Adding a `ServletOAuth2AuthorizedClientExchangeFilterFunction` to a `WebClient` using the `WebClientBuilder.filter()` method can result in an `IllegalArgumentEception` indicating `"request cannot be null"`. This happens if the filter was successful in getting a token, but the protected endpoint returned response that the webclient is set up to retry.

## Running This Sample
1. Clone the repository
2. Start the `auth-server` and `resource-server` applications using `bootRun` or within your IDE.
3. Use a REST client like Postman or curl to make a request to the `resource-server` endpoint `http://localhost:8080/from-client` using a GET request. No authentication is needed.
4. Observe that the response returned is status `500` and the `resource-server` has logged an `IllegalArugmentException`.

## Root of Problem

The `ServletOAuth2AuthorizedClientExchangeFilterFunction` does not set the servlet request and response objects in the `ClientRequest` attributes map when performing the actual exchange. Thus, if a retry occurs, the request is not part of the attributes and the `AuthorizationFailureForwarder` will not work correctly. You must use the convenience method on the filter function so that the filter automatically adds the request and responses to the attributes map--which is unintuitive behavior.

## Possible Solution

Ideally, the filter should function as expected without a convenience method. This can be achieved by always attempting to attach the request and response objects to the `ClientRequest` object when the fitler performs the exchange:

```java

@Override
public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return mergeRequestAttributesIfNecessary(request)
            .flatMap(merged -> Mono.just(merged)
                    .filter((req) -> req.attribute(OAUTH2_AUTHORIZED_CLIENT_ATTR_NAME).isPresent())
                    .flatMap((req) -> reauthorizeClient(getOAuth2AuthorizedClient(req.attributes()), req))
                    .switchIfEmpty(
                            Mono.just(merged)
                                    .filter((req) -> resolveClientRegistrationId(req) != null)
                                    .flatMap((req) -> authorizeClient(resolveClientRegistrationId(req), req)))
                    .map((authorizedClient) -> bearer(merged, authorizedClient))
                    .flatMap((requestWithBearer) -> exchangeAndHandleResponse(requestWithBearer, next))
                    .switchIfEmpty(exchangeAndHandleResponse(merged, next)));
}
```

In the above snippet, the `mergeRequestAttributesIfNecessary` method is used to ensure that the request and response attributes are merged into the `ClientRequest` before proceeding with the authorization and exchange process. This way, if a retry occurs, the necessary attributes will be present, and the `AuthorizationFailureForwarder` can function correctly.