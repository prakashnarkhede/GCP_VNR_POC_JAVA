# GCP_VNR_POC_JAVA
GCP POC  -- Read text file from cloud --> Process it using cloud SDK --> push back processed files to GCP Storage buckets


install VM.

update VM instance --> sudo apt update

Test if required packages are present, If not onstals--> sudo apt install python3

By default gcloud is installed on google VMs, If using external VM, need to install Gcloud. The gcloud tool is part of the Cloud SDK
Install Gcloud on unix: https://cloud.google.com/sdk/docs/#linux

Check how to login, set project, set authorization when using VM / unix other than project GCP account
Copy file from VM instance to Cloud storage: 
gsutil cp "/home/aec_prakash/VM CLI.txt" "gs://ctct_vnr_bucket/Consumer1"




pubsub event notifications (on google cloud bucket):
Create notifications for all events:                         gsutil notification create -t "bucketEventTriggerTopic" -f json gs://ctct_vnr_bucket
Create notifications only for file create event:             gsutil notification create -t "bucketEventTriggerTopic" -f json -e OBJECT_FINALIZE gs://ctct_vnr_bucket

	--> https://cloud.google.com/storage/docs/reporting-changes
	
To get info about all created notifications for bucket: gsutil notification list gs://ctct_vnr_bucket

Delete all notifications from specific bucket: gsutil notification delete gs://ctct_vnr_bucket


Create Subscriber.

To get message from subscriber::::    gcloud pubsub subscriptions pull --auto-ack subscriber1

google pubsub api authontication: 
	Set below environment variable. give try with either
	set GOOGLE_APPLICATION_CREDENTIALS=C:\Users\praka\AppData\Roaming\gcloud\legacy_credentials\aec.prakash@gmail.com\adc.json
	set GOOGLE_APPLICATION_CREDENTIALS=E:\GCP\VnR POC\Service acc key - maximal-ship-242013-072efa6d3c63.json
or set CLOUDSDK_CONFIG=$(cygpath -w ~/.config/gcloud)\GOOGLE_APPLICATION_CREDENTIALS=$(cygpath -w ~/.config/gcloud)/application_default_credentials.json


Upload file from local machine to GCP bucket using GSUTIL:
gsutil cp C:\Users\praka\eclipse-workspace\VnR_Java\tempFolder\OutputFile\Out_Batch_Input_File_C3.txt gs://ctct_vnr_bucket/Consumer1/OutputFile/







Create Docker file in eclipse ide.
--> Create docker image (go the the directry where Dockerfile is in) --> docker build -f Dockerfile -t docker_vnr_poc .
Enable container registry api
Open cloud sdk, run --> gcloud auth configure-docker


Run below commands from GCLOUD
To tag docker image
docker tag docker_vnr_poc gcr.io/maximal-ship-242013/docker_vnr_poc:latest

To push docker image to container registry
docker push gcr.io/maximal-ship-242013/docker_vnr_poc:latest

Then go to Kubernate engine, Deploy 

Make sure that you set GOOGLE_APPLICATION_CREDENTIALS in environment variable on container deploy screen
