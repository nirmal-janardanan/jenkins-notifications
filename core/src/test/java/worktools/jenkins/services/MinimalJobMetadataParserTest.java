package worktools.jenkins.services;

import org.junit.Test;
import static org.junit.Assert.*;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.utils.Utils;

public class MinimalJobMetadataParserTest {

	private final MinimalJobMetadataParser jobParser = new MinimalJobMetadataParser();
	
	@Test
	public void testG3PromotionBuild() throws Exception {
		String jobData = Utils.fetchClasspathFileData("G3Build-Promotion/job.xml");
		JobKey jobKey = new JobKey("PROJECT_1", "1244", "G3Build-Promotion");
		JobMetadata jobMetadata = jobParser.parseJobMetadata(jobKey, jobData);
		assertEquals("JobKey does not match", jobKey, jobMetadata.getJobKey());
		assertEquals("Result does not match", Result.SUCCESS, jobMetadata.getResult());
		assertEquals("Owner does not match", "idnasi", jobMetadata.getOwner());
	}
	
	@Test
	public void testG3BuildDeployG3() throws Exception {
		String jobData = Utils.fetchClasspathFileData("G3Build-DeployG3/job.xml");
		JobKey jobKey = new JobKey("PROJECT_1", "16", "G3Build-DeployG3");
		JobMetadata jobMetadata = jobParser.parseJobMetadata(jobKey, jobData);
		assertEquals("JobKey does not match", jobKey, jobMetadata.getJobKey());
		assertEquals("Result does not match", Result.SUCCESS, jobMetadata.getResult());
		assertEquals("Owner does not match", "developer.three@company.com", jobMetadata.getOwner());
	}

}
