package org.tkiyer.athena.execution;

public interface QueryEventListener {

    void onStateChanged(QueryEvent event);
}
