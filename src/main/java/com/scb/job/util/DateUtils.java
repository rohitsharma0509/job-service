package com.scb.job.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
@Slf4j
public class DateUtils {
  public static final String ISO_DATE_TIME_PATTERN_JODA = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

  private static final ZoneId localZoneId = ZoneId.of("Asia/Bangkok");

  private static final ZoneId UTCZoneId = ZoneId.of("UTC");

  public static boolean parseTime(String time){
    try {
      DateTimeFormatter parser = DateTimeFormat.forPattern("HH:mm");
      parser.parseDateTime(time);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
  public static boolean parseDateTime(String dateTime){
    try {
      DateTimeFormatter parser1 = DateTimeFormat.forPattern("yyyy-MM-dd");
      parser1.parseDateTime(dateTime);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
  public static String getCurrentDateTime(){
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ISO_DATE_TIME_PATTERN_JODA);
    return fmt.print(DateTime.now());
  }
  public static String getCurrentDateTimeBKK(){
    SimpleDateFormat bkkZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    bkkZoneFormat.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
    return bkkZoneFormat.format(new Date());
  }
  public static String zonedDateTimeToString(ZonedDateTime zonedDateTime) {
    String PATTERN ="yyyy-MM-dd'T'HH:mm:ssXXX";
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN);
    return zonedDateTime.format(formatter);
  }
  public static String zonedDateTimeThaiToString(ZonedDateTime zonedDateTime) {
    String PATTERN ="yyyy-MM-dd'T'HH:mm:ss";
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN);
    return zonedDateTime.format(formatter);
  }

  public static String parseDateTimeInBKK(String fromFormat, String dateTime){
    SimpleDateFormat kafkaformat = new SimpleDateFormat(fromFormat);
    SimpleDateFormat bkkZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    bkkZoneFormat.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
    Date kreqdate;
    String converDate;
    try {
      kreqdate = kafkaformat.parse(dateTime);
      converDate = bkkZoneFormat.format(kreqdate);
      log.debug("Successfully formatted date.");
    } catch (ParseException e) {
      log.error("Looks like invalid date format.." + dateTime);
      throw new RuntimeException("Invalid Date Format");
    }
    log.debug("Successfully formatted date.");
    return converDate;
  }
  public static String parseDateTimeInBKK(Date date){
    SimpleDateFormat bkkZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    bkkZoneFormat.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
    String converDate = bkkZoneFormat.format(date);
    return converDate;
  }
  public static String convertToThaiTime(String UTCTime){
    LocalDateTime dateTime = LocalDateTime.parse(UTCTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    LocalDateTime thaiTime = dateTime.atZone(UTCZoneId)
            .withZoneSameInstant(localZoneId)
            .toLocalDateTime();
    log.info("Converted UTC time: {} to thai time: {}",UTCTime,thaiTime);
    return thaiTime.toString();
  }
  public static LocalDate parseToLocalDate(String strDate) {
    LocalDate localDate = null;
    try {
      if(StringUtils.isNotBlank(strDate)) {
        localDate = LocalDate.parse(strDate);
      }
    } catch (DateTimeParseException ex) {
      log.error("string is not parsable to LocalDate", ex);
    }
    return localDate;
  }
  public static String convertLocalDateTimeToString(LocalDateTime localDateTime, String dateFormat) {
    if(Objects.isNull(localDateTime)) {
      return StringUtils.EMPTY;
    }
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(dateFormat);
    return localDateTime.format(formatter);
  }

  public static LocalDateTime convertStringToLocalDateTime(String strDateTime, String format) {
    if(StringUtils.isNotBlank(strDateTime)) {
      java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(format);
      return LocalDateTime.parse(strDateTime, dtf);
    }
    return null;
  }
}