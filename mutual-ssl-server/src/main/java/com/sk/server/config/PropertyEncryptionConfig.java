package com.sk.server.config;

import java.util.stream.Stream;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sk.server.util.AzureVaultUtils;
import com.sk.server.util.AzureVaultUtils.KeyVaultProperties;

/**
 * Jasypt Encryption configuration class
 * @author Sandeep Kumar
 *
 */
@Configuration
public class PropertyEncryptionConfig {

	private final static String ARG_NAME = "secretCode";
	private final static String ARG_EQ = "=";
	
	/**
	 * Encryption parameters
	 */
	@Value("${encryption.algorithm:PBEWithMD5AndDES}")
	private String algorithm;
	
	@Value("${encryption.key-obtention-iterations:1000}")
	private String KeyObtentionIterations;
	
	@Value("${encryption.pool-size:1}")
	private String poolSize;
	
	@Value("${encryption.provider-name:SunJCE}")
	private String providerName;
	
	@Value("${encryption.salt-generator-class-name:org.jasypt.salt.RandomSaltGenerator}")
	private String saltGeneratorClassName;

	@Value("${encryption.string-output-type:base64}")
	private String stringOutputType;
	
	@Value("${encryption.get-key-online:false}")
	private boolean getKeyOnline;
	
	
	/**
	 * Azure Vault Properties
	 */
	@Value("${azure-keyvault.azure-login-uri:https://login.microsoftonline.com/}")
	private String azureLoginUri;
	
	@Value("${azure-keyvault.scope:https://vault.azure.net}")
	private String scope;
	
	@Value("${azure-keyvault.resource-uri:}")
	private String resourceUri;

	@Value("${azure-keyvault.tenant-id:}")
	private String tenantId;
	
	@Value("${azure-keyvault.client-id:}")
	private String clientId;
	
	@Value("${azure-keyvault.client-key:}")
	private String clientKey;

	@Value("${azure-keyvault.secret-name:}")
	private String secretName;

	@Value("${azure-keyvault.secret-default-value:}")
	private String secretDefaultValue;
	
	@Bean(name = "encryptorBean")
	public StringEncryptor stringEncryptor(ApplicationArguments args) {
		String key = null;
		if(getKeyOnline) {
			
			/**
			 * A Pojo class to pass all required parameter to obtain
			 * entries from Azure KeyVault 
			 */
			KeyVaultProperties properties = new KeyVaultProperties();
			
			/**
			 * Azure login URL - fixed login URL for all
			 */
			properties.setAzureLoginUri(azureLoginUri);
			
			/**
			 * Azure Vault Scope URL for obtaining JWT token - Fixed for all
			 */
			properties.setScope(scope);
			
			/**
			 * Azure Vault URL - Obtained from Azure Portal Vault page
			 */
			properties.setResourceUri(resourceUri);
			
			/**
			 * Tenant ID, also called Directory ID in Azure Portal - Obtain from Vault page
			 */
			properties.setTenantId(tenantId);
			
			/**
			 * Subscribed client id - created under access policy
			 */
			properties.setClientId(clientId);
			
			/**
			 * Key for the client id
			 */
			properties.setClientKey(clientKey);

			/**
			 * Name of Key/Certificate/Secret
			 */
			properties.setSecretName(secretName);

			/**
			 * Custom implementation for providing online/fallback in case of offline and exception
			 * If online true then only it connects to KeyVault and obtain secret
			 * if fallback is true, in case of exception default value will be returned
			 */
			properties.setOnline(true);
			properties.setFallback(true);
			
			/**
			 * Default Value for the secret which will be returned in case of exception
			 */
			properties.setDefaultValue(secretDefaultValue);
			
			/**
			 * Method to obtain value from Azure Vault Secret
			 */
			key = AzureVaultUtils.getSecretFromVault(properties);
		} else {
			String[] arguments = args.getSourceArgs();
			if(null != arguments && arguments.length > 0) {
				key = Stream.of(arguments).filter(e -> e.contains(ARG_NAME+ARG_EQ)).findAny().orElse(ARG_NAME+ARG_EQ);
				key = (null != key)?key.split(ARG_EQ)[1]:null;
			}
		}
	    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
	    SimpleStringPBEConfig config = new SimpleStringPBEConfig();
	    config.setPassword(key);
	    config.setAlgorithm(algorithm);
	    config.setKeyObtentionIterations(KeyObtentionIterations);
	    config.setPoolSize(poolSize);
	    config.setProviderName(providerName);
	    config.setSaltGeneratorClassName(saltGeneratorClassName);
	    config.setStringOutputType(stringOutputType);
	    encryptor.setConfig(config);
	    return encryptor;
	}
	
	
}
