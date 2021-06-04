package com.http.configs;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.http.model.HttpCall;

/**
 * 
 * @author Sandeep Kumar
 *
 */
@ConfigurationProperties
public class HttpProperties {
	
	private List<HttpCall> calls;

	public List<HttpCall> getCalls() {
		return calls;
	}

	public void setCalls(List<HttpCall> calls) {
		this.calls = calls;
	}

}
