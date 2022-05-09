package com.scb.job.util.excelhelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellUtil;

public class PoiPOJOUtils {

  public static <T> void pojoToSheet(Sheet sheet, List<T> rows) throws Exception {
    if (rows.size() > 0) {
      Row row = null;
      Cell cell = null;
      int r = 0;
      int c = 0;
      int colCount = 0;
      Map<String, Object> properties = null;
      DataFormat dataFormat = sheet.getWorkbook().createDataFormat();

      Class beanClass = rows.get(0).getClass();

      // header row
      row = sheet.createRow(r++);
      for (Field f : beanClass.getDeclaredFields()) {
        if (!f.isAnnotationPresent(ExcelColumn.class)) {
          continue;
        }
        ExcelColumn ec = f.getAnnotation(ExcelColumn.class);
        cell = row.createCell(c++);
        // // do formatting the header row
        properties = new HashMap<String, Object>();
        properties.put(CellUtil.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);
        properties.put(CellUtil.FILL_FOREGROUND_COLOR, IndexedColors.GREY_25_PERCENT.getIndex());
        CellUtil.setCellStyleProperties(cell, properties);
        cell.setCellValue(ec.name());
      }

      colCount = c;

      // contents
      for (T bean : rows) {
        c = 0;
        row = sheet.createRow(r++);
        for (Field f : beanClass.getDeclaredFields()) {
          cell = row.createCell(c++);
          if (!f.isAnnotationPresent(ExcelColumn.class)) {
            continue;
          }
          ExcelColumn ec = f.getAnnotation(ExcelColumn.class);
          // do number formatting the contents
          String numberFormat = ec.numberFormat();
          properties = new HashMap<String, Object>();
          properties.put(CellUtil.DATA_FORMAT, dataFormat.getFormat(numberFormat));
          CellUtil.setCellStyleProperties(cell, properties);

          f.setAccessible(true);
          Object value = f.get(bean);
          if (value != null) {
            if (value instanceof String) {
              cell.setCellValue((String) value);
            } else if (value instanceof Double) {
              cell.setCellValue((Double) value);
            } else if (value instanceof Integer) {
              cell.setCellValue((Integer) value);
            } else if (value instanceof java.util.Date) {
              cell.setCellValue((java.util.Date) value);
            } else if (value instanceof Boolean) {
              cell.setCellValue((Boolean) value);
            }
          }
        }
      }

      // auto size columns
      for (int col = 0; col < colCount; col++) {
        sheet.autoSizeColumn(col);
      }
    }
  }

}
