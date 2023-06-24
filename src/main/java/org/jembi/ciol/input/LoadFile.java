package org.jembi.ciol.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.models.GlobalConstants;
import org.jembi.ciol.models.InputReportData;

import java.io.*;
import java.net.URL;

public class LoadFile {

    private static final Logger LOGGER = LogManager.getLogger(LoadFile.class.getName());

    public static MetadataConfigFileRecord getConfigFile() {

        MetadataConfigURL configURL = null;
        StringBuilder configString = null;

        try{
            BufferedReader reader = new BufferedReader(
                    new FileReader(GlobalConstants.METADATA_FILE_PATH));
            String line;
            configString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                configString.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            assert configURL != null;
            configURL = objectMapper.readValue(configString.toString(), MetadataConfigURL.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder jsonString = null;
        try {
            URL url = new URL(configURL.metadataURL());
            LOGGER.debug("{}", configURL.metadataURL());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String line;
            jsonString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            LOGGER.debug("{}", jsonString);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        MetadataConfigFileRecord metadataConfigFile = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            assert jsonString != null;
            metadataConfigFile = objectMapper.readValue(jsonString.toString(), MetadataConfigFileRecord.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.debug("Config file record: {}", metadataConfigFile);
        return metadataConfigFile;
    }

    public static InputReportData getReportData(String jsonString) {

       InputReportData reportData = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            assert jsonString != null;
            reportData = objectMapper.readValue(jsonString.toString(), InputReportData.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.debug("Report data file: {}", reportData);
        return reportData;
    }
}