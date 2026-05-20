package edu.eci.dosw.client;

import edu.eci.dosw.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${external-services.users-url}") String usersUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(usersUrl)
                .build();
    }

    public void deactivateUser(Long accountId, String authorizationHeader) {
        try {
            restClient.patch()
                    .uri("/api/users/{id}/deactivate", accountId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ExternalServiceException(
                    "Could not deactivate user data for account: " + accountId,
                    ex
            );
        }
    }
}