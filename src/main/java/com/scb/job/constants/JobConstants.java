package com.scb.job.constants;

public class JobConstants {

  private JobConstants(){throw new IllegalStateException("Utility class");}

  public static final String JOB_ID_PREFIX = "S";

  public static final String POINTX_JOB_ID_PREFIX = "P";

  public static final int INITIAL_JOB_STATUS = 1;

  public static final String INITIAL_JOB_STATUS_EN = "New";

  public static final String INITIAL_JOB_STATUS_TH = "\u0E43\u0E2B\u0E21\u0E48";

  public static final boolean DEFAULT_HAVE_RETURN = false;

  public static final int DEFAULT_USER_TYPE = 2;

  public static final int DEFAULT_RATING = 4;

  public static final String JOB_ACC="JOB_ACCEPTED";
  
  public static final String RID_NOT_FOU="RIDER_NOT_FOUND";
  
  public static final String CAL_MER="CALLED_MERCHANT";
  
  public static final String ARR_MER="ARRIVED_AT_MERCHANT";
  
  public static final String MEA_PIC_UP="MEAL_PICKED_UP";
  
  public static final String ARR_CUS_LOC="ARRIVED_AT_CUST_LOCATION";
  
  public static final String FOO_DEL="FOOD_DELIVERED";

  public static final String CANCELLED="ORDER_CANCELLED_BY_OPERATOR";

  public static final String JOB_REPIN_TYPE = "JOB_REPIN";
  
  public static final int FIN_JOB_STA=5;

  public static final String JOB_DESC_THAI_FROM = "\u0E23\u0E31\u0E1A";
  public static final String JOB_DESC_THAI_TO = "\u0E2A\u0E48\u0E07";

  public static final int MERCHANT_SEQUENCE = 1;
  public static final int CUSTOMER_SEQUENCE = 2;
  
  
  public static final String JOB_SEARCH = "/jobSearch";
  
  public static final String RIDER_JOB_DETAILS_DOWNLOAD = "/excel/download";

  public static final String KAFKA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
  public static final String SEARCH_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  public static final String ISO_DATE_FORMAT_MILI = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  public static final String X_USER_ID = "X-User-Id";
  public static final String OPS_MEMBER = "OPS-Member";
  
  public static final Long DEFAULT_REDIS_CACHE_TTL = 600L;

  public static final String VIEW_BY = "viewby";
  public static final String JOB_ID = "jobId";
  public static final String JOB_STATUS = "jobStatus";
  public static final String ORDER_ID = "orderId";
  public static final String RIDER_ID = "riderId";
  public static final String RIDER_NAME = "riderName";
  public static final String DRIVER_NAME = "driverName";
  public static final String PHONE_NUMBER = "phoneNumber";
  public static final String DRIVER_PHONE = "driverPhone";
  public static final String JOB_STATUS_EN = "jobStatusEn";
  public static final String JOB_STATUS_TH = "jobStatusTh";
  public static final String CUSTOMER_NAME = "customerName";
  public static final String MERCHANT_NAME = "merchantName";
  public static final String NET_PRICE = "netPrice";
  public static final String NET_PRICE_SEARCH = "netPriceSearch";
  public static final String OTHER_DEDUCTIONS = "otherDeductions";
  public static final String OTHER_DEDUCTIONS_SEARCH = "otherDeductionsSearch";
  public static final String JOB_TYPE_ENUM = "jobTypeEnum";
  public static final String JOB_TYPE_ENUM_THAI = "jobTypeEnumThai";
  public static final String LAST_UPDATED_DATE = "lastUpdatedDateTime";
  public static final String CREATED_DATE = "creationDateTime";
  public static final String TO_DATE = "toDate";
  public static final String JOB_DATE_FIELD = "jobDateField";
  public static final String FROM_DATE = "fromDate";
  public static final String ALL_JOBS = "alljobs";
  public static final String COMPLETED_JOBS = "completedjobs";
  public static final String RIDER_JOBS = "riderjobs";
  public static final String ACTIVE_JOBS = "activejobs";
  public static final String ASC = "asc";
  public static final String DESC = "desc";
  public static final String JOB_TYPE_MART_NUMBER = "2";
  public static final String JOB_TYPE_EXPRESS_NUMBER = "1";
  public static final String JOB_TYPE_FOOD_NUMBER = "3";
  public static final String EMPTY_STRING = "";
  public static final String JOB_TYPE_POINTX_NUMBER = "4";
  public static final String COMPLETED = "COMPLETED";
  public static final String JOB_CANCELLED = "CANCELLED";
  public static final String FOOD_DELIVERED_TIME = "foodDeliveredTime";
  public static final String ORDER_CANCELLED_BY_OPERATION_TIME = "orderCancelledByOperationTime";
  public static final String JOB_STATUS_KEY = "jobStatusKey";
  public static final String SHOP_LANDMARK_PREFIX = "slm";
}