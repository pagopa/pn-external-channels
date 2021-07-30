/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.poste.sin.msttservicehandler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.extern.slf4j.Slf4j;

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

}
