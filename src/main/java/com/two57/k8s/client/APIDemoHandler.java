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
import java.math.BigDecimal;
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


                fullfillmentText = processEnRequest(dialogflowV2WebhookRequest);


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

        try {

            switch (req.getQueryResult().getIntent().getDisplayName()) {
                case "EnvironmentHealth":
                    fullFillmentText = "There are two healthy instances of user service running in your dev environment.";
                case "DeployApp":
                    System.out.println("== Action: DeployApp ===");

                    System.out.println(req.getQueryResult().getParameters().get("app_version"));
                    System.out.println(req.getQueryResult().getParameters().get("app_name"));

                    version = ((BigDecimal) req.getQueryResult().getParameters().get("app_version")).intValue();
                    serviceName = (String) req.getQueryResult().getParameters().get("app_name");


                    status = K8SUtils.deployService(String.format("%s-service", serviceName), String.valueOf(version));
                    if (status.equalsIgnoreCase("success")) {
                        if (req.getQueryResult().getLanguageCode().contains("en")) {
                            System.out.println("== Request for EN ======================");
                            fullFillmentText = String.format("Done! Version %s of %s service is deployed successfully to dev environment", version, serviceName);
                        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
                            fullFillmentText = String.format("कामयाबी!! %s सर्विस वर्ज़न  नम्बर %s का डिप्लोयमेंट कामयाब हो गया हे.",  serviceName, version);
                        }

                    } else if (status.contains("already deployed")){
                        if (req.getQueryResult().getLanguageCode().contains("en")) {
                            System.out.println("== Request for EN ======================");
                            fullFillmentText = String.format("Sorry! Version %s of %s service is already deployed, please deploy new version", version, serviceName);
                        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
                            fullFillmentText = String.format("माफ़ कीजिए  %s सर्विस वर्ज़न  नम्बर %s का डिप्लोयमेंट हो चुका हे. कृपया करके नया वर्ज़न बताए.",  serviceName, version);
                        }

                    } else {
                        if (req.getQueryResult().getLanguageCode().contains("en")) {
                            System.out.println("== Request for EN ======================");
                            fullFillmentText = "Sorry! There was an error performing service deployment. Please try again";
                        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
                            fullFillmentText = "माफ़ कीजिए, टेक्निकल ख़राबी के कारण डिप्लोयमेंट खतम न हो सका, कृपया करके दुबारा आदेश दीजिए.";
                        }

                    }
                    break;
                case "RollbackApp":
                    System.out.println("== Action: DeployApp ===");
                    System.out.println(req.getQueryResult().getParameters().get("app_version"));
                    System.out.println(req.getQueryResult().getParameters().get("app_name"));
                    serviceName = (String) req.getQueryResult().getParameters().get("app_name");
                    status = K8SUtils.rollbackService(String.format("%s-service", serviceName));
                    if (status.equalsIgnoreCase("success")) {

                        if (req.getQueryResult().getLanguageCode().contains("en")) {
                            System.out.println("== Request for EN ======================");
                            fullFillmentText = String.format("Done! %s service is rolledback successfully", serviceName);
                        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
                            fullFillmentText = String.format("Done! %s service is rolledback successfully", serviceName);
                            fullFillmentText =  String.format("कामयाबी!, %s सर्विस का रोल्बैक कामयाब हो गया हे.", serviceName);
                        }
                    } else {
                        fullFillmentText = "There was an error performing rollback. Please try again";
                        if (req.getQueryResult().getLanguageCode().contains("en")) {
                            System.out.println("== Request for EN ======================");
                            fullFillmentText = "Sorry! There was an error performing rollback operation. Please try again";
                        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
                            fullFillmentText = "माफ़ कीजिए, टेक्निकल ख़राबी के कारण डरोल्बैक खतम न हो सका, कृपया करके दुबारा आदेश दीजिए.";
                        }
                    }
                    break;
                default:
                    System.out.println("== Action: Unknown ===");
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (req.getQueryResult().getLanguageCode().contains("en")) {
            System.out.println("== Request for EN ======================");
            fullFillmentText = "There was an error performing requested operation. Please try again";
        } else if (req.getQueryResult().getLanguageCode().contains("hi")) {
            fullFillmentText = "माफ़ कीजिए, टेक्निकल ख़राबी के कारण आपका आदेश खतम न हो सका, कृपया करके दुबारा आदेश दीजिए.";
        }
        System.out.println("FullFullment Text : " + fullFillmentText);

        return fullFillmentText;
    }


    public String processGetEnvironment() {
        List<String> namespaces = getNamespaces();

        System.out.println("Name Spaces from eks : " + namespaces.toString());

        String fullFillmentText = "आपके वातावरण में इस प्रकार नाम स्थान है, ";

        String s = namespaces.stream().map(Object::toString).collect(Collectors.joining(","));

        fullFillmentText = fullFillmentText + s;

        System.out.println("FullFullment Text : " + fullFillmentText);

        return fullFillmentText;
    }
}
