package cwms.cda.data.dto.timeseriesprofile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import cwms.cda.data.dto.CwmsDTOValidator;
import cwms.cda.formatters.Formats;
import cwms.cda.formatters.annotations.FormattableWith;
import cwms.cda.formatters.json.JsonV2;

import java.math.BigInteger;

@FormattableWith(contentType = Formats.JSONV2, formatter = JsonV2.class)
@JsonDeserialize(builder = TimeSeriesProfileParserIndexed.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public  class TimeSeriesProfileParserIndexed extends TimeSeriesProfileParser {
    private final Character fieldDelimiter;
    private final Integer timeField;

    TimeSeriesProfileParserIndexed(Builder builder) {
        super(builder);
        fieldDelimiter = builder.fieldDelimiter;
        timeField = builder.timeField;
    }

    @Override
    protected void validateInternal(CwmsDTOValidator validator) {
        validator.required(getFieldDelimiter(),"fieldDelimiter");
        validator.required(getTimeField(),"timeField");
     }


    public Character getFieldDelimiter() {
        return fieldDelimiter;
    }


    public BigInteger getTimeField() {
        return timeField==null ? null : BigInteger.valueOf(timeField);
    }

    @JsonPOJOBuilder
    @JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
    public static class Builder extends TimeSeriesProfileParser.Builder{
        private Character fieldDelimiter = null;
        private Integer timeField = null;


        public TimeSeriesProfileParserIndexed.Builder withFieldDelimiter(char delimiter) {
            this.fieldDelimiter = delimiter;
            return this;
        }


        public TimeSeriesProfileParserIndexed.Builder withTimeField(int field) {
            this.timeField = field;
            return this;
        }
        @Override
        public TimeSeriesProfileParserIndexed build() {
            return new TimeSeriesProfileParserIndexed(this);
        }
    }

}
