package worktools.jenkins.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.models.JobSearchKey;
import worktools.jenkins.services.CachedJenkinsDataService.CachedJobKeys;
import worktools.jenkins.utils.Utils;

public class CachedJenkinsDataServiceTest {

	public static final String TEST_JENKINS_NOTIFICATIONS_JOB_CACHE_FILE = "jobMetadata.cache";
	public static final String TEST_JENKINS_NOTIFICATIONS_JOB_CACHE_DIR = "cache";
	private static final String OWNER_1 = "user-1";
	private static final String OWNER_2 = "user-2";
	private static final String OWNER_3 = "user-3";
	private static final String JOB_ID_1 = "JOB_ID_1";
	private static final String JOB_ID_2 = "JOB_ID_2";
	private static final String JOB_ID_3 = "JOB_ID_3";
	private static final String JOB_CATEGORY = "JOB_CATEGORY";
	private static final JobKey JOB_KEY_1 = new JobKey("PROJECT_1", JOB_ID_1, JOB_CATEGORY);
	private static final JobKey JOB_KEY_2 = new JobKey("PROJECT_1", JOB_ID_2, JOB_CATEGORY);
	private static final JobKey JOB_KEY_3 = new JobKey("PROJECT_1", JOB_ID_3, JOB_CATEGORY);
	private static final JobMetadata SUCCESSFULL_JOB = new JobMetadata(JOB_KEY_1, Result.SUCCESS, OWNER_1, null, null, 0);
	private static final JobMetadata RUNNING_JOB = new JobMetadata(JOB_KEY_2, Result.RUNNING, OWNER_2, null, null, 0);
	private static final JobSearchKey JOB_SEARCH_KEY_1 = new JobSearchKey("PROJECT_1", JOB_CATEGORY, OWNER_1);
	private static final JobMetadata JOB_METADATA_1 = new JobMetadata(JOB_KEY_1, Result.RUNNING, OWNER_1, "No comments", null, 0);
	private static final JobMetadata JOB_METADATA_2 = new JobMetadata(JOB_KEY_2, Result.SUCCESS, OWNER_2, "No comments", null, 0);
	private static final JobMetadata JOB_METADATA_3 = new JobMetadata(JOB_KEY_3, Result.RUNNING, OWNER_3, "No comments", null, 0);
	

	private final JenkinsDataService dataService = mock(JenkinsDataService.class);
	private final CachedJenkinsDataService cachedDataService = new CachedJenkinsDataService(dataService, cacheDirectory());

	private final Map<JobKey, JobMetadata> jobMetadataCache = new HashMap<>();
	private final Map<String, CachedJobKeys> jobKeyCache = new HashMap<>();

	@Before
	public void setup() {
		cachedDataService.setJobMetadataCache(jobMetadataCache);
		cachedDataService.setJobKeyCache(jobKeyCache);
	}

	@Test
	public void fetchesLiveJobMetadataIfNotAvailableInCache() {
		when(dataService.fetchJobData(JOB_KEY_1)).thenReturn(SUCCESSFULL_JOB);
		cachedDataService.fetchJobData(JOB_KEY_1);
		verify(dataService).fetchJobData(JOB_KEY_1);
		assertTrue(jobMetadataCache.containsKey(JOB_KEY_1));
	}

	@Test
	public void fetchesJobMetadataFromCacheIfAvailable() { 
		jobMetadataCache.put(JOB_KEY_1, SUCCESSFULL_JOB);
		assertEquals(SUCCESSFULL_JOB, cachedDataService.fetchJobData(JOB_KEY_1));
		verify(dataService, never()).fetchJobData(JOB_KEY_1);
	}

	@Test
	public void fetchesLiveJobMetadataIfJobIsRunning() {
		jobMetadataCache.put(JOB_KEY_1, RUNNING_JOB);
		when(dataService.fetchJobData(JOB_KEY_1)).thenReturn(RUNNING_JOB);
		assertEquals(RUNNING_JOB, cachedDataService.fetchJobData(JOB_KEY_1));
		verify(dataService).fetchJobData(JOB_KEY_1);
	}

	@Test
	public void fetchesLiveJobsIfNotAvailableInCache() {
		cachedDataService.fetchJobs("PROJECT_1", JOB_CATEGORY);
		verify(dataService).fetchJobs("PROJECT_1", JOB_CATEGORY);
		assertTrue(jobKeyCache.containsKey(JOB_CATEGORY));
	}

	@Test
	public void fetchesCachedJobsIfAvailableInCacheAndNotStale() {
		cachedDataService.setStaleTimeoutInSeconds(1);
		cachedDataService.fetchJobs("PROJECT_1", JOB_CATEGORY);
		verify(dataService).fetchJobs("PROJECT_1", JOB_CATEGORY);
		reset(dataService);

		cachedDataService.fetchJobs("PROJECT_1", JOB_CATEGORY);
		verify(dataService, never()).fetchJobs("PROJECT_1", JOB_CATEGORY);
	}

	@Test
	public void fetchesLiveJobsIfAvailableInCacheAndStale() throws InterruptedException {
		jobKeyCache.put(JOB_CATEGORY, new CachedJobKeys(Arrays.asList(JOB_KEY_1, JOB_KEY_2)));
		TimeUnit.MILLISECONDS.sleep(1);
		cachedDataService.setStaleTimeoutInSeconds(0);
		cachedDataService.fetchJobs("PROJECT_1", JOB_CATEGORY);
		
		verify(dataService).fetchJobs("PROJECT_1", JOB_CATEGORY);
	}

