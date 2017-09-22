package worktools.jenkins.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import worktools.jenkins.services.JenkinsHttpDataService;
import worktools.jenkins.utils.Utils;


public class MockJenkinsDataService extends JenkinsHttpDataService {
	private static final String filePath = "%s/%s.xml";
	private static final String jobsFilePath = "%s/jobs.xml";
	
	@Override
	protected String fetchJobData(String project, String jobId, String jobCategory) throws MalformedURLException, IOException {
		String file = String.format(filePath, jobCategory, jobId);
		long start = System.currentTimeMillis();
		try {
		    
		    Utils.log("Reading file: " + file);
			return Utils.fetchClasspathFileData(file);
		} catch(Exception e) {
			throw new RuntimeException("Error reading from: " + file, e);
		} finally {
			 Utils.log("Reading took: " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	@Override
	protected String fetchJobsData(String project, String jobCategory) {
		String file = String.format(jobsFilePath, jobCategory);
		try {
			Utils.log("Reading file: " + file);
		    return Utils.fetchClasspathFileData(file);
		} catch (IOException | URISyntaxException e) {
			throw(new RuntimeException("Error reading from " + file, e));
		}
	}
}