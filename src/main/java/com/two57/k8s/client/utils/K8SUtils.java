package com.two57.k8s.client.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class K8SUtils {
    final static String API_URL = System.getenv("API_URL");
    final static String API_TOKEN = System.getenv("API_TOKEN");
    final static String VERSION_TAG = "version";
    final static String SERVICE_TAG = "service";

    final static String namespace = "dev";

    public static final KubernetesClient getConfigClient() {

        if (API_TOKEN == null || API_URL == null) {
            System.err.println("Unable to find API_URL or API_TOKEN system properties. Exiting now...d");
            System.exit(-1);
        }
        Config config = new ConfigBuilder()
                .withMasterUrl(API_URL)
                .withOauthToken(API_TOKEN)
                .build();
        KubernetesClient client = new DefaultKubernetesClient(config);

        return client;
    }

    public static List<String> getNamespaces() {
        return getConfigClient().namespaces().list().getItems().stream().map(ns -> {
            return ns.getMetadata().getName();
        }).collect(Collectors.toList());
    }

    /**
     * Create/Replace Namespace
     *
     * @param namespace
     * @return
     */
    public static String createNamespace(final String namespace) {
        final Namespace ns = new NamespaceBuilder()
                .withNewMetadata().withName(namespace).endMetadata()
                .build();
        getConfigClient().namespaces().createOrReplace(ns);
        return "success";
    }

    public static String deleteNamespace(final String namespace) {
        final Namespace ns = new NamespaceBuilder()
                .withNewMetadata().withName(namespace).endMetadata()
                .build();
        getConfigClient().namespaces().delete(ns);
        return "success";
    }

    public static List<String> getServices(final String namespace) {
        return getConfigClient().services().inNamespace(namespace).list().getItems().stream()
                .map(s -> s.getMetadata().getName())
                .collect(Collectors.toList());
    }

    public static Boolean allowTrafficTo(final String serviceName) {
        return Boolean.TRUE;
    }

    public static Boolean blockTrafficTo(final String serviceName) {
        return Boolean.TRUE;
    }

    public static String deployService(final String serviceName, final String version) {
        final String imageName = String.format("avpatel257/two57-%s:%s.0.0", serviceName, version);

        final Map<String, String> rcLabels = Stream.of(new String[][]{
                {VERSION_TAG, version},
                {SERVICE_TAG, serviceName}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        //Check if version already exists
        ReplicationControllerList gotRcList = getConfigClient().replicationControllers().inNamespace(namespace).withLabel(SERVICE_TAG, serviceName).withLabel(VERSION_TAG, version).list();
        if (gotRcList.getItems().size() != 0) {
            System.err.println(String.format("Version %s or Service [%s] is already deployed", version, serviceName));
            return String.format("Version %s of %s is already deployed in %s", version, serviceName, namespace);
        }

        //Create RC
        final ReplicationController rc = new ReplicationControllerBuilder()
                .withNewMetadata().withName("user-service-rc").addToLabels(rcLabels).endMetadata()
                .withNewSpec().withReplicas(2)
                .withNewTemplate()
                .withNewMetadata().addToLabels("app", "user-service-app").addToLabels(VERSION_TAG, version).endMetadata()
                .withNewSpec()
                .addNewContainer().withName("user-service").withImage(imageName)
                .addNewPort().withContainerPort(8080).endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec().build();

        getConfigClient().replicationControllers().inNamespace(namespace).createOrReplace(rc);
        //getConfigClient().replicationControllers().inNamespace(namespace).delete(rc);

        return "success";
    }

    /**
     * This method will always deploy version 1 of the service for now
     * @param serviceName
     * @return
     */

    public static String rollbackService(final String serviceName, final String version ) {
        return deployService(serviceName, version);
    }
}
