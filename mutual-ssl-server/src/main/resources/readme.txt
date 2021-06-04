#127.0.0.1       myserver.com
#127.0.0.1       www.myserver.com

Case 1: No SSL - HTTP
Case 2: HTTPS/SSL with Server Identity
Case 3: HTTPS/SSL with Server Identity and Verify Client Trust Identity


#1. Create Private Key and Certificate for Server

#Step 1: Generate Public Certificate and Private Key
openssl req -x509 -nodes -days 730 -newkey rsa:4096 -keyout server-private-key.pem -out server-certificate.crt -config req.conf -extensions 'v3_req'

#Step 2: Crete PKCS with generated certificate and private key 
openssl pkcs12 -export -out server.p12.jks -inkey server-private-key.pem -in server-certificate.crt

#Step 3: Import Server Certificate into JKS file
keytool -importcert -file client-certificate.crt  -alias clientcert -keystore server_truststore.jks



#Import PCKS12 private key into JKS file
keytool -importkeystore -srckeystore cert.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore keystore.jks -deststoretype jks -deststorepass 123456 -destalias own

#Convert JKS to PKCS12 format which is standard format
keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore_p12.jks -deststoretype pkcs12