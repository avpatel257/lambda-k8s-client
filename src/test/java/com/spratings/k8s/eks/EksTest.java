package com.spratings.k8s.eks;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.two57.k8s.client.utils.K8SUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.math.BigDecimal;


public class EksTest {


   @Test
   public void testEnResponse(){
//      APIDemoHandler api = new APIDemoHandler();
//      System.out.println("English Response: " + api.processEnRequest());
   }

   @Test
   public void  testHiResponse(){
//      APIDemoHandler api = new APIDemoHandler();
//      System.out.println("Hindi Response: " +api.processHiRequest());
   }

   @Ignore
   public void testGetNameSpaces(){

      try {
         JSONParser parser = new JSONParser();


         Object obj = parser.parse(new FileReader("src/test/resources/deployapp.json"));
         JSONObject jsonObject =  (JSONObject) obj;

          JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
          GoogleCloudDialogflowV2WebhookRequest req = jacksonFactory.createJsonParser(jsonObject.toString())
                  .parse(GoogleCloudDialogflowV2WebhookRequest.class);


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
                     fullFillmentText = String.format("Done! Version %s of %s service is deployed successfully to dev environment", version, serviceName);
                  } else if (status.contains("already deployed")){
                     fullFillmentText = status;
                  } else {
                     fullFillmentText = "There was an error performing service deployment. Please try again";
                  }
                  break;
               case "RollbackApp":
                  System.out.println("== Action: DeployApp ===");
                  System.out.println(req.getQueryResult().getParameters().get("app_version"));
                  System.out.println(req.getQueryResult().getParameters().get("app_name"));
                  serviceName = (String) req.getQueryResult().getParameters().get("app_name");
                  status = K8SUtils.rollbackService(String.format("%s-service", serviceName), "1");
                  if (status.equalsIgnoreCase("success")) {
                     fullFillmentText = String.format("Done! %s service is rolledback successfully", serviceName);
                  } else {
                     fullFillmentText = "There was an error performing rollback. Please try again";
                  }
                  break;
               default:
                  System.out.println("== Action: Unknown ===");
                  break;
            }
         }catch (Exception e){
            e.printStackTrace();
         }

         System.out.println("FullFullment Text : " + fullFillmentText);

//          System.out.println("== Request ======================");
//         System.out.println(request.getQueryResult().getQueryText() );
//         System.out.println("======================");
//
//         GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
//
//         String fullFillmentText = "You have folling namespaces in your environment ";
//         List<String> namespaces =  new ArrayList<>(); //getNamespaces();
//         String s = namespaces.stream().collect(Collectors.joining(","));
//
//         response.setFulfillmentText(fullFillmentText + s);
//
//         System.out.println("FullFullment Text : " + fullFillmentText);
//
//
//         String responseText = jacksonFactory.toPrettyString(response);
//
//         System.out.println("\n"
//                 +responseText);
      }catch (Exception e){
         e.printStackTrace();
      }


   }


}
