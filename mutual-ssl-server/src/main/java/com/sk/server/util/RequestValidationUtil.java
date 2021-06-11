package com.sk.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.sk.server.exception.InvalidHeaderException;

@Configuration
//@ConditionalOnProperty(value = "security.require-ssl", havingValue = "true", matchIfMissing = true)
public class RequestValidationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidationUtil.class);
	
	public static final String DATE = "Date";
	public static final String DIGEST = "Digest";
	public static final String SIGNATURE = "Signature";
	public static final String KEYSTORE_TYPE = "KEYSTORE_TYPE";
	public static final String KEYSTORE_PATH = "KEYSTORE_PATH";
	public static final String KEYSTORE_SECRET = "KEYSTORE_SECRET";
	public static final String PUBLICKEY_SEQ = "PUBLICKEY_SEQ";	
	public static final String DIGEST_MISMATCH = "Digest mismatch at API";
	public static final String SIGNATURE_BLANK = "Signature Can't be blank";
	public static final String SIGNATURE_MISMATCH = "Signature mismatch at API";

	public static final String POST = "post";
	public static final String GET = "get";

	private static String keystoreType;
	private static String keystorePath;
	private static String keystoreSecret;
	private static String publicKeySequence;
	private static boolean varCustomTrustStoreAuth;
	private static String varCustomTrustStoreType;
	private static Resource varCustomTrustStore;
	private static String varCustomTrustStorePassword;
	private static List<String> varCnUsernames;
	

	private static Map<String, String> keystoreDetails = new HashMap<>();

	@Value("${custom.client-auth:false}")
	private boolean customTrustStoreAuth;

	@Value("${custom.trust-store-type:}")
	private String customTrustStoreType;

	@Value("${custom.trust-store:}")
	private Resource customTrustStore;

	@Value("${custom.trust-store-password:}")
	private String customTrustStorePassword;
	
	@Value("${validation.keystoretype}")
	private String keystoreTypeVal;
	
	@Value("${validation.keystorePath}")
	private String keystorePathVal;

	@Value("${validation.publicKeySequence}")
	private String publicKeySequenceVal;

	@Value("#{'${cn.usernames}'.split(',')}")
	private List<String> cnUsernames;

	@Value("${validation.keystoreSecret}")
	public void setKeystoreSecret(String keystoreSecret) {
		setValues(keystoreSecret, publicKeySequenceVal, keystorePathVal, keystoreTypeVal, customTrustStoreAuth, customTrustStoreType, customTrustStore, customTrustStorePassword, cnUsernames);
	}
	
	private static void setValues(String keystoreSecret, String publicKeySequence, String keystorePath, String keystoreType, boolean customTrustStoreAuth, String customTrustStoreType, Resource customTrustStore, String customTrustStorePassword, List<String> cnUsernames) {
		RequestValidationUtil.keystoreSecret = keystoreSecret;
		RequestValidationUtil.publicKeySequence = publicKeySequence;
		RequestValidationUtil.keystorePath = keystorePath;
		RequestValidationUtil.keystoreType = keystoreType;
		RequestValidationUtil.varCustomTrustStoreAuth = customTrustStoreAuth; 
		RequestValidationUtil.varCustomTrustStoreType = customTrustStoreType;
		RequestValidationUtil.varCustomTrustStore = customTrustStore;
		RequestValidationUtil.varCustomTrustStorePassword = customTrustStorePassword;
		RequestValidationUtil.varCnUsernames = cnUsernames;
	}
	

	public static void parseRequest(HttpServletRequest request, String body) throws InvalidHeaderException {
		LOGGER.info("RequestValidationUtil: parseRequest");

		keystoreDetails.put(KEYSTORE_TYPE, keystoreType);
		keystoreDetails.put(KEYSTORE_PATH, keystorePath);
		keystoreDetails.put(KEYSTORE_SECRET, keystoreSecret);
		keystoreDetails.put(PUBLICKEY_SEQ, publicKeySequence);

		String date = request.getHeader(DATE);
		String digest = request.getHeader(DIGEST);
		String signature = request.getHeader(SIGNATURE);

		// Calculate Digest
		String calculatedDiagest = calculateDiagest(body);
		if (!calculatedDiagest.equals(digest)) {
			throw new InvalidHeaderException(DIGEST_MISMATCH);
		}

		// Signature logic
		if (!Objects.nonNull(signature)) {
			throw new InvalidHeaderException(SIGNATURE_BLANK);
		}

		if (signature.contains(",")) {
			String[] signatureToken = signature.split(",");
			if (signatureToken.length > 3 && signatureToken[3].contains("=")) {
				String obtainedSignature = signatureToken[3].split("=")[1];
				StringBuilder targetUrl = new StringBuilder();
				targetUrl.append(request.getRequestURL());
				targetUrl.append("?");
				targetUrl.append(request.getQueryString());
				Matcher matcher = Pattern.compile("^https?://[^/]+/").matcher(targetUrl);
				if (!verifySignature(keystoreDetails, POST, matcher.replaceAll("/"), date, digest, obtainedSignature)) {
					throw new InvalidHeaderException(SIGNATURE_MISMATCH);
				}
			}
		}

	}

	public static String calculateDiagest(String body) {
		LOGGER.info("RequestValidationUtil: calculateDiagest");

		byte[] sha256Digest = org.apache.commons.codec.digest.DigestUtils.sha256(body);
		String base64sha = Base64.getEncoder().encodeToString(sha256Digest);
		return "SHA-256=" + base64sha;
	}

	public static boolean verifySignature(Map<String, String> keystoreDetails, String method, String targetUrl,
			String remoteDate, String remoteDigest, String remoteSignature) {

		LOGGER.info("RequestValidationUtil: verifySignature");

		try (FileInputStream file = new FileInputStream(keystoreDetails.get(KEYSTORE_PATH))) {
			KeyStore keyStore = KeyStore.getInstance(keystoreDetails.get(KEYSTORE_TYPE));
			keyStore.load(file, keystoreDetails.get(KEYSTORE_SECRET).toCharArray());
			Certificate certificate = keyStore.getCertificate(keystoreDetails.get(PUBLICKEY_SEQ));

			String signHeadersNames = null;
			String signHeadersData = null;
			String date = "date: ";
			if (method.equals(GET)) {
				// Digest Header is not required only for get method
				signHeadersNames = "date (request-target)";
				signHeadersData = date + remoteDate + "\n(request-target): " + method + " " + targetUrl;
			} else {
				signHeadersNames = "date (request-target) digest";
				signHeadersData = date + remoteDate + "\n(request-target): " + method + " " + targetUrl + "\ndigest: "
						+ remoteDigest;
			}
			Signature signatureLib = Signature.getInstance("SHA256withRSA");
			signatureLib.initVerify(certificate.getPublicKey());
			signatureLib.update(signHeadersData.getBytes());
			return signatureLib.verify(Base64.getDecoder().decode(remoteSignature.replace("\"", "")));
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | KeyStoreException
				| CertificateException e) {
			LOGGER.error("Key Verification has failed: {} ", e);
			return false;
		}

	}
	
	

	public static boolean customTrustStoreValidation(HttpServletRequest request) {
		if(!varCustomTrustStoreAuth) {
			return true;
		}
		X509Certificate certs[] = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");
		if(null == certs || certs.length == 0) {
			return false;
		}
		try(InputStream trustInputStream = varCustomTrustStore.getInputStream()) {
			KeyStore trustKeyStore = KeyStore.getInstance(varCustomTrustStoreType);
			trustKeyStore.load(trustInputStream, varCustomTrustStorePassword.toCharArray());
			for(X509Certificate inCert:certs) {
				try {
					String ownCertAlias = trustKeyStore.getCertificateAlias(inCert);
					X509Certificate ownCert = (X509Certificate)trustKeyStore.getCertificate(ownCertAlias);
					boolean issuerDNValidation = ownCert.getIssuerDN().equals(inCert.getIssuerDN());
					boolean certificateEquals = Base64.getEncoder().encodeToString(ownCert.getEncoded()).equals(Base64.getEncoder().encodeToString(inCert.getEncoded()));
					boolean signatureEquals = Base64.getEncoder().encodeToString(ownCert.getSignature()).equals(Base64.getEncoder().encodeToString(inCert.getSignature()));
					boolean cnNameValidation = varCnUsernames.contains(getCNName(inCert));
					inCert.checkValidity();
					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustManagerFactory.init(trustKeyStore);
					for (TrustManager trustManager: trustManagerFactory.getTrustManagers()) {  
					    if (trustManager instanceof X509TrustManager) {  
					        X509TrustManager x509TrustManager = (X509TrustManager)trustManager;  
					        x509TrustManager.checkClientTrusted(certs, "RSA");
					    }  
					}
					if(issuerDNValidation && certificateEquals && signatureEquals && cnNameValidation) {
						return true;
					}
				} catch (Exception e) {
					continue;
				}
			}
			return false;
		} catch (NoSuchAlgorithmException | IOException | KeyStoreException | CertificateException e) {
			return false;
		}
	}
	
	private static String getCNName(X509Certificate cert) throws InvalidNameException {
		String dn = cert.getSubjectX500Principal().getName();
		LdapName ldapDN = new LdapName(dn);
		for(Rdn rdn: ldapDN.getRdns()) {
			if(rdn.getType().equalsIgnoreCase("CN")) {
				return rdn.getValue().toString();
			}
		}
		return null;
	}
}
