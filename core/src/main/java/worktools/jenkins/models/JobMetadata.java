package worktools.jenkins.models;

import java.io.Serializable;
import java.util.Objects;

public class JobMetadata implements Serializable {

	private static final long serialVersionUID = 7212344173790945779L;
	
	private final JobKey jobKey;
	private final Result result;
	private final String owner;
	private final String gerritComment;
	private final String buildStage;
	private final long buildTime;

	public JobMetadata(JobKey jobKey, String buildStatus, String owner, String gerritComment, String buildStage, long buildTime) {
		this(jobKey, Result.valueOf(buildStatus), owner, gerritComment, buildStage, 0);
	}
	
	public JobMetadata(JobKey jobKey, Result result, String owner, String gerritComment, String buildStage, long duration) {
		this.jobKey = jobKey;
		this.result = result;
		this.owner = owner;
		this.gerritComment = gerritComment;
		this.buildStage = buildStage;
		this.buildTime = duration;
	}
	
	public static JobMetadata empty() {
		return empty(JobKey.empty());
	}
	
	public static JobMetadata empty(JobKey jobKey) {
		return new JobMetadata(jobKey, Result.UNKNOWN, "unknown owner", "no gerrit comments", null, 0);
	}

	public String getJobId() {
		return jobKey.getJobId();
	}
	
	public String getJobCategory() {
		return jobKey.getJobCategory();
	}
	
	public JobKey getJobKey() {
		return jobKey;
	}
	
	public String getBuildStage() {
		return buildStage;
	}

	public Result getResult() {
		return result;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getGerritComment() {
		return gerritComment;
	}
	
	public long getBuildTime() {
		return buildTime;
	}
	
	public enum Result {
		FAILURE,
		SUCCESS,
		RUNNING,
		ABORTED,
		UNSTABLE,
		UNKNOWN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobKey == null) ? 0 : jobKey.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobMetadata that = (JobMetadata) obj;
		
		return result == that.result 
				&& Objects.equals(jobKey, that.jobKey) 
				&& Objects.equals(owner, that.owner)
				&& Objects.equals(buildStage, that.buildStage);
	}

	@Override
	public String toString() {
		return String.format("JobMetadata [jobKey=%s, owner=%s, result=%s, stage=%s]", jobKey, owner, result, buildStage);
	}
}