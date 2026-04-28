package edu.eci.dosw.model;

public class RefreshToken {

    private Long id;
    private String token;
    private Account account;
    private boolean revoked;

    public RefreshToken() {}

    public RefreshToken(Long id, String token, Account account, boolean revoked) {
        this.id = id;
        this.token = token;
        this.account = account;
        this.revoked = revoked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
