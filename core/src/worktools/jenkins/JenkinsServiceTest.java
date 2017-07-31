package worktools.jenkins;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import worktools.jenkins.JobMetadata.Result;

public class JenkinsServiceTest {
	
	JenkinsService jenkinsService;

	@Before
	public void setUp() throws Exception {
		
		jenkinsService = new JenkinsService(new MockJenkinsDataService());
	}

	@Test
	public void testASuccessfullJob() throws IOException, ParserConfigurationException, SAXException {
		String jobId = "14330";
		String jobCategory = "G3Build-Branch";
		JobMetadata jobStatus = jenkinsService.getJobStatus(new JobKey(jobId, jobCategory));
		Result result = jobStatus.getResult();
	    
	    assertEquals(Result.SUCCESS, result);
	    assertEquals("paresh.dahiwal@ideas.com", jobStatus.getOwner());
	}
	
	@Test
	public void testAFailedJob() throws IOException, ParserConfigurationException, SAXException {
		String jobId = "14331";
		String jobCategory = "G3Build-Branch";
		JobMetadata jobStatus = jenkinsService.getJobStatus(new JobKey(jobId, jobCategory));
		Result result = jobStatus.getResult();
	    
	    assertEquals(Result.FAILURE, result);
	    assertEquals("eknath.patil@ideas.com", jobStatus.getOwner());
	}
	
	@Test
	public void testARunningJob() throws IOException, ParserConfigurationException, SAXException {
		String jobId = "14334";
		String jobCategory = "G3Build-Branch";
		JobMetadata jobStatus = jenkinsService.getJobStatus(new JobKey(jobId, jobCategory));
		Result result = jobStatus.getResult();
	    
	    assertEquals(Result.RUNNING, result);
	    assertEquals("eknath.patil@ideas.com", jobStatus.getOwner());
	}
	
	
	@Test
	public void testJobs() {
		String jobCategory = "G3Build-Branch";
		List<JobKey> jobs = jenkinsService.refreshJobsCache(jobCategory);
		assertEquals(10, jobs.size());
	}
	
	@Test
	public void testJobsByOwner() {
		String jobCategory = "G3Build-Branch";
		List<JobKey> jobsCache = jenkinsService.refreshJobsCache(jobCategory);
		System.out.println(jobsCache);
		List<JobMetadata> jobsByOwner = jenkinsService.getJobsByOwner("rahul.chavan@ideas.com");
		assertEquals(3, jobsByOwner.size());
	}
}
