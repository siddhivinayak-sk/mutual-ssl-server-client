package com.http.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.http.model.HttpCall;
import com.http.utils.RestUtil;

/**
 * 
 * @author Sandeep Kumar
 *
 */
@Service
public class HttpCallService {

	private static final Logger LOGGER = LogManager.getLogger(HttpCallService.class);
	
	public void makeCall(HttpCall call) {
		String finalURL = null;
		try {
			MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
			if(null != call.getQueryParameters()) {
				call.getQueryParameters().entrySet().stream().forEach(entry -> queryParams.add(entry.getKey(), entry.getValue()));
			}
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(call.getHttpUrl()).queryParams(queryParams);
			URI uri = new URI(builder.toUriString());
			finalURL = uri.toString();
			
			RestTemplate restTemplate = RestUtil.getRestTemplate(
					call.isKeyMaterial(),
					call.getKeyMaterialType(),
					call.getKeyMaterialPath(),
					call.getKeyMaterialSecret(),
					call.isTrustMaterial(),
					call.getTrustMaterialType(),
					call.getTrustMaterialPath(),
					call.getTrustMaterialSecret(),
					call.isDiableCookieManagement(),
					call.isSetCustomConnectionManager()
					);
			
			byte[] data = null;
			if(null != call.getRequestBodyPath() && call.getRequestBodyPath().exists()) {
				try(InputStream fis = call.getRequestBodyPath().getInputStream()) {
					data = new byte[fis.available()];
					fis.read(data);
				}
			}
			MultiValueMap<String, String> headers = new HttpHeaders();
			if(null != call.getHeaders()) {
				call.getHeaders().entrySet().stream().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
			}
			RequestEntity<byte[]> requestEntity = new RequestEntity<>(data, headers, HttpMethod.resolve(call.getHttpMethod().toUpperCase()), uri, byte[].class);
			ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);
			writeResponse(call.getResponseFile(), finalURL, responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCode().value());
		}
		catch(HttpClientErrorException ex) {
			writeResponse(call.getResponseFile(), finalURL, ex.getResponseBodyAsByteArray(), ex.getResponseHeaders(), ex.getRawStatusCode());
			LOGGER.error("EX Response", ex);
		}
		catch(Exception ex) {
			LOGGER.error("Exception:", ex);
		}
	}
	
	private void writeResponse(String responseFile, String url, byte[] body, HttpHeaders httpHeaders, int responseCode) {
		LOGGER.info("URL: {}", url);
		LOGGER.info("Status: {}", responseCode);
		if(null != httpHeaders) {
			LOGGER.info("Response Header: ");
			httpHeaders.entrySet().stream().forEach(entry -> {
				LOGGER.info(entry.getKey() + "{}", entry.getValue().toString());
			});
		}
		if(null != responseFile) {
			try(FileOutputStream fos = new FileOutputStream(responseFile)) {
				fos.write(body);
				fos.flush();
			}
			catch(IOException ex) {
				LOGGER.error("Ex: ", ex);
			}
		}
		else {
			LOGGER.info("Response: {}", new String(body));
		}
	}
}
