package worktools.jenkins.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobSearchKey;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.services.JenkinsDataService;
import worktools.jenkins.services.JenkinsNotificationService;
import worktools.jenkins.services.NotificationsListener;

public class JenkinsNotificationServiceTest {

	private static final int SLEEP_TIME = 100;
	private static final JobSearchKey JOB_SEARCH_KEY_1 = new JobSearchKey("PROJECT_1", "G3Build-Branch", "developer.one@company.com");
	private static final JobSearchKey JOB_SEARCH_KEY_2 = new JobSearchKey("PROJECT_1", "G3Build-Master", "developer.one@company.com");
	private static final JobMetadata JOB_METADATA_1 = new JobMetadata(new JobKey("PROJECT_1", "Job_ID_1", "G3Build-Branch"), Result.SUCCESS, "developer.one@company.com", "Change_Id_1", null, 0);
	private static final JobMetadata JOB_METADATA_2 = new JobMetadata(new JobKey("PROJECT_1", "Job_ID_2", "G3Build-Master"), Result.SUCCESS, "developer.one@company.com", "Change_Id_2", null, 0);
	
	private final JenkinsDataService dataService = mock(JenkinsDataService.class);
	private final JenkinsNotificationService service = new JenkinsNotificationService(dataService, 1);
	private final NotificationsListener listener = mock(NotificationsListener.class);

	@Before
	public void setup() {
		service.addNotificationsListener(listener);
	}
	
	@After
	public void clearDown() {
		service.stopTracking();
	}
	
	@Test
	public void serviceNotifiesListenersOnTracking() throws InterruptedException {
		when(dataService.fetchJob(JOB_SEARCH_KEY_1)).thenReturn(JOB_METADATA_1);		
		service.track(JOB_SEARCH_KEY_1);
		TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
		verify(listener, atLeastOnce()).onUpdate(JOB_METADATA_1);
	}

	@Test
	public void onceTrackedServiceNotifiesListenersPeriodically() throws InterruptedException {
		when(dataService.fetchJob(JOB_SEARCH_KEY_1)).thenReturn(JobMetadata.empty());
		service.track(JOB_SEARCH_KEY_1);				
		when(dataService.fetchJob(JOB_SEARCH_KEY_1)).thenReturn(JOB_METADATA_1);
		TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
		verify(listener, atLeastOnce()).onUpdate(JOB_METADATA_1);
	}
	
	@Test
	public void serviceTracksNewJobCategory() throws InterruptedException {
		when(dataService.fetchJob(JOB_SEARCH_KEY_1)).thenReturn(JOB_METADATA_1);
		when(dataService.fetchJob(JOB_SEARCH_KEY_2)).thenReturn(JOB_METADATA_2);
		service.track(JOB_SEARCH_KEY_1);
		service.track(JOB_SEARCH_KEY_2);
//		TimeUnit.MILLISECONDS.sleep(100);
		reset(listener);
		TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
		verify(listener, atLeastOnce()).onUpdate(JOB_METADATA_2); // tracks new search key
		verify(listener, never()).onUpdate(JOB_METADATA_1);		  // does not track old search key	
	}

}
