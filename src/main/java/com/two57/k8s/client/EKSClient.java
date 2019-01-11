package com.two57.k8s.client;

import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;


public class EKSClient {
    public static void main(String[] args) {
        final String API_URL = System.getProperty("API_URL");
        final String API_TOKEN = System.getProperty("API_TOKEN");

        if (API_TOKEN == null || API_URL == null) {
            System.err.println("Unable to find API_URL or API_TOKEN system properties. Exiting now...");
            System.exit(-1);
        }
        Config config = new ConfigBuilder()
                .withMasterUrl(API_URL)
                .withOauthToken(API_TOKEN)
                .build();

        KubernetesClient client = new DefaultKubernetesClient(config);
        //KubernetesClient client =  new DefaultKubernetesClient();
        NamespaceList myNs = client.namespaces().list();
        System.out.println("################### Namespaces #####################");
        myNs.getItems().stream().forEach(ns -> System.out.println(ns.getMetadata().getName()));
        System.out.println("################### Services #####################");
        client.services().list().getItems().stream().forEach(s -> System.out.println(s.getMetadata().getName()));


        System.out.println("################### DEV Services #####################");
        long start = System.currentTimeMillis();
        for (int i = 0; i <10 ; i++) {
            getServices("dev", client);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time Taken: " + (end-start)/10 + " ms.");
        client.close();

    }

    private static void getServices(final String ns, final KubernetesClient client) {
        client.services().inNamespace("dev").list().getItems()
                .stream()
                .forEach(
                        s -> System.out.println(String.format("%s - %s", s.getMetadata().getName(), s.getMetadata().getAnnotations().get("APP_VERSION")))
                );
    }
}
