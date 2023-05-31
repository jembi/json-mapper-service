package org.jembi.ciol.Test;

import org.junit.jupiter.api.Test;

import static org.jembi.ciol.jsonMapper.ValidateFile.validateMetadataVersion;
import static org.junit.jupiter.api.Assertions.*;

public class ValidateMetadataVersionTest {

    @Test
    void validateMVTrue(){
        assertTrue(validateMetadataVersion("3", "3"));
    }

    @Test
    void validateMVisBlank(){
        assertFalse(validateMetadataVersion("  ", "  "));
    }

    @Test
    void validateMVisEmpty(){
        assertFalse(validateMetadataVersion("", ""));
    }

    @Test
    void validateMVNull(){
        assertFalse(validateMetadataVersion(null, null));
    }

    @Test
    void validateMVFalse(){
        assertFalse(validateMetadataVersion("1", "4"));
    }

}
