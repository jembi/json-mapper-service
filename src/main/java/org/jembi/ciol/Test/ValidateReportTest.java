package org.jembi.ciol.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jembi.ciol.jsonMapper.MetadataConfigFileRecord;
import org.jembi.ciol.models.GlobalConstants;
import org.jembi.ciol.models.InputReportData;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public final class ValidateReportTest {

    public static final MetadataConfigFileRecord sampleConfigFile = loadSampleConfig(GlobalConstants.SAMPLE_METADATA_CONFIG_FILE);
    public static final InputReportData reportDataValid = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_VALID);
    public static final InputReportData reportDataInvalidMV = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_INVALID_MV);
    public static final InputReportData reportDataInvalidOUI = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_INVALID_OUI);
    public static final InputReportData reportDataInvalidPer = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_INVALID_PER);

    public static final InputReportData reportDataInvalidDisaggSingle = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_INVALID_DISAGG_SINGLE);

    public static final InputReportData reportDataInvalidDisaggMulti = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_INVALID_DISAGG_MULTI);

    public static final InputReportData reportDataFull = loadSampleReport(GlobalConstants.SAMPLE_REPORT_DATA_FULL);

    public static final MetadataConfigFileRecord configFileFull = loadSampleConfig(GlobalConstants.SAMPLE_CONFIG_FILE_FULL);

    public static MetadataConfigFileRecord loadSampleConfig(String filePath){

        StringBuilder configString = null;
        MetadataConfigFileRecord metadataConfigFile = null;
        try{
            BufferedReader reader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            configString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                configString.append(line);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            metadataConfigFile = objectMapper.readValue(configString.toString(), MetadataConfigFileRecord.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return metadataConfigFile;
    }

    public static InputReportData loadSampleReport(String filePath){
        StringBuilder reportString = null;
        InputReportData reportFile = null;
        try{
            BufferedReader reader = new BufferedReader(
                    new FileReader(filePath));
            String line;
            reportString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                reportString.append(line);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            reportFile = objectMapper.readValue(reportString.toString(), InputReportData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reportFile;
    }

    @Test
    void validateConfigMappingError(){
        //TODO work on this
    }

    @Test
    void validateReportMappingError(){
        //TODO work on this
    }
}
