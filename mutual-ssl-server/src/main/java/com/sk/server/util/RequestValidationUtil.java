package com.sk.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.sk.server.exception.InvalidHeaderException;

@Configuration
@ConditionalOnProperty(value = "security.require-ssl", havingValue = "true", matchIfMissing = true)
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

	private static Map<String, String> keystoreDetails = new HashMap<>();

	@Value("${validation.keystoretype}")
	private String keystoreTypeVal;
	
	@Value("${validation.keystorePath}")
	private String keystorePathVal;

	@Value("${validation.publicKeySequence}")
	private String publicKeySequenceVal;
	
	@Value("${validation.keystoreSecret}")
	public void setKeystoreSecret(String keystoreSecret) {
		setValues(keystoreSecret, publicKeySequenceVal, keystorePathVal, keystoreTypeVal);
	}
	
	private static void setValues(String keystoreSecret, String publicKeySequence, String keystorePath, String keystoreType) {
		RequestValidationUtil.keystoreSecret = keystoreSecret;
		RequestValidationUtil.publicKeySequence = publicKeySequence;
		RequestValidationUtil.keystorePath = keystorePath;
		RequestValidationUtil.keystoreType = keystoreType;
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

}
