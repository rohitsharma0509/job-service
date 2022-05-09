package com.scb.job.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.commons.lang3.StringUtils;
import com.scb.job.util.excelhelper.ExcelColumn;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class ExcelJobEntity {
	
	@ExcelColumn(name ="รหัสงาน")
	private String jobId;

	@ExcelColumn(name ="รหัสคำสั่งซื้อ")
	private String orderId;
	
	@ExcelColumn(name ="ราคางาน")
	private Double jobPrice;
	
	@ExcelColumn(name ="สถานะงาน")
	private String jobStatus;
	
	@ExcelColumn(name ="ชื่อไรเดอร์")
	private String driverName;

	@ExcelColumn(name ="พ่อค้า")
	private String merchant;

	@ExcelColumn(name ="ลูกค้า")
	private String customer;

	@ExcelColumn(name ="สร้างขึ้น")
	private String creationDateTime;
	
	@ExcelColumn(name ="อัปเดตล่าสุด")
	private String lastUpdatedDateTime;

	
	
	public static ExcelJobEntity formExcelResponseFromEntity(JobEntity entity) {
		
		return ExcelJobEntity.builder()
				.jobId(entity.getJobId())
				.orderId(entity.getOrderId())
				.jobStatus(entity.getJobStatusEn())
				.jobPrice(entity.getNetPrice())
				.driverName(entity.getDriverName())
				.merchant(entity.getLocationList().get(0).getContactName())
				.customer(entity.getLocationList().get(1).getContactName())
				.creationDateTime(entity.getCreationDateTime()!=null?
				    getFormattedDate(entity.getCreationDateTime()):"")
				.lastUpdatedDateTime(entity.getLastUpdatedDateTime()!=null?
				    getFormattedDate(entity.getLastUpdatedDateTime()):"")
				.build();
	}
	
	private static String getFormattedDate(String date) {
      date = date.replace("Z", "");
      LocalDateTime localDateTime = LocalDateTime.now();
      try {
        localDateTime = !StringUtils.isEmpty(date) ? LocalDateTime.parse(date) : localDateTime;
      } catch (DateTimeParseException ex) {
      }
      ZonedDateTime zonedUTC = localDateTime.atZone(ZoneId.of("UTC"));
      ZonedDateTime zonedBankok = zonedUTC.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
      StringBuilder sb = new StringBuilder();
      sb.append(zonedBankok.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      sb.append("Z");
      return sb.toString();
    }
}