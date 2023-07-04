# conversion-session-lambda
## Overview
This is a Java project using the Gradle build system. It contains Handler functions that are able to be run via AWS Lambda, and also includes a CloudFormation template to setup any necessary infrastructure.

This is part of a larger project detailed here:  [Omni File Converter](https://github.com/kenmhayes/omni-file-converter)

Session refers to an instance of a user requesting to convert one or more files. Data relating to this request will be stored to DynamoDB and the user can refer back to this information any time before its expiration date to be able to download the converted files.

## Functionality

### CreateSessionHandler

Takes as input metadata of the files requesting to be converted, and returns the ID of a newly created session referencing these file requests. Creates new entries in the sessions and conversion DynamoDB tables. This is meant to be run as a Lambda proxy in API Gateway.

### GetSessionHandler

Given a session ID, will retrieve data relating to that session and any file requests associated with that session via DynamoDB. This is meant to be run as a Lambda proxy in API Gateway.

## Build

To package for Lambda as a ZIP archive, run the command "gradle -q buildZip". You should expect to see the ZIP in the 'build/distributions' folder.

## Deploy

A pre-requisite is a S3 bucket to hold the code artifacts (ZIP archives). This can be named in any way, so long as you enter the correct name when creating the CloudFormation stack. The ZIP should be uploaded prior to running the CloudFormation template.

A separate stack for each of the DynamoDB tables is maintained as indices can not be updated, only deleted and created anew.

In CloudFormation, create stacks using the templates:

- sessiontable-cloudformationtemplate.yml
- conversiontable-cloudformationtable.yml

Then, create a stack using the following template:

- lambda-cloudformationtemplate.yml

If changing the name of the DynamoDB tables, then those updated names need to be passed into the stack for the creation of the Lambda and Gateway resources.

If updating the Gateway resources, then it may be necessary to navigate to the Gateway service in the AWS console and manually deploy the API changes.

## Frameworks

The following libraries and frameworks are used in this project:

- Gradle
- Dagger 2
- Guava
- AWS SDK
- Lombok
- Jackson

- JUnit
- Mockito
