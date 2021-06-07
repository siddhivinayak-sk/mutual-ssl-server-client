# Server - with SSL implementation

## Description
This spring based java project has been written to create a sever applicaiton backed by tomcat container to expose some resource end points which will be consumed by REST client to demostrate the SSL implementaiton with server applications.

The code has been written in a way that it can be enabled/disabled with configuration only to test each scenario of SSL implementation.

There are three different implementations of TLS at server:

1. No SSL/HTTP - This is basic implemenation of HTTP protocol where there is no TLS implementation from either side (server/client).

   Disable below configuration by setting security.require-ssl: false, set server.port to 80 and commenting other properties listed here:
   
   ```
   server.port : 80
   security.require-ssl: false
   #server.ssl.key-store-type : PKCS12
   #server.ssl.key-alias : 1
   #server.ssl.key-store : classpath:server.p12.jks
   #server.ssl.key-store-password : 123456
   
   #server.ssl.client-auth : need
   #server.ssl.trust-store-type : JKS
   #server.ssl.trust-store : classpath:server_truststore.jks
   #server.ssl.trust-store-password : 123456
   ```

2. SSL (TLS) with Server without Trust Validation - This is the basic implementation of SSL with HTTP sever where a Private key is configured in server which enables HTTPS based communication.

   To implement, need to add SSL keystore and server port need to be changed for TLS based communication.
   ```
   server.port : 443
   security.require-ssl: true
   server.ssl.key-store-type : PKCS12
   server.ssl.key-alias : 1
   server.ssl.key-store : classpath:server.p12.jks
   server.ssl.key-store-password : 123456
   ```

3. SSL (TLS) with Server with Trust Validation - In this implemenation, the communication happen on SSL with Client authentication/validation based upon the client certificate. In this configuration, a private key is setup for SSL and a keystore is configured which is used to store trust certficates of client.

   To implement, need to enable the trust validation.
   ```
   server.ssl.client-auth : need
   server.ssl.trust-store-type : JKS
   server.ssl.trust-store : classpath:server_truststore.jks
   server.ssl.trust-store-password : 123456   
   ```

## Use Case
The secuirty with any integration/communication over network is most important factor which must be assessed and handled perfrectly. With Hyper Text Transfer Protocol (HTTP) which runs over the seven layers of OSI model where there is a way to provide encryption on OSI's transport layer and hence it is also called Transport Layer Security (TLS). The TLS implementation with HTTP results in a new protocol called Hyper Text Transfer Protocol Secure (HTTPS) and also called HTTP with Secure Socket Layer (SSL).

To protect data in transit, SSL is a proven solution for data theft and data corruption. To implement SSL in server, the most server containers provide in built mechanism which need to configure. When SSL is configured, an key is required which will be used for following purpose:
- Key for encryption while sending data to client for encrypted data transit.
- Server identification, a key is used to establish identity for server.
- Identity verification of server with trusted certificate authorities.

To configure SSL, a Private Key is required which is configured with server containers like Tomcat. Once, it is configured, SSL is enabled and HTTP connectors becomes HTTPS.

Trust validation of client is additional layer of security to verify the identity of client where a public certificate of trusted client is stored at the server trust store which is validated by server container when any requests comes to server. 
  
Parent Link: https://github.com/siddhivinayak-sk/mutual-ssl-server-client

Client Link: https://github.com/siddhivinayak-sk/mutual-ssl-server-client/tree/main/mutual-ssl-client


## Technology Stack
- Java 8 or later
- Spring boot 2.x
- Maven as build tool

## Build
Maven has been used as build tool for this project with default Spring boot build plugin.
Hence, we can use below maven code to build the runnable spring boot jar:

```
mvn clean package
```

## Deployment 
After compile and build, this will result in a runnable jar file which can be invoked to start sever. To run jar file use below command to start server:

```
java -jar <jar file name>
```

In case, if need to externalize the YAML configruation and run with external YAML:

```
java -jar <jar file name> --spring.config.additional-location=<path of yaml file>
```


## Configuration
The server applicaiton has been written in way to test different scenarios. Below are description of configruation:

```
server.port : 443
server.servlet.context-path : /

#Logging configuration
logging.path: '.'
logging.file: mutual-ssl-server.log
logging.config: 'classpath:log4j2.xml'

#Properties for certificate validation
security.require-ssl: false
server.ssl.key-store-type : PKCS12
server.ssl.key-alias : 1
server.ssl.key-store : classpath:server.p12.jks
server.ssl.key-store-password : 123456

#Properties for SSL
server.ssl.client-auth : need
server.ssl.trust-store-type : JKS
server.ssl.trust-store : classpath:server_truststore.jks
server.ssl.trust-store-password : 123456

#Property to match the common names from the certificate provided.
cn.usernames : www.myclient.com

#Additional properties for Request Certificate Validation
header.validation.required : false
validation.keystoretype : JKS
validation.keystorePath : classpath:server_truststore.jks
validation.keystoreSecret : 123456
validation.publicKeySequence :  clientcert
```

- server.port - Define the port of server. For HTTP, default port should be 80. For HTTPS, default port will be 443.
- server.servlet.context-path - Context path for the server.
- logging.path - Location where log file will be generated.
- logging.file - Log file name.
- logging.config - Log4j2 configuration file path.
- security.require-ssl - If need to enable SSL/TLS with server set to true else set false.
- server.ssl.key-store-type - Type of keystore configurated for SSL.
- server.ssl.key-alias - Alias name of the key available into the Keystore which will be used for SSL.
- server.ssl.key-store - Path of keystore file.
- server.ssl.key-store-password - Password for the keystore.
- server.ssl.client-auth - If trust validation of client is required set it with value need.
- server.ssl.trust-store-type - Type of the keystore used to store the trust certificate.
- server.ssl.trust-store - Path of the keystore used for storing trust certificate.
- server.ssl.trust-store-password - Password of trust keystore.
- cn.usernames - Provide list of CN names of client.
- header.validation.required - If additional header, payload validation requried. Enable it with true value.
- validation.keystoretype - Type of keystore file which used in header validation.
- validation.keystorePath - Path of the keystore used to store the certificate which will be used for header signature validation.
- validation.keystoreSecret - Password of validation keystore.
- validation.publicKeySequence - Alias of the certificate used for header validation.

Note: The Jasypt, Key Vault implemenation are optional.
