[![Build Status](https://travis-ci.org/avpatel257/lambda-k8s-client.svg?branch=master)](https://travis-ci.org/avpatel257/lambda-k8s-client)


###EKS client wrapper

This uses `https://github.com/fabric8io/kubernetes-client` as java client.

To run this code, please pass K8S `API_URL` and `API_TOKEN` as system properties.

Also provide `KUBERNETES_CERTS_CA_DATA` as environment variable. 


Purpose of this project is to work with ChatOps. Google assisntant will be passing the context and parameters to API Gateway