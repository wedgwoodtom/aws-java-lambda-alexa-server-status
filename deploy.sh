#!/usr/bin/env bash

# Deploy code to AWS Lambda

# Dependencies
# awscli (brew install awscli), set up guide: https://aws.amazon.com/cli/

# Usage with IntelliJ
# 1. Drop me (deploy.sh) into your project root
# 2. Create dir "aws-dist" in your project root, right click on it and "Mark directory" as "Excluded"
# 3. Open IntelliJ preferences, Tools / File watchers
#     a) Add a new watcher
#         Name: Deploy to AWS on change
#         File type: Any
#         Scope: Project Files
#         Program: Browse deploy.sh
#         Show console: Error
# 4. Cmd / Ctrl + S, and see the magic happen

# Troubleshoot
# Run deploy.sh from terminal and see what goes wrong

LAMBDA_FUNCTION_NAME=alexaSayHi
DIST_DIR=target
ZIP_FILE_NAME=cts-lambda-server-status-handler-1.0-SNAPSHOT.jar

#mkdir -p ${DIST_DIR}
#rm -f ${DIST_DIR}/${ZIP_FILE_NAME}
#zip --exclude="*.idea*" --exclude="*${DIST_DIR}*" --exclude="deploy.sh" -r ${DIST_DIR}/${ZIP_FILE_NAME} .

#TODO: Figure out how to host the file in S3

mvn clean package
aws lambda update-function-code --region us-east-1 --function-name ${LAMBDA_FUNCTION_NAME} --zip-file fileb://${DIST_DIR}/${ZIP_FILE_NAME}
#aws lambda update-function-code --function-name ${LAMBDA_FUNCTION_NAME} --zip-file fileb://${DIST_DIR}/${ZIP_FILE_NAME} --s3-bucket aws-lambda-handler-repository --s3-key ${ZIP_FILE_NAME}
echo Code successfully uploaded to AWS!

#cts-lambda-highscore-handler-1.0-SNAPSHOT.jar
#https://s3-us-west-2.amazonaws.com/aws-lambda-handler-repository/cts-lambda-highscore-handler-1.0-SNAPSHOT.jar