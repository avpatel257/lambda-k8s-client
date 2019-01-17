package com.two57.k8s.client;

import java.util.Collections;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.two57.k8s.client.domain.ApiGatewayResponse;
import com.two57.k8s.client.domain.Response;
import com.two57.k8s.client.utils.K8SUtils;

public class K8SHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", input);
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .build();
    }

    public static void main(String[] args) throws Exception {
        //K8SUtils.deployService("user-service", "2");
        //K8SUtils.deployService("user-service", "2");
        K8SUtils.rollbackService("user-service");
        //K8SUtils.test();
    }
}
