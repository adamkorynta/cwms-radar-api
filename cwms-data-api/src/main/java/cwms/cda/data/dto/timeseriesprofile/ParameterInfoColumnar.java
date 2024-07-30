package cwms.cda.data.dto.timeseriesprofile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import cwms.cda.data.dto.CwmsDTOValidator;


@JsonDeserialize(builder = ParameterInfoColumnar.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public final class ParameterInfoColumnar extends ParameterInfo {
    private final Integer startColumn;
    private final Integer endColumn;

    ParameterInfoColumnar(ParameterInfoColumnar.Builder builder) {
        super(builder);
        startColumn = builder.startColumn;
        endColumn = builder.endColumn;
    }

    public Integer getStartColumn(){
        return startColumn;
    }
    public Integer getEndColumn(){
        return endColumn;
    }
    @Override
    protected void validateInternal(CwmsDTOValidator validator) {
        validator.required(startColumn, "startColumn");
        validator.required(endColumn, "endColumn");
    }

    public String parameterInfoString()
    {
        return getParameter() +
                "," +
                getUnit() +
                "," +
                "," +
                getStartColumn() +
                "," +
                getEndColumn();
    }

    @JsonPOJOBuilder
    @JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
    public static final class Builder extends ParameterInfo.Builder{
       private Integer startColumn;
        private Integer endColumn;

       public ParameterInfoColumnar.Builder withStartColumn(int startColumn){
            this.startColumn = startColumn;
            return this;
        }
        public ParameterInfoColumnar.Builder withEndColumn(int endColumn){
            this.endColumn = endColumn;
            return this;
        }
        public ParameterInfo build() {
            return new ParameterInfoColumnar(this);
        }

    }
}