package com.two57.k8s.client;

import java.io.*;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.two57.k8s.client.utils.K8SUtils;
import org.json.simple.JSONObject;

public class K8Controller implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream,
                              OutputStream outputStream,
                              Context context) throws IOException {
        JSONObject responseJson = new JSONObject();
        List<String> namespaces = K8SUtils.getNamespaces();

        JSONObject responseBody = new JSONObject();
        responseBody.put("namespaces", namespaces);
        JSONObject headerJson = new JSONObject();
        headerJson.put("x-custom-header", "my custom header value");
        responseJson.put("statusCode", 200);
        responseJson.put("headers", headerJson);
        responseJson.put("body", responseBody.toString());

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }


    public static void main(String[] args) throws Exception {
        final K8Controller c = new K8Controller();
        //c.handleRequest(null, System.out, null);

        K8SUtils.deployService("user-service", "2", "dev");
    }
}
