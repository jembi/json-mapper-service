package org.jembi.ciol.shared.serdes;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface JSONSerdeCompatible {
}