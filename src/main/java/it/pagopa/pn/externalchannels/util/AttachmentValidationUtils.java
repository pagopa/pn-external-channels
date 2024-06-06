package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.externalchannels.model.PaperEngageRequestAttachments;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttachmentValidationUtils {

    private AttachmentValidationUtils(){}

    public static final String INVALID_ATTACHMETS_ORDER = "[%s] Invalid first documentType!";
    public static final String INVALID_ATTACHMENT_URI = "[%s] Invalid fileKey!";
    public static final String ATTACHMENT_URI_PREFIX = "safestorage://";

    public static void validateDigitalAttachments(List<String> attachments){
        if(!CollectionUtils.isNullOrEmpty(attachments)){
            validateFirstAttachment(attachments.get(0));

            for(String a : attachments)
                validateUri(a);
        }
    }

    public static void validatePaperAttachments(List<PaperEngageRequestAttachments> attachments){
        if(!CollectionUtils.isNullOrEmpty(attachments)){
            validateFirstAttachment(attachments.get(0).getUri());

            for(PaperEngageRequestAttachments a : attachments){
                validateUri(a.getUri());
            }
        }
    }

    private static void validateFirstAttachment(String uri){
        Pattern pattern = Pattern.compile("^safestorage://([^-]+)");
        Matcher matcher = pattern.matcher(uri);
        if(matcher.find() && !matcher.group(1).equals("PN_AAR"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(INVALID_ATTACHMETS_ORDER, matcher.group(1)));
    }

    private static void validateUri(String uri) {
        if(!uri.startsWith(ATTACHMENT_URI_PREFIX)
                || !uri.endsWith(".pdf")
                || uri.contains("?"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(INVALID_ATTACHMENT_URI, uri));
    }

}
