package org.jembi.ciol.jsonMapper;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.models.InputReportData;
import org.jembi.ciol.models.NotificationMessage;
import org.jembi.ciol.models.Payload;
import org.jembi.ciol.models.Payload.DataValues;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.jembi.ciol.jsonMapper.JsonMapper.sendNotification;
import static org.jembi.ciol.jsonMapper.JsonMapper.sendToPayload;
import static org.jembi.ciol.jsonMapper.LoadFile.getConfigFile;
import static org.jembi.ciol.jsonMapper.LoadFile.getReportData;

public class ValidateFile {

    private static final Logger LOGGER = LogManager.getLogger(ValidateFile.class.getName());

    public static @Nullable HttpResponse validateFile(String jsonString) {

        LOGGER.debug("File has arrived for validation");

        InputReportData reportData = getReportData(jsonString);
        MetadataConfigFileRecord configFile = getConfigFile();

        if (reportData.dataElements() == null) {
            LOGGER.error("Report Data is empty: {}", reportData);
            return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
                    .withEntity("Report data NULL");
        } else if (configFile.reports() == null) {
            LOGGER.error("Config file is empty: {}", configFile);
            return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
                    .withEntity("Metadata Config NULL");
        }

        String metaReason = validateMeta(reportData, configFile);
        if (metaReason != null) {
            return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
                    .withEntity(metaReason);
        }
        LOGGER.debug("First round of validation is good");

        String disaggregationsReason = validateDisaggregations(reportData, configFile);
        if (!disaggregationsReason.equals("")){
            return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
                    .withEntity(disaggregationsReason);
        }

        return HttpResponse.create().withStatus(StatusCodes.OK);
    }

    public static String validateDisaggregations(InputReportData reportFile, MetadataConfigFileRecord configFile) {

        StringBuilder reason = new StringBuilder();
        ArrayList<VerifierPacket> packets = new ArrayList<>();

        for (MetadataConfigFileRecord.Reports report : configFile.reports()) {
            if (report.reportName().equals(reportFile.reportName())) {
                for (MetadataConfigFileRecord.Reports.Dhis2Mapping.DataElements dataElement : report.dhis2Mapping().dataElements()) {
                    packets.add(new VerifierPacket(
                            report.reportName(),
                            report.dhis2Mapping().attributeOptionComboId(),
                            dataElement)
                    );
                }
            }
        }

        ArrayList<Payload> payloadList = new ArrayList<>();

        short verifierCounter = 0;    // TODO do I want to make these shorts?
        short reportCounter = 0;

        List<NotificationMessage.Message> messages = new ArrayList<>();
        short countError = 0;

        for (InputReportData.DataElements dataElements : reportFile.dataElements()) {
            if (dataElements.values() == null){
                reason.append("DataElement values is null");
                break;
            } else if (dataElements.values().length == 0){
                reason.append("DataElement values length is zero");
                break;
            }
            for (InputReportData.DataElements.Values value : dataElements.values()) {
                reportCounter++;
                boolean nonePresent = true;
                for (VerifierPacket vp : packets) {
                    if (reportFile.reportName().equals(vp.reportName())) {
                        if (dataElements.dataElementName().equals(vp.dataElements().dataElementName())) {
                            if (compareArrays(value.disaggregations(), vp.dataElements().disaggregations())) {
                                verifierCounter++;
                                nonePresent = false;
                                // TODO move this to own function to make neater, maybe
                                List<DataValues> dataValues = new ArrayList<>();
                                dataValues.add(
                                        new Payload.DataValues(
                                                vp.dataElements().dataElementId(),
                                                vp.dataElements().categoryOptionComboId(),
                                                value.value().toString()));
                                payloadList.add(new Payload(
                                        vp.dataElements().dataSetId(),
                                        java.time.LocalDate.now().toString().replace("-", ""),
                                        reportFile.period(),
                                        reportFile.orgUnitId(),
                                        vp.attributeOptionComboId(),
                                        dataValues));
                                break;
                            }
                        }
                    }
                }
                if (nonePresent) {
                    LOGGER.error("Not found:\n {}\n {}\n{}", dataElements.dataElementName(), value.disaggregations(), value.value());
                    reason.append("Not found:\n").append(dataElements.dataElementName()).
                            append("\n").append(Arrays.toString(value.disaggregations())).
                            append("\n").append(value.value()).
                            append("\n");
                    // TODO move this to own function to make neater, maybe
                    messages.add(countError, new NotificationMessage.Message(
                            "admin",
                            "error",
                            System.currentTimeMillis(),
                            400,
                            "Validation round 2 failed",
                            reportFile.reportName() + " " +
                                    reportFile.metadataVersion() + " " +
                                    reportFile.orgUnitId() + " " +
                                    reportFile.period() + " " +
                                    dataElements.dataElementName() + " " +
                                    value,
                            ValidateFile.class.toString()));
                }
            }
        }

        if (verifierCounter == reportCounter && verifierCounter != 0) {
            LOGGER.info("Second round of validation is good");
            sendFile(payloadList);
        } else {
            sendToNotificationPayload(messages);
        }
        return reason.toString();
    }

