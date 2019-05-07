# Welcome to Chorus Opensourse

Here's description how to run Chorus.

### Prerequisites

 * [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), version 1.8. The application doesn't support Java 9!
 * [Apache Maven](http://maven.apache.org), version 3.3.9.
 * [SMTP server credentials](https://docs.aws.amazon.com/ses/latest/DeveloperGuide/smtp-credentials.html) to let the app send emails.
 * [Amazon S3](http://aws.amazon.com/s3/) storage credentials (bucket name, key and secret) to store uploaded files.

### Check your environment
 * Type in command promt or terminal `java -version` 
 The output must be like this:
 >java version "1.8.0_144"
 Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
 Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)
   
# Instruction for development

### Configuration for deployment at the server startup on the local machine


1. Download [ApacheTomcat](https://tomcat.apache.org/download-80.cgi)       
2. Add webapp.war for deployment at the server startup.    
3. Copy property files to home directory (application.properties, jdb.properties and messaging.properties)
	- In application.properies and messaging.properties you need to configure AWS credentials and another necessary conf. data, 
	but if you don’t include conf. data like a AWS credentials in project directory, system will take conf. data form home directory automatically.	

### Example:

**application.properties**

* amazon.key=LHJKJBHJHDBKLDBLGHD
* amazon.secret=LJNDKJBJKDBGYTEYTIUWOMCMKJNCHWBCGWCG
* amazon.active.bucket=bucket=aws-bucket-production
* amazon.archive.bucket=bucket=aws-bucket-production
* amazon.glacier.vault=bucket=aws-bucket-production

**test messaging.properties**

* amazon.accessKey=LHJKJBHJHDBKLDBLGHD
* amazon.secretKey=LJNDKJBJKDBGYTEYTIUWOMCMKJNCHWBCGWCG
* amazon.default-bucket=aws-bucket-test

**test application.properties**

* amazon.key=LHJKJBHJHDBKLDBLGHD
* amazon.secret=LJNDKJBJKDBGYTEYTIUWOMCMKJNCHWBCGWCG
* amazon.active.bucket=aws-bucket-test




### Start working with tests.


1. Before run tests you need to rename messaging.properties, just add any symbols to file name in home directory, something like that 'messaging.properties.off'.
2. All tests in model-impl and dm-integration-helper extend AbstarctTest or another abstract classes 
	- it's needed for setting up test application context
 	- realization of methods for test cases with similar functionality
3. Additional class utils UseCase which provides ready solutions that will help you to understand how to create 
	-lab, instrument, person and etc. and implement it in your test cases.


	For example you can see on this test 'chorus-opensource/chorus/webapp/src/test/java/com/infoclinika/mssharing/web/uploader/UploaderRestServiceTest.java'
		
	1. UploaderRestServiceTest extends AbstractDataBasedTest and inject many services and interfaces which are used in this module for all existing tests.
		
	2. https://github.com/InfoClinika/chorus-opensource/tree/master/chorus/webapp/src/test/java/com/infoclinika/mssharing/web/uploader
	

3. When you run tests which work with repository using h2 database, database automatically create tables 
4. After each test case, database automatically clear data


### Implement or expand new functional


    MODEL - business logic API
        
    WEBAPP module is responsible for rest services, auth, download files and controllers etc..
        
    MODEL-IMPL - it's a general module where we need to implement all functionalilty (business logic impl)
        
    DATA-MANAGEMENT - contains basic data management functionality which if necessary for expanding of model-impl
	
```
TO EXPAND NEW FUNCTIONAL

	
1. Create new interface in 'model'
	
2. Extend any template that you need for your function in 'dm-integration-helper'
	
3. Create implementation of your interface(module - 'model') in the module 'model-impl'

	

TO IMPLEMENT NEW FUNCTIONAL

	
1. Create new interface in 'model'
	
2. Create implementation of your interface(module - 'model') in the module 'model-impl'
```

### Working with AWS


1. CloudStorageFactory is based on platform-storage.properties
2. CloudStorageService API for working with AWS
3. FileStorageService the implementation of the storage service, specific for the hosting-provided storage mechanism
	- put file, delete, get file and etc..
4. In addition navigate to chorus-opensource/chorus/model-impl/src/test/java/com/infoclinika/mssharing/model/test/instrument/ManagingFilesTest.java package for understand how it works or
 - https://github.com/InfoClinika/chorus-opensource/blob/master/chorus/model-impl/src/test/java/com/infoclinika/mssharing/model/test/instrument/ManagingFilesTest.java
 
### Licensing

Please see [LICENSE](LICENSE) for more info.
