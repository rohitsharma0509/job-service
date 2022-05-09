package com.scb.job.model.response;

import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.JobEntity;
import com.scb.job.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class JobReconciliationResponse {
    private String jobId;
    private String orderId;
    private String riderId;
    private String riderName;
    private BigDecimal jobPrice;
    private BigDecimal netPrice;
    private String jobStatus;
    private String accountNumber;
    private String jobType;
    private BigDecimal taxAmount;
    private Double ewtAmount;
    private LocalDateTime jobStartDateTime;
    private LocalDateTime jobEndDateTime;

    public static List<JobReconciliationResponse> of(List<JobEntity> jobEntities) {
        List<JobReconciliationResponse> list = new ArrayList<>();
        if(!CollectionUtils.isEmpty(jobEntities)) {
            jobEntities.stream().forEach(jobEntity -> list.add(of(jobEntity)));
        }
        return list;
    }

    public static JobReconciliationResponse of(JobEntity jobEntity) {
        String status = jobEntity.getJobStatusKey().equals(JobStatus.FOOD_DELIVERED.name()) ? JobConstants.COMPLETED : JobConstants.JOB_CANCELLED;
        LocalDateTime startDate = DateUtils.convertStringToLocalDateTime(jobEntity.getJobAcceptedTime(), JobConstants.DATE_TIME_FORMAT);
        LocalDateTime endDate = jobEntity.getJobStatusKey().equals(JobStatus.FOOD_DELIVERED.name()) ?
                DateUtils.convertStringToLocalDateTime(jobEntity.getFoodDeliveredTime(), JobConstants.DATE_TIME_FORMAT) :
                DateUtils.convertStringToLocalDateTime(jobEntity.getOrderCancelledByOperationTime(), JobConstants.DATE_TIME_FORMAT);
        double ewtAmount = Objects.nonNull(jobEntity.getExcessiveWaitTimeDetailsEntity()) && Objects.nonNull(jobEntity.getExcessiveWaitTimeDetailsEntity().getExcessiveWaitTopupAmount())
                ? jobEntity.getExcessiveWaitTimeDetailsEntity().getExcessiveWaitTopupAmount() : 0.0;
        JobReconciliationResponse job = JobReconciliationResponse.builder()
                .jobId(jobEntity.getJobId())
                .orderId(jobEntity.getOrderId())
                .riderId(jobEntity.getRiderId())
                .riderName(jobEntity.getDriverName())
                .jobPrice(BigDecimal.valueOf(jobEntity.getNetPrice()))
                .netPrice(BigDecimal.valueOf(jobEntity.getNetPaymentPrice()))
                .jobStatus(status)
                .jobType(jobEntity.getJobType())
                .taxAmount(BigDecimal.valueOf(jobEntity.getTaxAmount()))
                .jobStartDateTime(startDate)
                .jobEndDateTime(endDate)
                .ewtAmount(ewtAmount)
                .build();
        return job;
    }
}
