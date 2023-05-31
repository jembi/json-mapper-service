package org.jembi.ciol.Test;

import org.junit.jupiter.api.Test;

import static org.jembi.ciol.Test.ValidateReportTest.*;
import static org.jembi.ciol.jsonMapper.ValidateFile.validateMeta;
import static org.junit.jupiter.api.Assertions.*;


public class ValidateMetaTest {
    @Test
    void validateMetaTestValid(){
        assert(validateMeta(reportDataValid, sampleConfigFile)) == null;
    }

    @Test
    void validateMetaTestBadInvalidMV(){
        assertEquals("Metadata Version: 43\n", validateMeta(reportDataInvalidMV, sampleConfigFile));
    }

    @Test
    void validateMetaTestBadInvalidOUI(){
        assertEquals("OrgUnitID:    \n", validateMeta(reportDataInvalidOUI, sampleConfigFile));
    }

    @Test
    void validateMetaTestBadInvalidPer(){
        assertEquals("Period: 2022--\n", validateMeta(reportDataInvalidPer, sampleConfigFile));
    }
}
