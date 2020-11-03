package org.tkiyer.athena.execution;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryDurationTime {

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date begin;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date end;

    public void begin() {
        if (null == this.begin) {
            this.begin = new Date();
        }
    }

    public void end() {
        if (null == this.end) {
            this.end = new Date();
        }
    }

    @JsonGetter
    public Date getBegin() {
        return begin;
    }

    @JsonGetter
    public Date getEnd() {
        return end;
    }

    @JsonGetter
    public long getDuration() {
        if (null == this.begin) {
            return 0L;
        }
        if (null == this.end) {
            return System.currentTimeMillis() - this.begin.getTime();
        }
        return this.end.getTime() - this.begin.getTime();
    }
}
