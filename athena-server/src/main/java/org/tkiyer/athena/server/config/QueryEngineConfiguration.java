package org.tkiyer.athena.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.tkiyer.athena.server.support.ArtifactResolver;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class QueryEngineConfiguration {

    @JsonProperty
    private String queryEnginesDir = "query-engines";

    @JsonProperty
    @NotNull
    private List<String> queryEngines;

    @JsonProperty
    private String mavenLocalRepository = ArtifactResolver.USER_LOCAL_REPO;

    @JsonProperty
    private List<String> mavenRemoteRepository = Collections.unmodifiableList(Collections.singletonList(ArtifactResolver.MAVEN_CENTRAL_URI));

    public File getQueryEngineDirectory() {
        return new File(this.queryEnginesDir);
    }

    public String getQueryEnginesDir() {
        return queryEnginesDir;
    }

    public void setQueryEnginesDir(String queryEnginesDir) {
        this.queryEnginesDir = queryEnginesDir;
    }

    public List<String> getQueryEngines() {
        return queryEngines;
    }

    public void setQueryEngines(List<String> queryEngines) {
        this.queryEngines = queryEngines;
    }

    public String getMavenLocalRepository() {
        return mavenLocalRepository;
    }

    public void setMavenLocalRepository(String mavenLocalRepository) {
        this.mavenLocalRepository = mavenLocalRepository;
    }

    public List<String> getMavenRemoteRepository() {
        return mavenRemoteRepository;
    }

    public void setMavenRemoteRepository(List<String> mavenRemoteRepository) {
        this.mavenRemoteRepository = mavenRemoteRepository;
    }
}
