package org.tkiyer.athena.engine.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryMetadata {

    @JsonProperty
    private String queryString;

    @JsonProperty
    private String catalog;

    @JsonProperty
    private String engineName;

    @JsonProperty
    private String groupId;

    @JsonProperty
    private String clientId;

    @JsonProperty
    private Map<String, Object> queryConfig = new HashMap<>();

    @JsonProperty
    private String callbackUrl;

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, Object> getQueryConfig() {
        return queryConfig;
    }

    public void setQueryConfig(Map<String, Object> queryConfig) {
        this.queryConfig = queryConfig;
    }

    public void addQueryConfig(String key, Object val) {
        this.queryConfig.put(key, val);
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
}
