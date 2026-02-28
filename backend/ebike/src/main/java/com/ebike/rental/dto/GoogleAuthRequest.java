package com.ebike.rental.dto;

public class GoogleAuthRequest {
    private String idToken;
    private String code;
    private String redirectUri;

    public GoogleAuthRequest() {}

    public GoogleAuthRequest(String idToken, String code, String redirectUri) {
        this.idToken = idToken;
        this.code = code;
        this.redirectUri = redirectUri;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