    public static String validateMeta(InputReportData reportFile, MetadataConfigFileRecord configFile) {
        assert configFile != null;
        String reason = "";
        boolean metaDataVersionCheck = validateMetadataVersion(reportFile.metadataVersion(), configFile.metadata().version());
        boolean periodCheck = validatePeriod(reportFile.period());
        boolean orgUnitIdCheck = validateOrgUnitId(reportFile.orgUnitId());
        if (!metaDataVersionCheck ||
                !periodCheck ||
                !orgUnitIdCheck) {
            if (!metaDataVersionCheck) {
                reason += "Metadata Version: " + reportFile.metadataVersion() + "\n";
                LOGGER.error("Metadata validation failed, Metadata Version: {}", reportFile.metadataVersion());
            }
            if (!periodCheck) {
                reason += "Period: " + reportFile.period() + "\n";
                LOGGER.error("Metadata validation failed, Period: {}", reportFile.period());
            }
            if (!orgUnitIdCheck) {
                reason += "OrgUnitID: " + reportFile.orgUnitId() + "\n";
                LOGGER.error("Metadata validation failed, OrgUnitID: {}", reportFile.orgUnitId());
            }
            List<NotificationMessage.Message> messages = new ArrayList<NotificationMessage.Message>();
            messages.add(0, new NotificationMessage.Message(
                    "Admin",
                    "error",
                    System.currentTimeMillis(),
                    400,
                    "Validation round 1 failed",
                    reportFile.reportName() + " " +
                            reportFile.metadataVersion() + " " +
                            reportFile.orgUnitId() + " " +
                            reportFile.period(),
                    ValidateFile.class.toString()));

            sendToNotificationPayload(messages);

            return reason;
        }
        return null;
    }

    public static boolean validatePeriod(String period) {

        if (period.isBlank()) {
            return false;
        }
        else if (period.length() != 6) {
            return false;
        }
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e){
            return false;
        }

        return Integer.parseInt(period.substring(4, 6)) >= 1 && Integer.parseInt(period.substring(4, 6)) <= 12;
    }

    public static boolean validateMetadataVersion(String reportMV, String configMV){
        if (reportMV == null || configMV == null){
            LOGGER.error("Null value assigned to metadata version number");
            return false;
        }
        if (reportMV.isEmpty() || configMV.isEmpty()){
            LOGGER.error("Empty value assigned to metadata version number");
            return false;
        } else if (reportMV.isBlank() || configMV.isBlank()){
            LOGGER.error("Blank value assigned to metadata version number");
            return false;
        }
        return reportMV.equals(configMV);
    }

    public static boolean validateOrgUnitId(String reportFile){
        if (reportFile == null){
            LOGGER.error("Null value assigned to Report File");
            return false;
        } else if (reportFile.isBlank()){
            LOGGER.error("Blank value assigned to Report File");
            return false;
        } else if (reportFile.isEmpty()){
            LOGGER.error("Empty value assigned to Report File");
            return false;
        }
        return true;
    }

    private static void sendToNotificationPayload(List<NotificationMessage.Message> messages){
        try {
            sendNotification(new NotificationMessage(
                    System.currentTimeMillis(),
                    "source",
                    messages));
        }
        catch (NullPointerException e){
            LOGGER.error("{}", e.getMessage());
        }
    }

    private static void sendFile(ArrayList<Payload> payloadList) {
        try {
            for (Payload p : payloadList) {
                sendToPayload(p);
            }
            List<NotificationMessage.Message> messages = new ArrayList<NotificationMessage.Message>();
            messages.add(0, new NotificationMessage.Message(
                    "Admin",
                    "info",
                    System.currentTimeMillis(),
                    200,
                    "Successful",
                    "Report data pushed to Payload topic",
                    ValidateFile.class.toString()
            ));
            sendNotification(new NotificationMessage(
                    System.currentTimeMillis(),
                    "source",
                    messages
            ));
        } catch (Exception e) {
            LOGGER.error("Kafka server broken {}, {}", e.getMessage(), e);
        }
    }

    private static boolean compareArrays(InputReportData.DataElements.Values.Disaggregations[] reportArray,
                                      InputReportData.DataElements.Values.Disaggregations[] configArray){
        byte count = 0;
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 2; j++){
                if (Objects.equals(reportArray[i].key(), configArray[j].key()) &&
                        Objects.equals(reportArray[i].index(), configArray[j].index())){
                    count++;
                }
            }
        }
    return count == 2;
    }
}