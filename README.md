# TLS/SSL and Mutual SSL Implementation in Web Application along with Client

## Description
Data security is a major concerns in web applications which exposed and access via public network/internet. When any connection established in public network, there are various ways to steal data while it is on transit/network. To prevent such data vulnerability data on transit must be encrypted especially in the cases where data contains:
- Personal information
- Financial data
- Secured information like credit card details, credentials, private data like cellphone number etc.

The network which used for internet/LAN/WAN/MAN are based upon the TCP/IP protocol and communication established on Open Systems Interconnection (OSI) model which contains seven layers on which communication happens. It has one layer called Transport layer (OSI 4th layer) on which there is a way to add feature for encryption of data so that while on transit, data must be encrypted and if any data packet is stolen, the intruder will not get the actual data.

Consider the below communication how encryption applied:
```
1. From Client => Server
Server: OSI Layer                                       Client: OSI Layer
        ...                                                     ...
        Transport Layer (Encryption) ============> (Decryption) Transport Layer
        ...                                                     ...

2. From Server => Client
Server: OSI Layer                                       Client: OSI Layer
        ...                                                     ...
        Transport Layer (Decryption) <============ (Encryption) Transport Layer
        ...                                                     ...
```

Since this security mechanism (Encryption/Decryption) applied on Transport layer of OSI so it is called Transport Leyer Security (TLS).

This encryption and decryption happen with help of Public and Private Key. The Public Key is used to encrypt the data and Private Key is used to decrypt the data which reached to destination.

This TLS security can be implemented at both level (Client and Server).

This project contains a server implementation and client implementation along with different type of implementation.

## Terminologies
1. Server: Server is a technical component which is hosted by the business, gets request from client, process and provide response to client. Example, server which hosts applications like bank application which user uses to do transaction.
2. Client: Client is machine & technical component where actual user interact to send request to server for processing. Once request processed at server, server sends response to client where user consume the response. Example, browser on machine which is used to interact with web application.
3. Cryptography: Cryptography a technology which is used to protect the data. It consists of two processes:

   a. Encryption: Encryption is a process of Cryptography where actual data is converted into another format using some mathematical operation.

   b. Decryption: When encrypted data is reached to actual person, it is again go through a process called Decryption to convert to original data from encrypted data.
   
   The encryption and decryption logic/algorithm requires a key which is only available to sender and receiver so if data is stolen by anyone else will not get actual data.
   
   The Key used into cryptography may be used as symmetric and asymmetric key. In TLS, asymmetric key is used which consists of Public and Private Key.

4. Private Key: A key which is available to the receiver and used to decrypt the data. It is available into formats like pem, pem etc. Mostly, it is available at server. In case of mutual SSL case, client also holds a private key.
5. Public Key: A key which is available at client and used to encrypted data before sending on network. When client sends data, data is encrypted and sent to sever. It is available into formats like crt, cert, der etc.
6. Keystore: A container to store the public/private key securely protected by passwords. It is available in formats like .jks and .pfx.
7. HTTP: Hyper Text Transfer Protocol (HTTP), a protocol based on TCP/IP which is used to provide communication for web applications.
8. HTTPS:  HTTP Secure (HTTPS) = HTTP + TLS; When Transport Layer Security applied with HTTP called HTTPS.
9. TLS: A security methodology, where data cryptography is applied on transport layer of OSI communication model.
10. SSL: Secure Security Layer (SSL) was predecessor of TLS; it was deprecated around 1990. But often this term is used for TLS implementation with web applications or HTTPS communication.
11. JKS: Java Key Store (JKS) is a proprietary format to store certificates and keys.
12. PKCS: Public Key Cryptography Standards (PKCS) is a format used as key storage and widely used. It is also recommended by OpenSSL forum.

## Implementation
There are few ways by which TLS can be implemented with server and client.

Below are ways by which SSL/TLS can be implemented with server:
1. No SSL/HTTP - This is basic implementation of HTTP protocol where there is no TLS implementation from either side (server/client).
2. SSL (TLS) with Server without Trust Validation - This is the basic implementation of SSL with HTTP sever where a Private key is configured in server which enables HTTPS based communication.
3. SSL (TLS) with Server with Trust Validation - In this implementation, the communication happen on SSL with Client authentication/validation based upon the client certificate. In this configuration, a private key is setup for SSL and a keystore is configured which is used to store trust certificates of client.