	@Test
	public void fetchesLiveJobFromSearchKeyIfNotAvailableInCache() {
		when(dataService.fetchJobs("PROJECT_1", JOB_CATEGORY)).thenReturn(Arrays.asList(JOB_KEY_1, JOB_KEY_2));
		
		cachedDataService.fetchJob(JOB_SEARCH_KEY_1);
		
		verify(dataService).fetchJobs("PROJECT_1", JOB_SEARCH_KEY_1.getJobCategory());
		verify(dataService).fetchJobData(JOB_KEY_1);
	}
	
	@Test
	public void fetchesCachedJobFromSearchKeyIfJobKeyAndJobMetadataForOwnerIsCached() {
		jobKeyCache.put(JOB_CATEGORY, new CachedJobKeys(Arrays.asList(JOB_KEY_1, JOB_KEY_2)));
		jobMetadataCache.put(JOB_KEY_1, SUCCESSFULL_JOB);
		jobMetadataCache.put(JOB_KEY_2, RUNNING_JOB);
		
		cachedDataService.fetchJob(JOB_SEARCH_KEY_1);
		
		verify(dataService, never()).fetchJob(JOB_SEARCH_KEY_1);
	}
	
	@Test
	public void loadsJobMetadataCacheFromFile() {
		Map<JobKey, JobMetadata> jobMetadataCache = new HashMap<>();
		JobMetadata jobMetadata2 = new JobMetadata(JOB_KEY_1, Result.ABORTED, OWNER_1, null, null, 0);
		jobMetadataCache.put(JOB_KEY_1, jobMetadata2);
		
		writeCache(jobMetadataCache);
		
		CachedJenkinsDataService cachedDataService = new CachedJenkinsDataService(dataService, cacheDirectory());
		
		JobMetadata jobMetadata = cachedDataService.fetchJobData(JOB_KEY_1);
		assertEquals(jobMetadata2, jobMetadata);
	}
	
	@Test
	public void cacheCanBeCleared() {
		jobMetadataCache.put(JOB_KEY_1, SUCCESSFULL_JOB);
		
		cachedDataService.clearCache();
		
		cachedDataService.fetchJobData(JOB_KEY_1);
		verify(dataService).fetchJobData(JOB_KEY_1);
	}

	private void writeCache(Map<JobKey, JobMetadata> jobMetadataCache2) {
		String filePath = cacheDirectory()  + "/jobMetadata.cache";
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
			Utils.log("Flushing jobMap to disk");
			oos.writeObject(jobMetadataCache2);
			oos.flush();
		} catch (IOException e) {
			System.err.println("Could not write to cache file - " + filePath);
			e.printStackTrace();
		}
	}

	public static String cacheDirectory() {
		URL url = Thread.currentThread().getContextClassLoader().getResource(TEST_JENKINS_NOTIFICATIONS_JOB_CACHE_DIR);
		try {
			return new File(url.toURI()).getAbsolutePath();
		} catch (Exception e) {
			throw new RuntimeException("Cache directory: " + TEST_JENKINS_NOTIFICATIONS_JOB_CACHE_DIR + " not found", e);
		}
	}
	
	
	@Test
	public void serviceWorksJustFineIfCacheFileIsMissing() {
		CachedJenkinsDataService cachedDataService = new CachedJenkinsDataService(dataService, cacheDirectory() + "_notExisting");
	}
	
	@Test
	public void skipsLookupForRunningJobsNotTrigerredByJobSearchKeyOwner() {
		
		List<JobKey> jobKeys = Arrays.asList(JOB_KEY_1, JOB_KEY_2, JOB_KEY_3);
		when(dataService.fetchJobs("PROJECT_1", JOB_CATEGORY)).thenReturn(jobKeys);
		when(dataService.fetchJobData(JOB_KEY_1)).thenReturn(JOB_METADATA_1);
		when(dataService.fetchJobData(JOB_KEY_2)).thenReturn(JOB_METADATA_2);
		when(dataService.fetchJobData(JOB_KEY_3)).thenReturn(JOB_METADATA_3);
		
		CachedJenkinsDataService cachedDataService = new CachedJenkinsDataService(dataService, CachedJenkinsDataServiceTest.cacheDirectory());
		cachedDataService.clearCache();
		
		// Attempt once
		JobMetadata jobMetadata = cachedDataService.fetchJob(new JobSearchKey("PROJECT_1", JOB_CATEGORY, OWNER_3));
		assertEquals(JOB_METADATA_3, jobMetadata);
		
		// Attempt once again
		JobMetadata refreshedJobMetadata = cachedDataService.fetchJob(new JobSearchKey("PROJECT_1", JOB_CATEGORY, OWNER_3));
		assertEquals(JOB_METADATA_3, refreshedJobMetadata);
		
		verify(dataService ).fetchJobData(JOB_KEY_1);				// 1 time
		verify(dataService ).fetchJobData(JOB_KEY_2);				// 1 time
		verify(dataService, times(2)).fetchJobData(JOB_KEY_3);		// 2 times
	}	
}

