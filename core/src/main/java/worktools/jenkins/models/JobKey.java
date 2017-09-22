package worktools.jenkins.models;

import java.io.Serializable;
import java.util.Objects;

public class JobKey implements Serializable {
	private static final long serialVersionUID = -8202103744248263793L;
	private final String project;
	private final String jobId;
	private final String jobCategory;
	
	private static final JobKey empty = new JobKey("", "unknown jobid", "unknown job category");
	public static JobKey empty() {
		return empty;
	}

	public JobKey(String project, String jobId, String jobCategory) {
		this.project = project;
		this.jobId = jobId;
		this.jobCategory = jobCategory;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobCategory() {
		return jobCategory;
	}
	
	public String getProject() {
		return project;
	}
	
	@Override
	public String toString() {
		return String.format("JobKey [%s, %s, %s]", project, jobCategory, jobId);
	}

	@Override
	public int hashCode() {
		int result = 31 * 1 + Objects.hashCode(jobCategory);
		result = 31 * result + Objects.hashCode(jobId);
		result = 31 * result + Objects.hashCode(project);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JobKey)) {
			return false;
		}
		JobKey that = (JobKey) obj;
		return Objects.equals(jobId, that.jobId)
				&& Objects.equals(jobCategory, that.jobCategory)
				&& Objects.equals(project, that.project);
	}
	
	public static void main(String[] args) {
		JobKey jk1 = new JobKey("PROJECT_1", "1", "PROJECT_1");
		JobKey jk2 = new JobKey("PROJECT_1", "1", "PROJECT_1");
		JobKey jk3 = new JobKey("PROJECT_1", "1", "G2");
		JobKey jk4 = new JobKey("PROJECT_1", "2", "PROJECT_1");
		
		System.out.println(jk1.equals(jk2));
		System.out.println(jk1.equals(jk3));
		System.out.println(jk1.equals(jk4));
	}
}