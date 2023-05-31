package org.jembi.ciol.jsonMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetadataConfigURL(
        @JsonProperty("metadata_config_url") String metadataURL
    ) {}