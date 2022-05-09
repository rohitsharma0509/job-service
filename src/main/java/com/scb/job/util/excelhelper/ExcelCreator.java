package com.scb.job.util.excelhelper;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.scb.job.entity.ExcelJobEntity;
import com.scb.job.exception.ResourceNotFoundException;

public class ExcelCreator {

  private ExcelCreator() {}

  public static byte[] excelCreator(List<ExcelJobEntity> jobList, String riderId) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Workbook workbook = new HSSFWorkbook();
      Sheet sheet = workbook.createSheet(riderId);

      PoiPOJOUtils.pojoToSheet(sheet, jobList);
      workbook.write(baos);
      workbook.close();
      return baos.toByteArray();
    } catch (Exception e) {
      throw new ResourceNotFoundException(e.getLocalizedMessage() + "Server Error");
    }


  }

}
