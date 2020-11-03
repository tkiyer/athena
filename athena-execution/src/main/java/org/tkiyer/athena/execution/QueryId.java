package org.tkiyer.athena.execution;

import java.util.Objects;

public class QueryId implements Comparable<QueryId> {

    public static QueryId of(String queryId) {
        return new QueryId(queryId);
    }

    private final String id;

    protected QueryId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QueryId)) {
            return false;
        }
        QueryId other = (QueryId) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int compareTo(QueryId o) {
        return this.id.compareTo(o.getId());
    }
}
