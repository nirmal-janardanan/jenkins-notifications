package worktools.jenkins;

import java.io.Serializable;
import java.util.Objects;

public class JobKey implements Serializable {
	private static final long serialVersionUID = -8202103744248263793L;
	private String jobId;
	private String jobCategory;

	public JobKey(String jobId, String jobCategory) {
		this.jobId = jobId;
		this.jobCategory = jobCategory;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobCategory() {
		return jobCategory;
	}
	
	@Override
	public String toString() {
		return String.format("JobKey [jobId=%s, jobCategory=%s]", jobId, jobCategory);
	}

	@Override
	public int hashCode() {
		int result = 31 * 1 + ((jobCategory == null) ? 0 : jobCategory.hashCode());
		result = 31 * result + ((jobId == null) ? 0 : jobId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JobKey)) {
			return false;
		}
		JobKey that = (JobKey) obj;
		return Objects.equals(jobId, that.jobId) && Objects.equals(jobCategory, that.jobCategory);
	}
	
	public static void main(String[] args) {
		JobKey jk1 = new JobKey("1", "G3");
		JobKey jk2 = new JobKey("1", "G3");
		JobKey jk3 = new JobKey("1", "G2");
		JobKey jk4 = new JobKey("2", "G3");
		
		System.out.println(jk1.equals(jk2));
		System.out.println(jk1.equals(jk3));
		System.out.println(jk1.equals(jk4));
	}
}