package worktools.jenkins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

import worktools.jenkins.utils.Utils;

public class JenkinsDataService {

	private static final int MAX_JOBS = 10;
	private static final String ALL_JOBS_URL = AppConstants.JENKINS_SERVER + "/view/G3/job/%s/api/xml";
	private static final String JOB_URL = AppConstants.JENKINS_SERVER + "/job/%s/%s/api/xml";
	
	public String fetchJobData(String jobId, String jobCategory) {
		try {
			String url = String.format(JOB_URL, jobCategory, jobId);
			Utils.log("Fetching url: " + url);
			
			String data = Utils.fetchHttpData(url);
			writeToDisk(jobId, jobCategory, data);
			return data;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeToDisk(String jobId, String jobCategory, String data) throws IOException {
		String filePath = String.format("%s%s/%s.xml", AppConstants.CACHE_ROOT,jobCategory, jobId);
		
		try {
			Files.write(Paths.get(filePath), data.getBytes());
		} catch (Exception e) {
			// swallow it.
		}
	}

	public List<JobKey> fetchJobs(String jobCategory) {
		List<JobKey> jobs = new ArrayList<>();
		String url = String.format(ALL_JOBS_URL, jobCategory);
		try {
			String jobsResponse = Utils.fetchHttpData(url);
			NodeList jobIdNodes = Utils.extractNodes(jobsResponse, "/workflowJob/build/number");
			for(int i=0; i<jobIdNodes.getLength() && i < MAX_JOBS; i++) {
				String jobId = jobIdNodes.item(i).getTextContent();
				jobs.add(new JobKey(jobId, jobCategory));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return jobs;
	}
}