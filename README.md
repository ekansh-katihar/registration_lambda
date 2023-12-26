# Lambda Regsitration controller
It handles user registration and onboarding them to the platform

# Build
This project depends on `communication-commons` project. Put that jar in the `local-repo` folder and build this project using `mvn clean package`

### Other dependencies:
maven.compiler.source:11

maven.compiler.target:11

Developed with: Apache Maven 3.8.6 Java version: 17.0.5, vendor: Oracle Corporation 
 
## Configuration
Deploy it on AWS Lambda (Registration_Controller) with java 11 runtime
Add VERIFY_TOKEN and WHATSAPP_TOKEN to environment variable (See cloud API for whatsapp).





