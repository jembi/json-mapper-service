package org.jembi.ciol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestConfig(@JsonProperty("AppID") String appID,
                         @JsonProperty("admin_emails") List<String> adminEmails,
                         @JsonProperty("smtp_config") SMTPConfig smtpConfig) {
    public record SMTPConfig(@JsonProperty("host") String host,
                              @JsonProperty("port") Integer port,
                              @JsonProperty("username") String username,
                              @JsonProperty("password") String password) {
    }
}