# Utility - HTTP/HTTPS Client Utility

## Description
This utility has been developed to make rest call based upon the configuration into the YAML. The utility is baked by RestTemplate powered by Spring and for execution purpose, Spring boot based task created which picks the configuration from YAML and make calls as per configuration.

The REST based calls supports methods provided by HTTP along with configurable query parameters and request body. Although, this utility supports complete REST based client but specially focused on the security part of HTTP calls using Secure Socket Layer (SSL) and Trust based validation.

There are four options with Transport Layer Security (TLS) implementation with HTTP protocol:
1. No SSL/HTTP - This is basic implementation of HTTP protocol where there is no TLS implementation from either side (server/client).

   No need to define additional configuration/arrangement for HTTP as it does not requires encryption.

2. SSL (TLS) with Server without Trust Validation - When server resource (the URL/endpoint exposed by server which has to be invoked by client) implements the TLS with a private key and hence the client must communicate in encrypted fashion. With this implementation server implements private key at server but does not implement the trust validation at server level and hence only TLS implemented but client does not verified, therefore at client there is no any kind of private key or certificate required at client level, only need to establish connection with SSL enabled mode.

   To implement, no need to add additional configuration, only protocol HTTPS in URL will enable this.

3. SSL (TLS) with Server with Trust Validation - In this implementation, server implements Private Key for TLS and a trust store which contains the clients certificate who is going to consume the server resource. In other words, server will be on SSL and consumer client is validated with trust certificate which has been provided by client and therefore, client requires a private key which public key (X509 certificate) goes with each request to server and establish encrypted connection with certificate validation at server level.

   To implement, need to enable Key Material and define the Private Key for the key material.

4. Mutual SSL/Server with Trust Validation and Client with Server Identification Validation - This implementation is quite similar to Point 3 with addition that client also contains a trust validation with server. In other words, client additionally validates the server identity with a certificate provided by server. Hence, it is called Mutual SSL and while making call to server, it requires a Private Key for client SSL & identification and Server public certificate to validate the server's identity.

   To implement, need to enable both Key & Trust Material and define the Private Key for the key material and Public Key certificates of server.

## Use Case
The security with any integration/communication over network is most important factor which must be assessed and handled perfectly. With Hyper Text Transfer Protocol (HTTP) which runs over the seven layer of OSI model where there is a way to provide encryption on OSI's transport layer and hence it is also called Transport Layer Security (TLS). The TLS implementation with HTTP results in a new protocol called Hyper Text Transfer Protocol Secure (HTTPS) and also called HTTP with Secure Socket Layer (SSL).

Since HTTP is based upon request and response therefore bidirectional communication happens and encryption requires at both end and similarly decryption also requires to happen at both end. Therefore, there are multiple ways with implementation with HTTPS which discussed into the Description section.

This client depicts the different implementation of SSL with clients.

To use this utlity, below are the properties need to provide into the YAML for each call (multiple, calls can also be configured in YAML):
```
calls:
  - http-url: 'https://www.myserver.com/test'
    http-method: GET
    key-material: true
    key-material-type: PKCS12
    key-material-path: classpath:client.p12.jks
    key-material-secret: 123456
    trust-material: true
    trust-material-type: JKS
    trust-material-path: classpath:client_truststore.jks
    trust-material-secret: 123456
    diable-cookie-management: true
    set-custom-connection-manager: true
    headers:
      #Accept: '*/*'
      Connection: keep-alive
      Host: www.myclient.com
    query-parameters:
      abc: abc
    request-body-path: classpath:readme.txt
    #response-file: c:/temp/myserver.htm
```

Details:
```
   http-url - Takes target resource URL of server resource.
   http-method - Set HTTP method for the call like POST, GET, PUT, PATCH etc.
   diable-cookie-management - Enable/disable cookie management
   set-custom-connection-manager - This is implementation factor whether custom connection manager used.
   headers - List of headers to be sent to sever, provided with key and value pair e.g. Content-Type: plain/text etc.
   query-parameters - Way to provide query parameters (Optional) in key and value pair.
   request-body-path - Way to provide request body with POST or other methods (Optional).
   response-file - Way to direct server response to file (Optional), by default server headers and response printed to console with logger.
```

In case server requires trust validation at server level with SSL at both end, below parameters need to be defined:
```
   key-material - To enable set true else false.
   key-material-type - Type of key store like PKCS12, JKS etc.
   key-material-path - Provide path of the keystore.
   key-material-secret - Password of keystore
```

In case client requires trust/identity validation of server with SSL at both end, below parameters need to be defined:
```
   trust-material - To enable set true else false.
   trust-material-type - Type of key store like PKCS12, JKS etc.
   trust-material-path - Provide path of the keystore.
   trust-material-secret - Password of keystore
```

Parent Link: https://github.com/siddhivinayak-sk/mutual-ssl-server-client

Server Link: https://github.com/siddhivinayak-sk/mutual-ssl-server-client/tree/main/mutual-ssl-server


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
This utlity can be used as library in the domain projects or reference can be taken or can be invoked directly for testing purpose by using below command.

```
java -jar <jar file name>
```
