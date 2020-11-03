package org.tkiyer.athena.engine.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema implements Comparable<Schema> {

    @JsonProperty
    private Catalog catalog;

    @JsonProperty
    private String name;

    @JsonProperty
    private String comment;

    @JsonIgnore
    private List<Table> tables;

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

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public int compareTo(Schema o) {
        int i = this.catalog.compareTo(o.catalog);
        if (i == 0) {
            return this.name.compareTo(o.name);
        } else {
            return i;
        }
    }
}
