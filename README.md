# Welcome to Chorus Opensourse

Here's description how to run Chorus.

### Prerequisites

 * [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), version 1.8. The application doesn't support Java 9!
 * [Apache Maven](http://maven.apache.org), version 3.3.9.
 * Installed [Docker](https://www.docker.com/community-edition#/download) and [docker compose](https://docs.docker.com/compose/install/) on your local machine.
 * [SMTP server credentials](https://docs.aws.amazon.com/ses/latest/DeveloperGuide/smtp-credentials.html) to let the app send emails.
 * [Amazon S3](http://aws.amazon.com/s3/) storage credentials (bucket name, key and secret) to store uploaded files.

### Check your environment
 * Type in command promt or terminal `java -version` 
 The output must be like this:
 >java version "1.8.0_144"
 Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
 Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)
 * Type in command promt or terminal `docker -version` 
  The output must be like this:
  >Docker version 17.05.0-ce, build 89658be
 * Type in command promt or terminal `docker-compose -version` ;
   The output must be like this:
   >docker-compose version 1.17.0, build ac53b73
   
### To run the Chorus Opensourse installation in docker containers:
 * If you use [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), you may download [docker plugin](https://www.jetbrains.com/help/idea/docker.html) for comfort work with Docker integration.
 * Build the application using 'clean install -DskipTests -Pdocker' command
 * If you use IntelliJ IDEA just run **docker-compose.yml** using IDEA docker plugin. Or you can do it manually in terminal,
  just type this command `docker-compose up` in *your-project-folder/docker/* .
 * Point your browser to http://localhost:8080
 * Register admin user within Web UI
 * User name is "demo-user" and password is "pwd".
 * Go to database with your favorite SQL tool and grant this user admin rights
   - UPDATE USER SET admin=1;