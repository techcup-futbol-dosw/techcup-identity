package edu.eci.dosw.client;

import edu.eci.dosw.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.Optional;

@Component
public class TeamClient {

    private final RestClient restClient;

    private static final Logger log = LoggerFactory.getLogger(TeamClient.class);

    public TeamClient(@Value("${external-services.teams-url}") String teamsUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(teamsUrl)
                .build();
        log.info("Teams base URL configured as: {}", teamsUrl);
    }


    public Optional<Long> findTeamIdByPlayerId(Long accountId, String authorizationHeader) {
        Integer playerId = toPlayerId(accountId);

        try {
            Long teamId = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams/search/player")
                            .queryParam("playerId", playerId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(Long.class);

            return Optional.ofNullable(teamId);

        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();

        } catch (HttpStatusCodeException ex) {
            throw new ExternalServiceException(
                    "Teams service responded with status "
                            + ex.getStatusCode().value()
                            + " while verifying team membership for account: "
                            + accountId
                            + ". Body: "
                            + ex.getResponseBodyAsString(),
                    ex
            );

        }catch (ResourceAccessException ex) {
                throw new ExternalServiceException(
                        "Could not connect to Teams service while verifying team membership for account: "
                                + accountId
                                + ". Cause: "
                                + ex.getMessage(),
                        ex
                );

        } catch (RestClientException ex) {
            throw new ExternalServiceException(
                    "Could not verify team membership for account: " + accountId,
                    ex
            );
        }
    }

    private Integer toPlayerId(Long accountId) {
        try {
            return Math.toIntExact(accountId);
        } catch (ArithmeticException ex) {
            throw new ExternalServiceException(
                    "Account id is too large to be sent as playerId: " + accountId,
                    ex
            );
        }
    }
}
