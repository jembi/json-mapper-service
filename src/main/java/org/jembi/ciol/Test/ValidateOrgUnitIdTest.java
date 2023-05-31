package org.jembi.ciol.Test;

import org.junit.jupiter.api.Test;
import static org.jembi.ciol.jsonMapper.ValidateFile.validateOrgUnitId;
import static org.junit.jupiter.api.Assertions.*;

public class ValidateOrgUnitIdTest {

    @Test
    void validateOUITrue(){
        assertTrue(validateOrgUnitId("Literally anything at this point"));
    }

    @Test
    void validateOUIBlank(){
        assertFalse(validateOrgUnitId(""));
    }

    @Test
    void validateOUIEmpty(){
        assertFalse(validateOrgUnitId("   "));
    }

    @Test
    void validateOUINUll(){
        assertFalse(validateOrgUnitId(null));
    }
}
