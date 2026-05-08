package edu.eci.dosw.unitaria.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import edu.eci.dosw.security.AccountAccessPolicy;

import static org.junit.jupiter.api.Assertions.*;

class AccountAccessPolicyTest {

    private final AccountAccessPolicy policy = new AccountAccessPolicy();

    @Test
    void canReadAccount_ShouldReturnTrue_WhenAccountIdMatchesPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null, java.util.List.of());
        assertTrue(policy.canReadAccount(1L, auth));
    }

    @Test
    void canReadAccount_ShouldReturnFalse_WhenAccountIdDoesNotMatchPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("1", null);
        assertFalse(policy.canReadAccount(2L, auth));
    }

    @Test
    void canReadAccount_ShouldReturnFalse_WhenAuthenticationIsNull() {
        assertFalse(policy.canReadAccount(1L, null));
    }

    @Test
    void canReadAccount_ShouldReturnFalse_WhenAuthenticationIsNotAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);
        assertFalse(policy.canReadAccount(1L, auth));
    }
}