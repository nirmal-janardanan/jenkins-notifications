package worktools.jenkins.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.NodeList;

import worktools.jenkins.AppConstants;
import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.utils.Utils;

public class JenkinsHttpDataService implements JenkinsDataService {

	private static final int MAX_JOBS = 25;
	private static final String ALL_JOBS_URL = AppConstants.JENKINS_SERVER + "/view/%s/job/%s/api/xml";
	private static final String JOB_URL = AppConstants.JENKINS_SERVER + "/job/%s/%s/api/xml";
	
	
	@Override
	public JobMetadata fetchJobData(JobKey jobKey) {
		try {
			String data = fetchJobData(jobKey.getProject(), jobKey.getJobId(), jobKey.getJobCategory());
			return metadataParser(jobKey).parseJobMetadata(jobKey, data);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private JobMetadataParser metadataParser(JobKey jobKey) {
		List<String> goodBuildCategories = Arrays.asList("G3Build-Branch", "G3Build-Master","NGIBuild-Branch", "NGIBuild-Master");
		if(goodBuildCategories.contains(jobKey.getJobCategory())) {
			return new G3JobMetadataParser();
		} else {
			return new MinimalJobMetadataParser();
		}
	}

	protected String fetchJobData(String project, String jobId, String jobCategory) throws MalformedURLException, IOException {
		String url = String.format(JOB_URL, jobCategory, jobId);
		String data = Utils.fetchHttpData(url);
		return data;
	}

	@Override
	public List<JobKey> fetchJobs(String project, String jobCategory) {
		List<JobKey> jobs = new ArrayList<>();
		String jobsResponse = fetchJobsData(project, jobCategory);
		NodeList jobIdNodes = Utils.extractNodes(jobsResponse, "/workflowJob/build/number");
		for(int i=0; i<jobIdNodes.getLength() && i < MAX_JOBS; i++) {
			String jobId = jobIdNodes.item(i).getTextContent();
			jobs.add(new JobKey(project, jobId, jobCategory));
		}
		return jobs;
	}

	protected String fetchJobsData(String project, String jobCategory) {
		String url = String.format(ALL_JOBS_URL, project, jobCategory);
		
		try {
			return Utils.fetchHttpData(url);
		} catch (IOException e) {
			throw(new RuntimeException("Error reading from " + url, e));
		}
	}
}