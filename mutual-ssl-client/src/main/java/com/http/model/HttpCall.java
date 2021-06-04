package com.http.model;

import java.util.Map;

import org.springframework.core.io.Resource;

/**
 * 
 * @author Sandeep Kumar
 *
 */
public class HttpCall {

	private String httpUrl;
	private String httpMethod;
	private Map<String, String> headers;
	private Map<String, String> queryParameters;
	private Resource requestBodyPath;
	private String responseFile;
	private boolean isKeyMaterial;
	private String keyMaterialType;
	private Resource keyMaterialPath;
	private String keyMaterialSecret;
	private boolean isTrustMaterial;
	private String trustMaterialType;
	private Resource trustMaterialPath; 
	private String trustMaterialSecret;
	private boolean diableCookieManagement;
	private boolean setCustomConnectionManager;
	
	
	public String getHttpUrl() {
		return httpUrl;
	}
	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	public void setQueryParameters(Map<String, String> queryParameters) {
		this.queryParameters = queryParameters;
	}
	public String getResponseFile() {
		return responseFile;
	}
	public void setResponseFile(String responseFile) {
		this.responseFile = responseFile;
	}
	public boolean isKeyMaterial() {
		return isKeyMaterial;
	}
	public void setKeyMaterial(boolean isKeyMaterial) {
		this.isKeyMaterial = isKeyMaterial;
	}
	public String getKeyMaterialType() {
		return keyMaterialType;
	}
	public void setKeyMaterialType(String keyMaterialType) {
		this.keyMaterialType = keyMaterialType;
	}
	public String getKeyMaterialSecret() {
		return keyMaterialSecret;
	}
	public void setKeyMaterialSecret(String keyMaterialSecret) {
		this.keyMaterialSecret = keyMaterialSecret;
	}
	public boolean isTrustMaterial() {
		return isTrustMaterial;
	}
	public void setTrustMaterial(boolean isTrustMaterial) {
		this.isTrustMaterial = isTrustMaterial;
	}
	public String getTrustMaterialType() {
		return trustMaterialType;
	}
	public void setTrustMaterialType(String trustMaterialType) {
		this.trustMaterialType = trustMaterialType;
	}
	public String getTrustMaterialSecret() {
		return trustMaterialSecret;
	}
	public void setTrustMaterialSecret(String trustMaterialSecret) {
		this.trustMaterialSecret = trustMaterialSecret;
	}
	public boolean isDiableCookieManagement() {
		return diableCookieManagement;
	}
	public void setDiableCookieManagement(boolean diableCookieManagement) {
		this.diableCookieManagement = diableCookieManagement;
	}
	public boolean isSetCustomConnectionManager() {
		return setCustomConnectionManager;
	}
	public void setSetCustomConnectionManager(boolean setCustomConnectionManager) {
		this.setCustomConnectionManager = setCustomConnectionManager;
	}
	public Resource getRequestBodyPath() {
		return requestBodyPath;
	}
	public void setRequestBodyPath(Resource requestBodyPath) {
		this.requestBodyPath = requestBodyPath;
	}
	public Resource getKeyMaterialPath() {
		return keyMaterialPath;
	}
	public void setKeyMaterialPath(Resource keyMaterialPath) {
		this.keyMaterialPath = keyMaterialPath;
	}
	public Resource getTrustMaterialPath() {
		return trustMaterialPath;
	}
	public void setTrustMaterialPath(Resource trustMaterialPath) {
		this.trustMaterialPath = trustMaterialPath;
	}
}
