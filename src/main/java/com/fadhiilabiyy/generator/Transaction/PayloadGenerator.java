package com.fadhiilabiyy.generator.Transaction;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.fadhiilabiyy.generator.GeneralComponent.Service.JSONSchemeHelper.getFieldOrDefault;
import static com.fadhiilabiyy.generator.GeneratorApplication.om;
import static com.fadhiilabiyy.generator.Transaction.PayloadData.*;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
public class PayloadGenerator {
    private static final Random RANDOM = new Random();
    private static final String SECRET_KEY = "1Jq7bcbgVjeSKCQ5C2jlV8rPaYpp3Iyv";
    private static final String SIGNATURE_FORMAT = "POST:/analysis/online:%s:%s:";

    @GetMapping
    public ResponseEntity<?> generator(
            @RequestParam(value = "token", defaultValue = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJTb2x2ZSBFYXNlIEZyYXVkIEludmVzdGlnYXRpb24gU3lzdGVtIiwiaXNzIjoiUFQgRGVzYWNvZGUiLCJzdWIiOiJBZG1pbiIsImV4cCI6MTc0MzM1NTYxMn0.Qq6IHrmOXF9zt_l2I4IW-e_5gahJZcN_Jp15f9Is0eLUsVBq3Ae8AHNK8ZBajMwm464Ig71yIXatGZCX3qMIDQ") String token,
            @RequestParam(value = "transactionCode", defaultValue = "60") String transactionCode) throws NoSuchAlgorithmException, InvalidKeyException {
        ObjectNode responsePayload = om.createObjectNode();

        assignRandomValues(responsePayload, "HPAN", hpan);
        responsePayload.set("processingCode", createProcessingCode(transactionCode));
        responsePayload.put("transactionAmount", generateRandomNumber());
        assignRandomValues(responsePayload, "STAN", STAN);
        assignRandomValues(responsePayload, "localDate", localDate);
        assignRandomValues(responsePayload, "merchantType", merchantType);
        assignRandomValues(responsePayload, "cardExpDate", cardExpDate);
        assignRandomValues(responsePayload, "acqCountryCode", countryCode);
        assignRandomValues(responsePayload, "posDataCode", countryCode);
        assignRandomValues(responsePayload, "acqInstCode", countryCode);
        assignRandomValues(responsePayload, "forwardAcqInstCode", countryCode);
        assignRandomValues(responsePayload, "RRN", hpan);
        responsePayload.set("terminalData", createTerminalData());
        responsePayload.set("additionalData", createAdditionalData(transactionCode));
        assignRandomValues(responsePayload, "currencyCode", countryCode);
        responsePayload.set("addtAmount", createAddtAmount(responsePayload));
        responsePayload.set("fraudData", createFraudData());
        responsePayload.set("sourceAccount", createSourceAccount());
        responsePayload.set("destAccount", createDestAccount());
        assignRandomValues(responsePayload, "invoiceNumber", invoiceNumber);
        assignRandomValues(responsePayload, "isCrossBorder", new ArrayList<>(Arrays.asList("false", "true")));

        ObjectNode finalResponse = om.createObjectNode();
        finalResponse.set("payload", responsePayload);
        String signature = generateSignature(responsePayload, token);
        finalResponse.put("signature", signature);

        ObjectNode testing = om.createObjectNode();
        testing.put("signatureToken", getFieldOrDefault(finalResponse, "signature", ""));
        testing.put("HPAN", getFieldOrDefault(responsePayload, "HPAN", ""));
        testing.put("transactionAmount", getFieldOrDefault(responsePayload, "transactionAmount", ""));
        testing.put("merchantType", getFieldOrDefault(responsePayload, "merchantType", ""));
        testing.put("terminalId", getFieldOrDefault(responsePayload, "terminalData.terminalId", ""));
        testing.put("merchantID", getFieldOrDefault(responsePayload, "terminalData.merchantID", ""));
        testing.put("sourceAccNumber", getFieldOrDefault(responsePayload, "sourceAccount.accNumber", ""));
        testing.put("destAccNumber", getFieldOrDefault(responsePayload, "destAccount.accNumber", ""));
        testing.put("invoiceNumber", getFieldOrDefault(responsePayload, "invoiceNumber", ""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Signature", signature);
        headers.set("Authorization", token);
        headers.set("X-Timestamp", "");

        HttpEntity<ObjectNode> requestEntity = new HttpEntity<>(responsePayload, headers);

        RestTemplate restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();

        // Call `http://localhost:4545/analysis/online` with responsePayload as body and signature in headers
        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                "http://localhost:4545/analysis/online",
                HttpMethod.POST,
                requestEntity,
                ObjectNode.class
        );

        log.info("Response from analysis/online: {}", response.getBody().toPrettyString());

        return response;
    }

    @GetMapping("/csv")
    public ResponseEntity<?> generateCsvFile(
            @RequestParam(value = "totalData", defaultValue = "1") long totalData,
            @RequestParam(value = "token", defaultValue = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJTb2x2ZSBFYXNlIEZyYXVkIEludmVzdGlnYXRpb24gU3lzdGVtIiwiaXNzIjoiUFQgRGVzYWNvZGUiLCJzdWIiOiJBZG1pbiIsImV4cCI6MTc0MzM1NTYxMn0.Qq6IHrmOXF9zt_l2I4IW-e_5gahJZcN_Jp15f9Is0eLUsVBq3Ae8AHNK8ZBajMwm464Ig71yIXatGZCX3qMIDQ") String token,
            @RequestParam(value = "transactionCode", defaultValue = "60") String transactionCode) {
        String fileName = "payload_" + totalData + ".csv";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)){
            this.generateCsv(writer, totalData, token, transactionCode);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return new ResponseEntity<>(baos.toByteArray(), headers, OK);
    }

    private void generateCsv(PrintWriter writer, long totalData, String token, String transactionCode) {
        try {
            writer.println("HPAN,transactionCode,accountType1,accountType2,transactionAmount,STAN,localDate,merchantType,cardExpDate,acqCountryCode,posDataCode,acqInstCode,forwardAcqInstCode,RRN,terminalId,merchantID,name,street,city,state,country,postalCode,transactionType,cvvIndicator,terminalType,cardStatus,chipCondCode,securityLvlIndicator,ecAuthIndicator,authSource,addrVerification,pinAuthFlag,cvvAuthFlag,cvv2AuthFlag,cardAuthFlag,authFlag3ds,trackCardFlag,currencyCode,availableBalance,clientIpAddr,latitude,longitude,issInstsCode,accNumber,destAccNumber,destInstsCode,invoiceNumber,isCrossBorder,token");
            for (int i = 1; i <= totalData; i++ ) {
                ObjectNode responsePayload = om.createObjectNode();

                assignRandomValues(responsePayload, "HPAN", hpan);
                responsePayload.set("processingCode", createProcessingCode(transactionCode));
                responsePayload.put("transactionAmount", generateRandomNumber());
                assignRandomValues(responsePayload, "STAN", STAN);
                assignRandomValues(responsePayload, "localDate", localDate);
                assignRandomValues(responsePayload, "merchantType", merchantType);
                assignRandomValues(responsePayload, "cardExpDate", cardExpDate);
                assignRandomValues(responsePayload, "acqCountryCode", countryCode);
                assignRandomValues(responsePayload, "posDataCode", countryCode);
                assignRandomValues(responsePayload, "acqInstCode", countryCode);
                assignRandomValues(responsePayload, "forwardAcqInstCode", countryCode);
                assignRandomValues(responsePayload, "RRN", hpan);
                responsePayload.set("terminalData", createTerminalData());
                responsePayload.set("additionalData", createAdditionalData(transactionCode));
                assignRandomValues(responsePayload, "currencyCode", countryCode);
                responsePayload.set("addtAmount", createAddtAmount(responsePayload));
                responsePayload.set("fraudData", createFraudData());
                responsePayload.set("sourceAccount", createSourceAccount());
                responsePayload.set("destAccount", createDestAccount());
                assignRandomValues(responsePayload, "invoiceNumber", invoiceNumber);
                assignRandomValues(responsePayload, "isCrossBorder", new ArrayList<>(Arrays.asList("false", "true")));

                log.info("TESTING : {}", responsePayload.toPrettyString());

                String signature = generateSignature(responsePayload, token);

                String hpan = getFieldOrDefault(responsePayload, "HPAN", "");
                String transactionCodeValue = getFieldOrDefault(responsePayload, "processingCode.transactionCode", "");
                String accountType1 = getFieldOrDefault(responsePayload, "processingCode.accountType1", "");
                String accountType2 = getFieldOrDefault(responsePayload, "processingCode.accountType2", "");
                long transactionAmount = getFieldOrDefault(responsePayload, "transactionAmount", 1L);
                String stan = getFieldOrDefault(responsePayload, "STAN", "");
                String localDate = getFieldOrDefault(responsePayload, "localDate", "");
                String merchantType = getFieldOrDefault(responsePayload, "merchantType", "");
                String cardExpDate = getFieldOrDefault(responsePayload, "cardExpDate", "");
                String acqCountryCode = getFieldOrDefault(responsePayload, "acqCountryCode", "");
                String posDataCode = getFieldOrDefault(responsePayload, "posDataCode", "");
                String acqInstCode = getFieldOrDefault(responsePayload, "acqInstCode", "");
                String forwardAcqInstCode = getFieldOrDefault(responsePayload, "forwardAcqInstCode", "");
                String rrn = getFieldOrDefault(responsePayload, "RRN", "");
                String terminalId = getFieldOrDefault(responsePayload, "terminalData.terminalId", "");
                String merchantID = getFieldOrDefault(responsePayload, "terminalData.merchantID", "");
                String name = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.name", "");
                String street = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.street", "");
                String city = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.city", "");
                String state = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.state", "");
                String country = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.country", "");
                String postalCode = getFieldOrDefault(responsePayload, "terminalData.terminalInfo.postalCode", "");

                String transactionType = getFieldOrDefault(responsePayload, "additionalData.transactionType", "");
                String cvvIndicator = getFieldOrDefault(responsePayload, "additionalData.cvvIndicator", "");
                String terminalType = getFieldOrDefault(responsePayload, "additionalData.terminalType", "");
                String cardStatus = getFieldOrDefault(responsePayload, "additionalData.cardStatus", "");
                String chipCondCode = getFieldOrDefault(responsePayload, "additionalData.chipCondCode", "");
                String securityLvlIndicator = getFieldOrDefault(responsePayload, "additionalData.securityLvlIndicator", "");
                String ecAuthIndicator = getFieldOrDefault(responsePayload, "additionalData.ecAuthIndicator", "");

                String authSource = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.authSource", "");
                String addrVerification = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.addrVerification", "");
                String pinAuthFlag = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.pinAuthFlag", "");
                String cvvAuthFlag = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.cvvAuthFlag", "");
                String cvv2AuthFlag = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.cvv2AuthFlag", "");
                String cardAuthFlag = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.cardAuthFlag", "");
                String authFlag3ds = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.3DSecAuthFlag", "");
                String trackCardFlag = getFieldOrDefault(responsePayload, "fraudData.addtionalAuthInfo.trackCardFlag", "");

                String currencyCode = getFieldOrDefault(responsePayload, "currencyCode", "");
                String availableBalance = getFieldOrDefault(responsePayload, "addtAmount.availableBalance", "");

                String clientIpAddr = getFieldOrDefault(responsePayload, "fraudData.clientIpAddr", "");
                String latitude = getFieldOrDefault(responsePayload, "fraudData.latitude", "");
                String longitude = getFieldOrDefault(responsePayload, "fraudData.longitude", "");

                String issInstsCode = getFieldOrDefault(responsePayload, "sourceAccount.issInstsCode", "");
                String accNumber = getFieldOrDefault(responsePayload, "sourceAccount.accNumber", "");
                String destAccNumber = getFieldOrDefault(responsePayload, "destAccount.accNumber", "");
                String destInstsCode = getFieldOrDefault(responsePayload, "destAccount.destInstsCode", "");
                String invoiceNumber = getFieldOrDefault(responsePayload, "invoiceNumber", "");
                String isCrossBorder = getFieldOrDefault(responsePayload, "isCrossBorder", "");

                writer.println(String.join(",", hpan, transactionCodeValue, accountType1, accountType2,
                        String.valueOf(transactionAmount), stan, localDate, merchantType, cardExpDate, acqCountryCode,
                        posDataCode, acqInstCode, forwardAcqInstCode, rrn, terminalId, merchantID, name, street, city,
                        state, country, postalCode, transactionType, cvvIndicator, terminalType, cardStatus, chipCondCode,
                        securityLvlIndicator, ecAuthIndicator, authSource, addrVerification, pinAuthFlag, cvvAuthFlag, cvv2AuthFlag, cardAuthFlag, authFlag3ds, trackCardFlag, currencyCode, availableBalance, clientIpAddr, latitude, longitude,
                        issInstsCode, accNumber, destAccNumber, destInstsCode, invoiceNumber, isCrossBorder, signature));
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode createProcessingCode(String transactionCode) {
        ObjectNode processingCode = om.createObjectNode();
        processingCode.put("transactionCode", transactionCode);
        assignRandomValues(processingCode, "accountType1", accountType);
        assignRandomValues(processingCode, "accountType2", accountType);
        return processingCode;
    }

    private ObjectNode createTerminalData() {
        ObjectNode terminalInfo = om.createObjectNode();
        assignRandomValues(terminalInfo, "name", merchantName);
        assignRandomValues(terminalInfo, "street", street);
        assignRandomValues(terminalInfo, "city", cities);
        assignRandomValues(terminalInfo, "state", provinces);
        terminalInfo.put("country", "Indonesia");
        assignRandomValues(terminalInfo, "postalCode", postalCode);

        ObjectNode terminalData = om.createObjectNode();
        assignRandomValues(terminalData, "terminalId", terminalId);
        assignRandomValues(terminalData, "merchantID", merchantId);
        terminalData.set("terminalInfo", terminalInfo);

        return terminalData;
    }

    private ObjectNode createAdditionalData(String transactionCode) {
        ObjectNode additionalData = om.createObjectNode();

        switch (transactionCode) {
            case "00" -> assignRandomValues(additionalData, "transactionType", transactionCode_00);
            case "01" -> assignRandomValues(additionalData, "transactionType", transactionCode_01);
            case "10" -> assignRandomValues(additionalData, "transactionType", transactionCode_10);
            case "18" -> assignRandomValues(additionalData, "transactionType", transactionCode_18);
            case "20" -> assignRandomValues(additionalData, "transactionType", transactionCode_20);
            case "31" -> assignRandomValues(additionalData, "transactionType", transactionCode_31);
            case "40" -> assignRandomValues(additionalData, "transactionType", transactionCode_40);
            case "50" -> assignRandomValues(additionalData, "transactionType", transactionCode_50);
            case "60" -> assignRandomValues(additionalData, "transactionType", transactionCode_60);
            case "70" -> assignRandomValues(additionalData, "transactionType", transactionCode_70);
        }
        assignRandomValues(additionalData, "cvvIndicator", cvvIndicator);
        assignRandomValues(additionalData, "terminalType", terminalType);
        assignRandomValues(additionalData, "cardStatus", cardStatus);
        assignRandomValues(additionalData, "chipCondCode", chipCondCode);
        assignRandomValues(additionalData, "securityLvlIndicator", securityLvlIndicator);
        assignRandomValues(additionalData, "ecAuthIndicator", ecAuthIndicator);
        return additionalData;
    }

    private ObjectNode additionalAuthInfo() {
        ObjectNode additionalAuthInfo = om.createObjectNode();
        assignRandomValues(additionalAuthInfo, "authSource", authSource);
        assignRandomValues(additionalAuthInfo, "addrVerification", addrVerification);
        assignRandomValues(additionalAuthInfo, "pinAuthFlag", pinAuthFlag);
        assignRandomValues(additionalAuthInfo, "cvvAuthFlag", cvvAuthFlag);
        assignRandomValues(additionalAuthInfo, "cvv2AuthFlag", cvv2AuthFlag);
        assignRandomValues(additionalAuthInfo, "cardAuthFlag", cardAuthFlag);
        assignRandomValues(additionalAuthInfo, "3DSecAuthFlag", authFlag3ds);
        assignRandomValues(additionalAuthInfo, "trackCardFlag", trackCardFlag);
        return additionalAuthInfo;
    }

    private ObjectNode createAddtAmount(ObjectNode payload) {
        ObjectNode addtAmount = om.createObjectNode();
        addtAmount.put("availableBalance", "0");
        addtAmount.put("currencyCode", getFieldOrDefault(payload, "currencyCode", ""));
        return addtAmount;
    }

    private ObjectNode createFraudData() {
        ObjectNode fraudData = om.createObjectNode();
        fraudData.set("addtionalAuthInfo", additionalAuthInfo());
        assignRandomValues(fraudData, "clientIpAddr", clientIpAddress);
        assignRandomValues(fraudData, "latitude", latitudes);
        assignRandomValues(fraudData, "longitude", longitudes);
        return fraudData;
    }

    private ObjectNode createSourceAccount() {
        ObjectNode sourceAccount = om.createObjectNode();
        assignRandomValues(sourceAccount, "issInstsCode", countryCode);
        assignRandomValues(sourceAccount, "accNumber", srcAccNumber);
        return sourceAccount;
    }

    private ObjectNode createDestAccount() {
        ObjectNode destAccount = om.createObjectNode();
        assignRandomValues(destAccount, "accNumber", destAccNumber);
        assignRandomValues(destAccount, "destInstsCode", countryCode);
        return destAccount;
    }

    private String generateRandomNumber() {
        return String.valueOf(10_000L + (long) (RANDOM.nextDouble() * 90_000L));
    }

    private void assignRandomValues(ObjectNode payload, String fieldName, List<String> values) {
        if (!values.isEmpty()) {
            payload.put(fieldName, values.get(RANDOM.nextInt(values.size())));
        }
    }

    private String generateSignature(ObjectNode respMessage, String token) throws NoSuchAlgorithmException, InvalidKeyException {
        String signatureData = String.format(SIGNATURE_FORMAT, token, respMessage);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        log.info("{}", signatureData);
        return signDataWithHmacSHA512(signatureData, secretKey);
    }

    private String signDataWithHmacSHA512(String data, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(key);
        return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