Below are ways by which SSL/TLS can be implemented with client:
1. No SSL/HTTP - This is basic implementation of HTTP protocol where there is no TLS implementation from either side (server/client).
2. SSL (TLS) with Server without Trust Validation - When server resource (the URL/endpoint exposed by server which has to be invoked by client) implements the TLS with a private key and hence the client must communicate in encrypted fashion. With this implementation server implements private key at server but does not implement the trust validation at server level and hence only TLS implemented but client does not verified, therefore at client there is no any kind of private key or certificate required at client level, only need to establish connection with SSL enabled mode.
3. SSL (TLS) with Server with Trust Validation - In this implementation, server implements Private Key for TLS and a trust store which contains the clients certificate who is going to consume the server resource. In other words, server will be on SSL and consumer client is validated with trust certificate which has been provided by client and therefore, client requires a private key which public key (X509 certificate) goes with each request to server and establish encrypted connection with certificate validation at server level.
4. Mutual SSL/Server with Trust Validation and Client with Server Identification Validation - This implementation is quite similar to Point 3 with addition that client also contains a trust validation with server. In other words, client additionally validates the server identity with a certificate provided by server. Hence, it is called Mutual SSL and while making call to server, it requires a Private Key for client SSL & identification and Server public certificate to validate the server's identity.

If server implements SSL/TLS as point 3 and client implements SSL/TLS as point 4 then it is called Mutual SSL because in the way SSL/TLS implemented at both Client and Server end.

The Private Key and Public Key (Certificates) can be obtained from the Certificate Authorities. For testing purpose, certificates can also be generated by own, called self-signed certificate.

To generate self-sign certificate, OpenSSL is a popular tool available for all platforms like Windows, Linux. Below are few commands for generating self-sign certificates and private keys:
```
#1. Create Private Key and Certificate for Client

#Step 1: Generate Public Certificate and Private Key
openssl req -x509 -nodes -days 730 -newkey rsa:4096 -keyout client-private-key.pem -out client-certificate.crt -config req.conf -extensions 'v3_req'

#Step 2: Crete PKCS with generated certificate and private key 
openssl pkcs12 -export -out client.p12.jks -inkey client-private-key.pem -in client-certificate.crt

#Step 3: Import Server Certificate into JKS file
keytool -importcert -file server-certificate.crt  -alias servercert -keystore client_truststore.jks

#2. Create Private Key and Certificate for Server

#Step 1: Generate Public Certificate and Private Key
openssl req -x509 -nodes -days 730 -newkey rsa:4096 -keyout server-private-key.pem -out server-certificate.crt -config req.conf -extensions 'v3_req'

#Step 2: Crete PKCS with generated certificate and private key 
openssl pkcs12 -export -out server.p12.jks -inkey server-private-key.pem -in server-certificate.crt

#Step 3: Import Server Certificate into JKS file
keytool -importcert -file client-certificate.crt  -alias clientcert -keystore server_truststore.jks

```

The detailed information available into project src/main/resources directory.


## Use Case
The security with any integration/communication over network is most important factor which must be assessed and handled perfectly. With Hyper Text Transfer Protocol (HTTP) which runs over the seven layer of OSI model where there is a way to provide encryption on OSI's transport layer and hence it is also called Transport Layer Security (TLS). The TLS implementation with HTTP results in a new protocol called Hyper Text Transfer Protocol Secure (HTTPS) and also called HTTP with Secure Socket Layer (SSL).

Since HTTP is based upon request and response therefore bidirectional communication happens and encryption requires at both end and similarly decryption also requires to happen at both end. Therefore, there are multiple ways with implementation with HTTPS which discussed into the Description section.

This project contains both implementations:

SSL/TLS with Server: https://github.com/siddhivinayak-sk/mutual-ssl-server-client/tree/main/mutual-ssl-server

SSL/TLS with Client: https://github.com/siddhivinayak-sk/mutual-ssl-server-client/tree/main/mutual-ssl-client

The both implementation contains all possible implementation of SSL.

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
This utility can be used as library in the domain projects or reference can be taken or can be invoked directly for testing purpose by using below command.

```
java -jar <jar file name>
```

