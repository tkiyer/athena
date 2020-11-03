package org.tkiyer.athena.engine.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Table implements Comparable<Table> {

    @JsonProperty
    private Schema schema;

    @JsonProperty
    private String name;

    @JsonProperty
    private String comment;

    @JsonProperty
    private List<Column> columns;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

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

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public int compareTo(Table o) {
        int i = this.schema.getCatalog().compareTo(o.schema.getCatalog());
        if (0 == i) {
            int j = this.schema.compareTo(o.schema);
            if (0 == j) {
                return this.name.compareTo(o.name);
            } else {
                return j;
            }
        } else {
            return i;
        }
    }
}
