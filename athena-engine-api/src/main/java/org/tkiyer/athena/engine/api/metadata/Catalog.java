package org.tkiyer.athena.engine.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Catalog implements Comparable<Catalog> {

    @JsonProperty
    private String name;

    @JsonProperty
    private String comment;

    @JsonProperty
    private String engine;

    @JsonIgnore
    private List<Schema> schemas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }

    @Override
    public int compareTo(Catalog o) {
        int i = this.engine.compareTo(o.engine);
        if (i == 0) {
            return this.name.compareTo(o.name);
        }
        return i;
    }
}
