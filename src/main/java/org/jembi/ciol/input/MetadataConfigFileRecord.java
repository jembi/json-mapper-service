package org.jembi.ciol.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.ciol.models.InputReportData;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetadataConfigFileRecord(
        @JsonProperty("metadata") Metadata metadata,
        @JsonProperty("reports") Reports[] reports) {

    public record Metadata(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("version") String version,
            @JsonProperty("creationDate") String creationDate,
            @JsonProperty("category") String category) {
    }

    public record Reports(
            @JsonProperty("reportName") String reportName,
            @JsonProperty("parameters") Parameters[] parameters,
            @JsonProperty("dataElements") DataElements[] dataElements,
            @JsonProperty("dhis2Mapping") Dhis2Mapping dhis2Mapping) {

        public record Parameters(
                @JsonProperty("key") String key,
                @JsonProperty("options") String[] options,
                @JsonProperty("ranges") Ranges[] ranges) {

            public record Ranges(
                    @JsonProperty("min") Integer min,
                    @JsonProperty("max") Integer max) {
            }
        }

        public record DataElements(
                @JsonProperty("dataElementName") String dataElementName,
                @JsonProperty("query") String query) {
        }

        public record Dhis2Mapping(
                @JsonProperty("attributeOptionComboId") String attributeOptionComboId,
                @JsonProperty("dataElements") DataElements[] dataElements) {

            public record DataElements(
                    @JsonProperty("dataElementName") String dataElementName,
                    @JsonProperty("disaggregations") InputReportData.DataElements.Values.Disaggregations[] disaggregations,
                    @JsonProperty("dataSetId") String dataSetId,
                    @JsonProperty("dataElementId") String dataElementId,
                    @JsonProperty("categoryOptionComboId") String categoryOptionComboId) {
            }
        }
    }
}