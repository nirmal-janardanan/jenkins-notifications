package worktools.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

import worktools.jenkins.utils.Utils;

public class MockJenkinsDataService extends JenkinsDataService {

	private final String filePath = "C:/Nirmal/code/jenkins/%s/%s.xml";
	private final String jobsFilePath = "C:/Nirmal/code/jenkins/%s/jobs.xml";
	
	@Override
	public String fetchJobData(String jobId, String jobCategory) {
		try {
		    
		    String file = String.format(filePath, jobCategory, jobId);
		    System.out.println("Reading file: " + file);
			return Utils.fetchFileData(file);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<JobKey> fetchJobs(String jobCategory) {
		List<JobKey> jobs = new ArrayList<>();
		try {
			String file = String.format(jobsFilePath, jobCategory);
		    System.out.println("Reading file: " + file);
		    String jobsResponse = Utils.fetchFileData(file);
			NodeList jobIdNodes = Utils.extractNodes(jobsResponse, "/workflowJob/build/number");
			for(int i=0; i<jobIdNodes.getLength() && i < 10; i++) {
				String jobId = jobIdNodes.item(i).getTextContent();
				jobs.add(new JobKey(jobId, jobCategory));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return jobs;
	}
}