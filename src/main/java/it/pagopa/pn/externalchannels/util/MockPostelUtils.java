package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.NormalizerCallbackRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequestData;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Component
public class MockPostelUtils {

    public String computeSha256(byte[] content) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(content);
            return bytesToBase64(encodedHash);
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot compute sha256", e);
        }
    }

    public static String bytesToBase64(byte[] hash) {
        return Base64Utils.encodeToString(hash);
    }

    public NormalizzazioneResponse getNormalizzazioneOK(String requestId) {
        NormalizzazioneResponse normalizzazioneResponse = new NormalizzazioneResponse();
        normalizzazioneResponse.setRequestId(requestId);
        return normalizzazioneResponse;
    }

    public NormalizzazioneResponse getNormalizzazioneKO(String requestId) {
        NormalizzazioneResponse normalizzazioneResponse = new NormalizzazioneResponse();
        normalizzazioneResponse.setRequestId(requestId);
        normalizzazioneResponse.setError("Error during call activation");
        return normalizzazioneResponse;
    }

    public PreLoadRequestData createPreloadRequest(String sha256) {
        PreLoadRequestData preLoadRequestData = new PreLoadRequestData();
        PreLoadRequest preLoadRequest = new PreLoadRequest();
        preLoadRequest.setContentType("text/csv");
        preLoadRequest.setPreloadIdx(UUID.randomUUID().toString());
        preLoadRequest.setSha256(sha256);
        preLoadRequestData.setPreloads(List.of(preLoadRequest));
        return preLoadRequestData;
    }

    public NormalizerCallbackRequest createNormalizerCallbackRequest(String batchId, String key, String sha256) {
        NormalizerCallbackRequest callbackRequest = new NormalizerCallbackRequest();
        callbackRequest.setRequestId(batchId);
        callbackRequest.setUri(key);
        callbackRequest.setSha256(sha256);
        callbackRequest.setError(null);
        return callbackRequest;
    }
}
