package worktools.jenkins.services;

import java.util.List;
import java.util.Objects;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobSearchKey;

public interface JenkinsDataService {

	JobMetadata fetchJobData(JobKey jobKey);

	List<JobKey> fetchJobs(String project, String jobCategory);
	
	default JobMetadata fetchJob(JobSearchKey jobSearchKey) {
		String owner = jobSearchKey.getOwner();
		String jobCategory = jobSearchKey.getJobCategory();
		List<JobKey> fetchJobs = this.fetchJobs(jobSearchKey.getProject(), jobCategory);
		
		return fetchJobs.stream()
			.map(jobKey -> this.fetchJobData(jobKey))
			.filter(Objects::nonNull)
			.filter(jobMetadata -> owner.equalsIgnoreCase(jobMetadata.getOwner()))
			.findFirst()
			.orElse(JobMetadata.empty());
	}

}