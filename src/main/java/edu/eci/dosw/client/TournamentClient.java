package edu.eci.dosw.client;

import edu.eci.dosw.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TournamentClient {

    private final RestClient restClient;

    public TournamentClient(@Value("${external-services.tournament-url}") String tournamentUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(tournamentUrl)
                .build();
    }

    public boolean isTeamInActiveTournament(Long teamId, String authorizationHeader) {
        try {
            Boolean result = restClient.get()
                    .uri("/api/inscriptions/teams/{teamId}/active", teamId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(result);
        } catch (RestClientException ex) {
            throw new ExternalServiceException(
                    "Could not verify active tournament for team: " + teamId,
                    ex
            );
        }
    }
}
