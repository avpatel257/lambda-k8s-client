package com.two57.k8s.client;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookResponse;
import com.two57.k8s.client.utils.K8SUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.two57.k8s.client.utils.K8SUtils.getNamespaces;


public class APIDemoHandler implements RequestStreamHandler {

    public void handleRequest(
            InputStream inputStream,
            OutputStream outputStream,
            Context context)
            throws IOException {

        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        String responseText = null;

        try {
            JSONObject event = (JSONObject) parser.parse(reader);

            if (event.get("body") != null) {
                System.out.println("== Request Body======================");
                System.out.println((String) event.get("body"));

                JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
                GoogleCloudDialogflowV2WebhookRequest dialogflowV2WebhookRequest = jacksonFactory.createJsonParser((String) event.get("body"))
                        .parse(GoogleCloudDialogflowV2WebhookRequest.class);

                System.out.println("== User is Requesting ======================");
                System.out.println(dialogflowV2WebhookRequest.getQueryResult().getQueryText());
                System.out.println("======================");


                GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
                String fullfillmentText = null;

                if (dialogflowV2WebhookRequest.getQueryResult().getLanguageCode().contains("en")) {
                    System.out.println("== Request for EN ======================");
                    fullfillmentText = processEnRequest(dialogflowV2WebhookRequest);
                } else if (dialogflowV2WebhookRequest.getQueryResult().getLanguageCode().contains("hi")) {
                    fullfillmentText = processHiRequest();
                }


                response.setFulfillmentText(fullfillmentText);

                responseText = jacksonFactory.toPrettyString(response);
            }


            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "New item created");

            JSONObject headerJson = new JSONObject();
            headerJson.put("Content-Type", "application/json;charset=utf-8");

            responseJson.put("statusCode", 200);
            responseJson.put("headers", headerJson);
            //responseJson.put("body", responseBody.toString());
            responseJson.put("body", responseText);

        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        } catch (Exception e) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", e);
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }


    public String processEnRequest(final GoogleCloudDialogflowV2WebhookRequest req) {
        System.out.println("== In process Request now: " + req.getQueryResult().getIntent().getDisplayName() + " ===");

        String fullFillmentText = "There was an error performing requested operation. Please try again";
        int version;
        String serviceName;
        String status;
        switch (req.getQueryResult().getIntent().getDisplayName()) {
            case "EnvironmentHealth":
                fullFillmentText = "There are two healthy instances of user service running in your dev environment.";
            case "DeployApp":
                System.out.println("== Action: DeployApp ===");
                version = (int)Double.parseDouble((String) req.getQueryResult().getParameters().get("app_version"));
                serviceName = (String) req.getQueryResult().getParameters().get("app_name");
                status = K8SUtils.deployService(String.format("%s-service", serviceName), String.valueOf(version));
                if (status.equalsIgnoreCase("success")) {
                    fullFillmentText = String.format("Done! Version %s of %s service is deployed successfully to dev environment", version, serviceName);
                } else {
                    fullFillmentText = "There was an error performing service deployment. Please try again";
                }
            case "RollbackApp":
                System.out.println("== Action: DeployApp ===");
                serviceName = (String) req.getQueryResult().getParameters().get("app_name");
                status = K8SUtils.rollbackService(String.format("%s-service", serviceName));
                if (status.equalsIgnoreCase("success")) {
                    fullFillmentText = String.format("Done! %s service is rolledback successfully", serviceName);
                } else {
                    fullFillmentText = "There was an error performing rollback. Please try again";
                }
            default:
                System.out.println("== Action: Unknown ===");
                break;
        }
        System.out.println("FullFullment Text : " + fullFillmentText);

        return fullFillmentText;
    }


    public String processHiRequest() {
        List<String> namespaces = getNamespaces();

        System.out.println("Name Spaces from eks : " + namespaces.toString());

        String fullFillmentText = "आपके वातावरण में इस प्रकार नाम स्थान है, ";

        String s = namespaces.stream().map(Object::toString).collect(Collectors.joining(","));

        fullFillmentText = fullFillmentText + s;

        System.out.println("FullFullment Text : " + fullFillmentText);

        return fullFillmentText;
    }
}
