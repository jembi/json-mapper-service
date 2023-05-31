package org.jembi.ciol.Test;

import org.junit.jupiter.api.Test;

import static org.jembi.ciol.Test.ValidateReportTest.*;
import static org.jembi.ciol.jsonMapper.ValidateFile.validateDisaggregations;
import static org.junit.jupiter.api.Assertions.*;
public class ValidateDisaggregatesTest {

    @Test
    void validateDisaggValid(){
        assert(validateDisaggregations(reportDataValid, sampleConfigFile)).isEmpty();
    }

    @Test
    void validateDisaggInvalidSingle() {
        String comp = """
                Not found:
                Number of old PLWHA on ARV who came for treatment in the month
                [Disaggregations[key=gender, index=0], Disaggregations[key=ageGroupInYears, index=10]]
                11
                """;
        assertEquals(comp, validateDisaggregations(reportDataInvalidDisaggSingle, sampleConfigFile));
    }

    @Test
    void validateDissaggInvalidMulti(){
        String comp = """
                Not found:
                Number of old PLWHA on ARV who came for treatment in the month
                [Disaggregations[key=gender, index=0], Disaggregations[key=ageGroupInYears, index=10]]
                11
                Not found:
                Number of old PLWHA on ARV who came for treatment in the month
                [Disaggregations[key=gender, index=3], Disaggregations[key=ageGroupInYears, index=0]]
                13
                Not found:
                Number of old PLWHA on ARV who came for treatment in the month
                [Disaggregations[key=gender, index=0], Disaggregations[key=ageGroupInYears, index=4]]
                17
                """;
        assertEquals(comp, validateDisaggregations(reportDataInvalidDisaggMulti, sampleConfigFile));
    }

    @Test
    void validateLarge(){
        assert(validateDisaggregations(reportDataFull, configFileFull)).isEmpty();
    }
}
