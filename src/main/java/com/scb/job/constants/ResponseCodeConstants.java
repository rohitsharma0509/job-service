package com.scb.job.constants;

public class ResponseCodeConstants {

  private ResponseCodeConstants(){throw new IllegalStateException("Utility class");}

  public static final String SUCCESS = "Success";

  public static final String CREATE_JOB_SUCCESS = "200";

  public static final String CREATE_JOB_FAILED = "100";

  public static final String CREDENTIAL_ERROR = "101";

  public static final String LOCATION_LIST_ERROR = "102";

  public static final String INVALID_REQUEST_PARAMETER = "103";

  public static final String UNEXPECTED_EXCEPTION_ERROR = "104";



}
