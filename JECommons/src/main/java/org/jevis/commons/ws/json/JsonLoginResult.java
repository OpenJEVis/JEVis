package org.jevis.commons.ws.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response of the {@code /JEWebService/v1/auth/*} login endpoints.
 *
 * <p>Depending on {@link #getStatus()} different fields are set:
 * <ul>
 *     <li>{@link Status#SUCCESS}: {@code accessToken}, {@code expiresIn} and, if requested, {@code refreshToken}</li>
 *     <li>{@link Status#MFA_REQUIRED}: {@code mfaToken}, send it with the code to {@code /auth/login/mfa}</li>
 *     <li>{@link Status#MFA_ENROLLMENT_REQUIRED}: {@code mfaToken}, only valid for {@code /auth/mfa/totp/*}</li>
 *     <li>{@link Status#FAILURE}: {@code message}</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonLoginResult {

    private Status status;
    private String accessToken;
    private String refreshToken;
    private String mfaToken;
    private Long expiresIn;
    private Long userId;
    private String userName;
    private Boolean mustChangePassword;
    private List<String> recoveryCodes;
    private String message;

    public JsonLoginResult() {
    }

    public static JsonLoginResult failure(String message) {
        JsonLoginResult result = new JsonLoginResult();
        result.setStatus(Status.FAILURE);
        result.setMessage(message);
        return result;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    /**
     * @return lifetime of the access token in seconds
     */
    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    /**
     * @return the new recovery codes, only set once after 2FA was activated
     */
    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "JsonLoginResult{status=" + status + ", userName='" + userName + "'}";
    }

    public enum Status {
        SUCCESS, MFA_REQUIRED, MFA_ENROLLMENT_REQUIRED, FAILURE
    }
}
