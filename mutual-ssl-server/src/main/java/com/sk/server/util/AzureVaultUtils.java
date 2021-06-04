package com.sk.server.util;

import java.net.MalformedURLException;
import java.security.KeyPair;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.SecretBundle;

/**
 * Utilty class for connecting azure vault and getting secret/certificate/key from vault
 * @author Sandeep Kumar
 *
 */
public class AzureVaultUtils {

	private static final Logger logger = Logger.getLogger(AzureVaultUtils.class.getName());
	
	/**
	 * This method takes input parameter to connect to Azure Vault and obtain specified secret
	 * @param properties Input parameter to connect with Azure Vault 
	 * @return Returns obtained secret entry from vault
	 */
	public static String getSecretFromVault(KeyVaultProperties properties) {
		String retVal = null;
		if(properties.isOnline()) {
			try {
				KeyVaultClient keyVaultClient = getKeyVaultClient(
						properties.getTenantId(), 
						properties.getAzureLoginUri(), 
						properties.getScope(), 
						properties.getClientId(), 
						properties.getClientKey());
				retVal = getSecretFromVault(keyVaultClient, properties.getResourceUri(), properties.getSecretName());
			} catch(Exception ex) {
				if(properties.isFallback()) {
					logger.info("Default value picked up");
					return properties.getDefaultValue();
				}
			}
		}
		else {
			if(properties.isFallback()) {
				logger.info("Default value picked up");
				return properties.getDefaultValue();
			}
		}
		return retVal;
	}

	/**
	 * This method creates the vault client and obatins specified key from Azure Vault
	 * @param properties Input parameters for obtaining 
	 * @return Returns KeyPair from the Azure Vault which further can be used to obtain Public and Private Key
	 */
	public static KeyPair getKeyFromVault(KeyVaultProperties properties) {
		KeyVaultClient keyVaultClient = getKeyVaultClient(
				properties.getTenantId(), 
				properties.getAzureLoginUri(), 
				properties.getScope(), 
				properties.getClientId(), 
				properties.getClientKey());
		KeyBundle keyBundle = getKeyFromVault(keyVaultClient, properties.getResourceUri(), properties.getSecretName());
		/*
		 * There are three mechanism - based upon saved key in vault
		 * toAes() - Get secret key for AES based encryption
		 * toEC() - Get key pair based up EC
		 * toRSA() - Gets Key pair based upon RSA
		 * 
		 * In case of KeyPair, obtain keyPair.getPublicKey() and keyPair.getPrivateKey()
		 */
		return keyBundle.key().toRSA();
	}
	
	/**
	 * This method obtains certificate from Azure Vault
	 * @param properties Provide input parameters to connect with Vault 
	 * @return Returns Certificate obtained from vault
	 * @throws CertificateException Throws in case of any exception 
	 */
	public static X509Certificate getCertificateFromVault(KeyVaultProperties properties) throws CertificateException {
		KeyVaultClient keyVaultClient = getKeyVaultClient(
				properties.getTenantId(), 
				properties.getAzureLoginUri(), 
				properties.getScope(), 
				properties.getClientId(), 
				properties.getClientKey());
		CertificateBundle certificateBundle = getCertificateFromVault(keyVaultClient, properties.getResourceUri(), properties.getSecretName());
		return X509Certificate.getInstance(certificateBundle.cer()); 
	}
	
