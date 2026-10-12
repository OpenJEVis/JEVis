package org.jevis.commons.ws.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Two-factor state of a user, also used as the response of the TOTP setup ({@code secret}/{@code otpAuthUri} are
 * only set there).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonMfaStatus {

    private Boolean enabled;
    private Boolean required;
    private Integer recoveryCodesLeft;
    private String secret;
    private String otpAuthUri;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getRecoveryCodesLeft() {
        return recoveryCodesLeft;
    }

    public void setRecoveryCodesLeft(Integer recoveryCodesLeft) {
        this.recoveryCodesLeft = recoveryCodesLeft;
    }

    /**
     * @return Base32 TOTP secret for manual entry in an authenticator app
     */
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return {@code otpauth://} URI, shown as QR code
     */
    public String getOtpAuthUri() {
        return otpAuthUri;
    }

    public void setOtpAuthUri(String otpAuthUri) {
        this.otpAuthUri = otpAuthUri;
    }
}
