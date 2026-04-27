package edu.eci.dosw.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountAccessPolicy {
    public boolean canReadAccount(Long requestedAccountId, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()){
            return false;
        }
        Long currentAccountId = Long.valueOf(authentication.getPrincipal().toString());
        return requestedAccountId.equals(currentAccountId);
    }
}
