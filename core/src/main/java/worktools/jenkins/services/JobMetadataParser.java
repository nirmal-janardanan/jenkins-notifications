package worktools.jenkins.services;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;

public interface JobMetadataParser {
	
	JobMetadata parseJobMetadata(JobKey jobKey, String response);

}
