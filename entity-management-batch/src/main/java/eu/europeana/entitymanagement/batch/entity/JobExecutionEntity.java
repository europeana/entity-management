package eu.europeana.entitymanagement.batch.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import java.util.Date;

@Entity
public class JobExecutionEntity {

    @Id
    private ObjectId _id;

    private int version;

    private long jobExecutionId;

    private long jobInstanceId;

    private Date startTime;

    private Date endTime;

    private String status;

    private String exitCode;

    private String exitMessage;

    private Date createTime;

    private Date lastUpdated;

    public int getVersion() {
        return version;
    }

    public long getJobExecutionId() {
        return jobExecutionId;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public static JobExecutionEntity toEntity(JobExecution jobExecution) {
        JobExecutionEntity jobExecutionEntity = new JobExecutionEntity();

        jobExecutionEntity.version = jobExecution.getVersion();
        jobExecutionEntity.jobExecutionId = jobExecution.getJobId();
        jobExecutionEntity.startTime = jobExecution.getStartTime();
        jobExecutionEntity.endTime = jobExecution.getEndTime();
        jobExecutionEntity.status = jobExecution.getStatus().toString();
        jobExecutionEntity.exitCode = jobExecution.getExitStatus().getExitCode();
        jobExecutionEntity.exitMessage = jobExecution.getExitStatus().getExitDescription();
        jobExecutionEntity.createTime = jobExecution.getCreateTime();
        jobExecutionEntity.lastUpdated = jobExecution.getLastUpdated();

        return jobExecutionEntity;
    }


    public static JobExecution fromEntity(JobExecutionEntity jobExecutionEntity) {
        if (jobExecutionEntity == null) {
            return null;
        }

        JobExecution jobExecution = new JobExecution(jobExecutionEntity.getJobExecutionId());
        jobExecution.setStartTime(jobExecutionEntity.getStartTime());
        jobExecution.setEndTime(jobExecutionEntity.getEndTime());
        jobExecution.setStatus(BatchStatus.valueOf(jobExecutionEntity.getStatus()));
        jobExecution.setExitStatus(new ExitStatus(jobExecutionEntity.getExitCode()));

        jobExecution.setCreateTime(jobExecutionEntity.getCreateTime());
        jobExecution.setLastUpdated(jobExecutionEntity.getLastUpdated());
        jobExecution.setVersion(jobExecutionEntity.getVersion());

        return jobExecution;
    }
}
