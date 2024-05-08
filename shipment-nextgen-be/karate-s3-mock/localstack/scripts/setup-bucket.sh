#!/usr/bin/env bash

ORG_ID="82027602-402a-41f7-82dc-80cfbb011b4e"

awslocal s3 mb s3://shipment-nextgen-nonprod-uploads
awslocal s3api put-object\
 --bucket shipment-nextgen-nonprod-uploads\
 --key local/$ORG_ID/shipment_attachments/karate-test-sample-file.jpg\
 --body /home/logo.jpg
awslocal s3api put-object\
 --bucket shipment-nextgen-nonprod-uploads\
 --key local/$ORG_ID/shipment_attachments/karate-test-sample2-file.jpg\
 --body /home/logo.jpg
awslocal s3api put-object\
 --bucket shipment-nextgen-nonprod-uploads\
 --key local/$ORG_ID/shipment_attachments/karate-pdf-file.pdf\
 --body /home/logo.pdf
awslocal s3api put-object\
 --bucket shipment-nextgen-nonprod-uploads\
 --key local/$ORG_ID/cost_attachments/karate-png-file.jpg\
 --body /home/logo.jpg
awslocal s3api put-object\
 --bucket shipment-nextgen-nonprod-uploads\
 --key local/$ORG_ID/cost_attachments/karate-jpg-file.png\
 --body /home/logo.png