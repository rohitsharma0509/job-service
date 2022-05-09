package com.scb.job.util;

import static com.scb.job.constants.JobConstants.KAFKA_DATE_FORMAT;
import static com.scb.job.util.DateUtils.ISO_DATE_TIME_PATTERN_JODA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.scb.job.constants.JobConstants;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class DateUtilsTest {

  @Test
  public void testConvert() {
    String testDate = "2021-02-25T19:10:20+00:00";
    String actDate = "2021-02-26T02:10:20.000+07:00";
    String result = DateUtils.parseDateTimeInBKK(KAFKA_DATE_FORMAT, testDate);
    assertThat(result).isEqualTo(actDate);
  }

  @Test
  public void testGetCurrentDateTime(){
    DateTimeFormatter fmt = DateTimeFormat.forPattern(ISO_DATE_TIME_PATTERN_JODA);
    String currentDateTime = DateUtils.getCurrentDateTime();
    assertDoesNotThrow(() -> fmt.parseDateTime(currentDateTime));
  }

  @Test
  public void parseToLocalDateTestForNull() {
    LocalDate result = DateUtils.parseToLocalDate(null);
    assertNull(result);
  }

  @Test
  public void parseToLocalDateTestForInvalidInput() {
    LocalDate result = DateUtils.parseToLocalDate("abc");
    assertNull(result);
  }

  @Test
  public void parseToLocalDateTestForValidInput() {
    LocalDate result = DateUtils.parseToLocalDate("2021-10-10");
    assertNotNull(result);
  }

  @Test
  public void convertLocalDateTimeToStringForNull() {
    String result = DateUtils.convertLocalDateTimeToString(null, JobConstants.SEARCH_DATE_FORMAT);
    assertEquals(StringUtils.EMPTY, result);
  }

  @Test
  public void convertLocalDateTimeToStringForValidInput() {
    String result = DateUtils.convertLocalDateTimeToString(LocalDateTime.now(), JobConstants.SEARCH_DATE_FORMAT);
    assertNotNull(result);
  }

  @Test
  public void convertStringToLocalDateTimeForNull() {
    LocalDateTime result = DateUtils.convertStringToLocalDateTime(null, JobConstants.DATE_TIME_FORMAT);
    assertNull(result);
  }

  @Test
  public void convertStringToLocalDateTimeForValidInput() {
    LocalDateTime result = DateUtils.convertStringToLocalDateTime("2021-11-22T04:20:10Z", JobConstants.DATE_TIME_FORMAT);
    assertNotNull(result);
  }

}
