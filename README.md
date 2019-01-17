[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f17f1b801de5402ea7d6086d5c235983)](https://app.codacy.com/app/avpatel-257/lambda-k8s-client?utm_source=github.com&utm_medium=referral&utm_content=avpatel257/lambda-k8s-client&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://travis-ci.org/avpatel257/lambda-k8s-client.svg?branch=master)](https://travis-ci.org/avpatel257/lambda-k8s-client)


###EKS client wrapper

This uses `https://github.com/fabric8io/kubernetes-client` as java client.

To run this code, please pass K8S `API_URL` and `API_TOKEN` as system properties.

Also provide `KUBERNETES_CERTS_CA_DATA` as environment variable. 


Purpose of this project is to work with ChatOps. Google assisntant will be passing the context and parameters to API Gateway, which will trigger a lambda to process the event. The response will be given back to google assistant and eventually to the user.



Demo Flow

- Q. How is health of my dev environment ? 
- A. Your dev environment health is looking good, There are 2 healthy instances of user service are running with version 1.
- Q. Can you please deploy version 2 ?
- A. Done! Version 2 of User service is deployed successfully to dev environment
- Q. Can you please rollback user service with version 1?
- A. Done! Rollback Successful. User service is running with verison 1. 