	/**
	 * Define Executor service which will be utilized to make rest calls
	 */
	private static final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	/**
	 * This method will create KeyValutClient by Authentication form microsoft
	 * @param tenantId - It is tenant ID for Key vault in Azure. Also called Directory ID 
	 * @param azureLoginUri - Azure login to obtain token to open vault - Remain same for all
	 * @param scopeUri - A vault scope URL - Remain same for all
	 * @param clientId - A client mapped with Vault to access the keys from vault
	 * @param clientKey - Key for the client to access vault
	 * @return Returns the KeyVaultClient object which further used to obtain the keys 
	 */
	private static KeyVaultClient getKeyVaultClient(
			String tenantId, 
			String azureLoginUri, 
			String scopeUri, 
			String clientId, 
			String clientKey) {
		return new KeyVaultClient(new KeyVaultCredentials() {
			@Override
			public String doAuthenticate(String authorization, String resource, String scope) {
				String accessToken = null;
				try {
					AuthenticationContext context = new AuthenticationContext(azureLoginUri + tenantId, false, service);
					ClientCredential credential = new ClientCredential(clientId, clientKey);
					Future<AuthenticationResult> future = context.acquireToken(scopeUri, credential, null);
					AuthenticationResult token = future.get();
					accessToken = token.getAccessToken();
				} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
					logger.log(Level.SEVERE, "Error while getting token", ex);
				}
				return accessToken;
			}			
		});
	}

	/**
	 * Obtain secret from Vault from the passed parameter 
	 * @param keyVaultClient It takes KeyVaultClient object as input
	 * @param vaultBaseUrl Base url of vault obtained from Azure portal
	 * @param secretName Secret name for which value has to be obtained
	 * @return Returns obtained secret value
	 */
	private static String getSecretFromVault(KeyVaultClient keyVaultClient, String vaultBaseUrl, String secretName) {
		SecretBundle secretBundle = keyVaultClient.getSecret(vaultBaseUrl, secretName);
		return secretBundle.value();
	}

	/**
	 * Obtain the certificate bundle object from vault
	 * @param keyVaultClient Input parameter KeyVaultClient object
	 * @param vaultBaseUrl Vault Base URL obtained from Azure portal
	 * @param certificateName Name of certificate entry into the Vault
	 * @return Returns the CertificateBundle object which further used to obtain the X509 certificate 
	 */
	private static CertificateBundle getCertificateFromVault(KeyVaultClient keyVaultClient, String vaultBaseUrl, String certificateName) {
		return keyVaultClient.getCertificate(vaultBaseUrl, certificateName);
	}

	/**
	 * This method obtain Key stored into Azure Vault based upon input parameters 
	 * @param keyVaultClient It takes the KeyVaultClient Object
	 * @param vaultBaseUrl Vault URL obtained from Azure Portal
	 * @param keyName Key name which need to load 
	 * @return Returns the KeyBundle object obtained from Vault
	 */
	private static KeyBundle getKeyFromVault(KeyVaultClient keyVaultClient, String vaultBaseUrl, String keyName) {
		return keyVaultClient.getKey(vaultBaseUrl, keyName);
	}
	
	/**
	 * POJO Class to pass parameters into utility method
	 * @author Sandeep Kumar
	 *
	 */
	public static class KeyVaultProperties {
		private String azureLoginUri;
		private String tenantId;
		private String resourceUri;
		private String scope;
		private String clientId;
		private String clientKey;
		private String secretName;
		private boolean online;
		private boolean fallback;
		private String defaultValue;

		public KeyVaultProperties() {
			this.azureLoginUri = "https://login.microsoftonline.com/";
			this.scope = "https://vault.azure.net";
			this.online = false;
			this.fallback = true;
		}
		
		public String getAzureLoginUri() {
			return azureLoginUri;
		}
		public void setAzureLoginUri(String azureLoginUri) {
			this.azureLoginUri = azureLoginUri;
		}
		public String getTenantId() {
			return tenantId;
		}
		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}
		public String getResourceUri() {
			return resourceUri;
		}
		public void setResourceUri(String resourceUri) {
			this.resourceUri = resourceUri;
		}
		public String getScope() {
			return scope;
		}
		public void setScope(String scope) {
			this.scope = scope;
		}
		public String getClientId() {
			return clientId;
		}
		public void setClientId(String clientId) {
			this.clientId = clientId;
		}
		public String getClientKey() {
			return clientKey;
		}
		public void setClientKey(String clientKey) {
			this.clientKey = clientKey;
		}
		public String getSecretName() {
			return secretName;
		}
		public void setSecretName(String secretName) {
			this.secretName = secretName;
		}
		public boolean isOnline() {
			return online;
		}
		public void setOnline(boolean online) {
			this.online = online;
		}
		public boolean isFallback() {
			return fallback;
		}
		public void setFallback(boolean fallback) {
			this.fallback = fallback;
		}
		public String getDefaultValue() {
			return defaultValue;
		}
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}
}
