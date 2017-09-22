package worktools.jenkins.models;

import java.util.Objects;

public class JobSearchKey {
	private final String jobCategory;
	private final String owner;
	private final String project;

	public JobSearchKey(String project, String jobCategory, String owner) {
		this.project = project;
		this.jobCategory = jobCategory;
		this.owner = owner;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getJobCategory() {
		return jobCategory;
	}
	
	public String getProject() {
		return project;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobCategory == null) ? 0 : Objects.hashCode(jobCategory));
		result = prime * result + ((owner == null) ? 0 : Objects.hashCode(owner));
		result = prime * result + ((project == null) ? 0 : Objects.hashCode(project));
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
		JobSearchKey that = (JobSearchKey) obj;
		return Objects.equals(project, that.project) 
				&& Objects.equals(jobCategory, that.jobCategory) 
				&& Objects.equals(owner, that.owner);
	}

	@Override
	public String toString() {
		return String.format("%s, %s, %s", project, jobCategory, owner);
	}

}