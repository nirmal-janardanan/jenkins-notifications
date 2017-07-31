package worktools.jenkins;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

public class JobMetadata implements Serializable {

	private static final long serialVersionUID = 7212344173790945779L;
	
	private final JobKey jobKey;
	private final Result result;
	private final String owner;
	private final String gerritComment;
	
	public JobMetadata(JobKey jobKey, String buildStatus, String owner, String gerritComment) {
		this(jobKey, Result.valueOf(buildStatus), owner, gerritComment);
	}
	
	public JobMetadata(JobKey jobKey, Result result, String owner, String gerritComment) {
		this.jobKey = jobKey;
		this.result = result;
		this.owner = owner;
		this.gerritComment = gerritComment;
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

	public Result getResult() {
		return result;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getGerritComment() {
		return gerritComment;
	}
	
	public enum Result {
		FAILURE("FAILURE"),
		SUCCESS("SUCCESS"),
		RUNNING("RUNNING"),
		ABORTED("ABORTED"),
		UNSTABLE("UNSTABLE");
		
		private final String name;

		Result(String name) {
			this.name = name;
		}
		
		public static Result of(String name) {
			return Stream.of(Result.values())
				.filter(result -> result.name.equals(name))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid Result type - " + name));
		}
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
		
		return result == that.result && Objects.equals(jobKey, that.jobKey) && Objects.equals(owner, that.owner);
	}

	@Override
	public String toString() {
		return String.format("JobMetadata [jobKey=%s, owner=%s, result=%s]", jobKey, owner, result);
	}
}