/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 *
 * @author GIANGR40
 */
@Slf4j
public class Util {

    public static LocalDateTime now() {
        Instant instant = Instant.now();
        ZoneId idz = ZoneId.of("Europe/Rome");
        ZonedDateTime romeZoned = instant.atZone(idz);
        return romeZoned.toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        Date date = null;
        if (localDateTime != null) {
            date
                    = java.sql.Timestamp.valueOf(localDateTime);
        } // if
        return date;
    }

    public static Date addMillisecondsToDate(Date date, Integer milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, milliseconds);
        return cal.getTime();
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar xmldate = null;
        if (date != null) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(date);
            try {
                xmldate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(gc);
            } // try
            catch (Exception ex) {
                log.error("unable to convert {} to XMLGregorianCalendar", date.toString());
            } // catch
        } // if
        return xmldate;
    }

    public static String dateToString(Date date) {
        DateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        String strdate = dateformat.format(date);
        return strdate;
    }

    public static Date XMLGregorianCalendarToDate(XMLGregorianCalendar xmlDate) {
        Date date = null;
        if (xmlDate != null) {
            GregorianCalendar gc = xmlDate.toGregorianCalendar();
            date = gc.getTime();
        } // if        
        return date;
    }

    public static Date removeMilliseconds(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);            
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } else {
            return date;
        }
    }

    public static Throwable findRouteCauseUsing(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getXmlTagValue(String xmlUrl, String tagXpath) {
        try {
            URL url = new URL(xmlUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "text/xml");

            forceConnectionTrust(conn); // TODO: forzatura da levare dovuta a certificato scaduto url metadata

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // SONAR disable protocols by external entities
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate(tagXpath + "/text()", doc, XPathConstants.NODE);

            return node.getNodeValue();
        } catch (Exception e) {
            return null;
        }
    }
    private static void forceConnectionTrust(HttpsURLConnection conn) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        conn.setHostnameVerifier((hostname, session) -> true);
        conn.setSSLSocketFactory(sc.getSocketFactory());
    }

    public static List<byte[]> splitByteArray(byte[] source, int chunksize) {
        if(chunksize == 0)
            return Collections.singletonList(source);
        List<byte[]> result = new ArrayList<>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }

}
