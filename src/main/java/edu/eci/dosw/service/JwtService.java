package edu.eci.dosw.service;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

@Service
public class JwtService {




    public String extractUserId(String token){
        return "";
    }
    public String extractEmail(String token){
        return "";
    }
    public List<String> extractRoles(String token){
        return new ArrayList<>();
    }

    public List<String> extractPermissions(String token){
        return new ArrayList<>();
    }
    public boolean isTokenValid(String token){
        return true;
    }
}
