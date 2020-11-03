package org.tkiyer.athena.engine.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Column implements Comparable<Column> {

    @JsonIgnore
    private Table table;

    @JsonProperty
    private String name;

    @JsonProperty
    private int dataType;

    @JsonProperty
    private String dataTypeName;

    @JsonProperty
    private String comment;

    @JsonProperty
    private int size;

    @JsonProperty
    private int decimalDigits;

    @JsonProperty
    private int position;

    @JsonProperty
    private boolean isPartition;

    @JsonProperty
    private boolean isNullable;

    public Column() {
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPartition() {
        return isPartition;
    }

    public void setPartition(boolean partition) {
        isPartition = partition;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    @Override
    public int compareTo(Column o) {
        int i = this.table.getSchema().getCatalog().compareTo(o.table.getSchema().getCatalog());
        if (0 == i) {
            int j = this.table.getSchema().compareTo(o.table.getSchema());
            if (0 == j) {
                int k = this.table.getName().compareTo(o.table.getName());
                if (0 == k) {
                    int m = this.position - o.position;
                    if (0 == m) {
                        return this.name.compareTo(o.name);
                    } else {
                        return m;
                    }
                } else {
                    return k;
                }
            } else {
                return j;
            }
        } else {
            return i;
        }
    }
}
