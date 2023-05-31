package org.jembi.ciol.jsonMapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifierPacket(
        @JsonProperty("reportName") String reportName,
        @JsonProperty("attributeOptionComboId") String attributeOptionComboId,
        @JsonProperty("dataElements") MetadataConfigFileRecord.Reports.Dhis2Mapping.DataElements dataElements
    ) {
}
