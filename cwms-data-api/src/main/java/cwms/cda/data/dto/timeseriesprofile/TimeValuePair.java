package cwms.cda.data.dto.timeseriesprofile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Instant;

@JsonDeserialize(builder = TimeValuePair.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public final class TimeValuePair {
    private final Instant dateTime;
    private final double value;

    private TimeValuePair(Builder builder)
    {
        dateTime = builder.dateTime;
        value = builder.value;
     }
    public Instant getDateTime()
    {
        return dateTime;
    }
    public double getValue()
    {
        return value;
    }
    @JsonPOJOBuilder
    @JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
    public static final class Builder {
        private Instant dateTime;
        private double value;

        public Builder withDateTime(Instant dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder withValue(double value) {
            this.value = value;
            return this;
        }

        public TimeValuePair build() {
            return new TimeValuePair(this);
        }
    }
}