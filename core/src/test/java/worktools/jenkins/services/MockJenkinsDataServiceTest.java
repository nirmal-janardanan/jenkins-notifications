package worktools.jenkins.services;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;

public class MockJenkinsDataServiceTest {

	private final MockJenkinsDataService dataService = new MockJenkinsDataService();

	@Test
	public void parsesJobMetadata() {
		{
			JobKey jobKey = new JobKey("PROJECT_1", "14330", "G3Build-Branch");
			assertJobMetadata(jobKey, new JobMetadata(jobKey, Result.SUCCESS, "developer.one@company.com", "Story-6", "G3Test", 0));
		}
		
		{
			JobKey jobKey = new JobKey("PROJECT_1", "14671", "G3Build-Branch");
			assertJobMetadata(jobKey, new JobMetadata(jobKey, Result.SUCCESS, "developer.four@company.com", "Story-2", "G3Deploy", 0));
		}
	}

	private void assertJobMetadata(JobKey jobKey, JobMetadata expectedMetadata) {
		JobMetadata jobMetadata = dataService.fetchJobData(jobKey);
		assertEquals(expectedMetadata, jobMetadata);
	}

}
