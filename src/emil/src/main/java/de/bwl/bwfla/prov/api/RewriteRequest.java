package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RewriteRequest {

    @JsonProperty("rewriteURL")
    private String rewriteURL;

    public String getRewriteURL() {
        return rewriteURL;
    }

    public void setRewriteURL(String rewriteURL) {
        this.rewriteURL = rewriteURL;
    }
}
