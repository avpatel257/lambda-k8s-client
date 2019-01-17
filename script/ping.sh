#!/bin/bash

while true;
do
	host=`kubectl describe svc user-service-app -n dev | grep Endpoints | awk '{ print $2}' | awk -F, '{print $2}'`
	url=$host/users/user/1
	curl -s $url | jq '.version'
	sleep 1
done
