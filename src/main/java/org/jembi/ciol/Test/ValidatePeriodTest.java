package org.jembi.ciol.Test;

import org.junit.jupiter.api.Test;
import static org.jembi.ciol.jsonMapper.ValidateFile.validatePeriod;
import static org.junit.jupiter.api.Assertions.*;

class ValidatePeriodTest {

    @Test
    void validatePeriodEmptyTest(){
        assertFalse(validatePeriod(""));
    }

    @Test
    void validatePeriodLengthFalseShort(){
        assertFalse(validatePeriod("12345"));
    }

    @Test
    void validatePeriodLengthFalseLong(){
        assertFalse(validatePeriod("1234567"));
    }

    @Test
    void validatePeriodMonthFalse00(){
        assertFalse(validatePeriod("202200"));
    }

    @Test
    void validatePeriodMonthFalse13(){
        assertFalse(validatePeriod("202213"));
    }

    @Test
    void validatePeriodIsNumeric(){
        assertFalse(validatePeriod("20221."));
    }

    @Test
    void validatePeriodTrue(){
        assertTrue(validatePeriod("202206"));
    }
}