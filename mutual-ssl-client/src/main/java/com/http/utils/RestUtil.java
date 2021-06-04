package com.http.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Sandeep Kumar
 *
 */
public class RestUtil {

	private static final Logger LOGGER = LogManager.getLogger(RestUtil.class);

	public static HttpComponentsClientHttpRequestFactory getRequestFactory(
			boolean isKeyMaterial,
			String keyMaterialType,
			Resource keyMaterialPath, 
			String keyMaterialSecret,
			boolean isTrustMaterial,
			String trustMaterialType,
			Resource  trustMaterialPath, 
			String trustMaterialSecret,
			boolean diableCookieManagement,
			boolean setCustomConnectionManager
			) {
        try {
        	TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> {
        		if(isTrustMaterial) {
            		try(InputStream trustInputStream = trustMaterialPath.getInputStream()) {
            			KeyStore trustKeyStore = KeyStore.getInstance(trustMaterialType);
            			trustKeyStore.load(trustInputStream, trustMaterialSecret.toCharArray());
            			Enumeration<String> aliasList = trustKeyStore.aliases();
            			while(aliasList.hasMoreElements()) {
            				String alias = aliasList.nextElement();
            				if(trustKeyStore.isCertificateEntry(alias)) {
            					Certificate certificate = trustKeyStore.getCertificate(alias);
            					for(X509Certificate inCert:chain) {
            						try {
            							certificate.verify(inCert.getPublicKey());
            							return true;
            						}
            						catch(Exception ex) {
            							continue;
            						}
            					}
            					
            				}
            			}
            			return false;
            		} catch (NoSuchAlgorithmException | IOException | KeyStoreException e) {
            			return false;
					}
        		}
        		else {
        			return true;        			
        		}
        	};
        	SSLContext sslContext = null;
        	if(isKeyMaterial && isTrustMaterial) {
        		try(InputStream keyInputStream = keyMaterialPath.getInputStream(); 
        				InputStream trustInputStream = trustMaterialPath.getInputStream()) {
        			KeyStore keyKeyStore = KeyStore.getInstance(keyMaterialType);
        			keyKeyStore.load(keyInputStream, keyMaterialSecret.toCharArray());
        			KeyStore trustKeyStore = KeyStore.getInstance(trustMaterialType);
        			trustKeyStore.load(trustInputStream, trustMaterialSecret.toCharArray());

        	        sslContext = SSLContexts
        	        		.custom()
        	        		.loadKeyMaterial(keyKeyStore, keyMaterialSecret.toCharArray())
                            .loadTrustMaterial(trustKeyStore, acceptingTrustStrategy)
                            .build();
        		}
        	}
        	else if(isKeyMaterial && !isTrustMaterial) {
        		try(InputStream keyInputStream = keyMaterialPath.getInputStream()) {
        			KeyStore keyKeyStore = KeyStore.getInstance(keyMaterialType);
        			keyKeyStore.load(keyInputStream, keyMaterialSecret.toCharArray());

        	        sslContext = SSLContexts
        	        		.custom()
        	        		.loadKeyMaterial(keyKeyStore, keyMaterialSecret.toCharArray())
                            .loadTrustMaterial(null, acceptingTrustStrategy)
                            .build();
        		}
        	}
        	else if(!isKeyMaterial && isTrustMaterial) {
        		try(InputStream trustInputStream = trustMaterialPath.getInputStream()) {
        			KeyStore trustKeyStore = KeyStore.getInstance(trustMaterialType);
        			trustKeyStore.load(trustInputStream, trustMaterialSecret.toCharArray());

        	        sslContext = SSLContexts
        	        		.custom()
                            .loadTrustMaterial(trustKeyStore, acceptingTrustStrategy)
                            .build();
        		}
        	}
        	else {
    	        sslContext = SSLContexts
    	        		.custom()
                        .loadTrustMaterial(null, acceptingTrustStrategy)
                        .build();
        	}

        	SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        	PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

        	Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				  .register("https", csf)
				  .register("http", plainConnectionSocketFactory)
				  .build();
        	BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        	
        	CloseableHttpClient httpClient = null;
        	if(diableCookieManagement && setCustomConnectionManager) {
            	httpClient = HttpClients
            			.custom()
            			.disableDefaultUserAgent()
            			.disableCookieManagement()
                        .setSSLSocketFactory(csf)
                        .setConnectionManager(connectionManager)
                        .build();
        		
        	}
        	else if(diableCookieManagement && !setCustomConnectionManager) {
            	httpClient = HttpClients
            			.custom()
            			.disableDefaultUserAgent()
            			.disableCookieManagement()
                        .setSSLSocketFactory(csf)
                        .build();
        	}
        	else if(!diableCookieManagement && setCustomConnectionManager) {
            	httpClient = HttpClients
            			.custom()
            			.disableDefaultUserAgent()
                        .setSSLSocketFactory(csf)
                        .setConnectionManager(connectionManager)
                        .build();
        	}
        	else {
            	httpClient = HttpClients
            			.custom()
            			.disableDefaultUserAgent()
                        .setSSLSocketFactory(csf)
                        .build();
        	}

        	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return requestFactory;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException e) {
        	LOGGER.error("Invalid Details: ", e);
        }
        return null;
	}
	
	public static RestTemplate getRestTemplate(
			boolean isKeyMaterial,
			String keyMaterialType,
			Resource keyMaterialPath, 
			String keyMaterialSecret,
			boolean isTrustMaterial,
			String trustMaterialType,
			Resource trustMaterialPath, 
			String trustMaterialSecret,
			boolean diableCookieManagement,
			boolean setCustomConnectionManager
			) {
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = getRequestFactory(
				isKeyMaterial,
				keyMaterialType,
				keyMaterialPath, 
				keyMaterialSecret,
				isTrustMaterial,
				trustMaterialType,
				trustMaterialPath, 
				trustMaterialSecret,
				diableCookieManagement,
				setCustomConnectionManager
				);
		return new RestTemplate(httpComponentsClientHttpRequestFactory);
	}
		
}
