package worktools.jenkins.services;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobSearchKey;
import worktools.jenkins.utils.Utils;

public class JenkinsNotificationService {

	private final JenkinsDataService dataService;
	private final Set<NotificationsListener> listeners = new CopyOnWriteArraySet<>();
	
	private final ScheduledExecutorService scheduler;
	
	private final JobPolling jobPolling;

	public JenkinsNotificationService(JenkinsDataService dataService, int pollingIntervalInMilliSeconds) {
		this.dataService = dataService;
		scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		this.jobPolling = new JobPolling(scheduler, pollingIntervalInMilliSeconds, this);
		
		registerShutdownHook();
	}

	public void trackAsync(JobSearchKey jobSearchKey) {
		jobPolling.trackAsync(jobSearchKey);
	}

	public void track(JobSearchKey jobSearchKey) {
		jobPolling.track(jobSearchKey);
	}

	private JobMetadata fetch(JobSearchKey jobSearchKey) {
		Utils.log("Fetching details for: " + jobSearchKey);
		JobMetadata jobMetadata = dataService.fetchJob(jobSearchKey);
		return jobMetadata;
	}

	public void addNotificationsListener(NotificationsListener listener) {
		listeners.add(listener);
	}
	
	private void notifyListeners(JobMetadata jobMetadata) {
		for(NotificationsListener listener : listeners) {
			listener.onUpdate(jobMetadata);
		}
	}
	
	public void stopTracking() {
		jobPolling.stopTracking();
	}
	
	private void registerShutdownHook() {
		Thread shutdownSchedulerTask = new Thread(() -> this.scheduler.shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownSchedulerTask);
	}
	
	static class JobPolling {
		
		private final ScheduledExecutorService scheduler;
		private final int pollingIntervalInMilliSeconds;
		private ScheduledFuture<?> pollingTask;
		private final JenkinsNotificationService notificationService;
		private JobSearchKey trackedJobSearchKey;
		public JobPolling(ScheduledExecutorService scheduler, int pollingIntervalInMilliSeconds, JenkinsNotificationService notificationService) {
			this.scheduler = scheduler;
			this.pollingIntervalInMilliSeconds = pollingIntervalInMilliSeconds;
			this.notificationService = notificationService;
		}

		void poll(JobSearchKey jobSearchKey) {
			if(Objects.equals(trackedJobSearchKey, jobSearchKey)) {
				return;
			}
			
			if(pollingTask != null) {
				pollingTask.cancel(true);
			}
			
			pollingTask = scheduler.scheduleAtFixedRate(() -> fetchJobAndNotify(jobSearchKey) , pollingIntervalInMilliSeconds, pollingIntervalInMilliSeconds, TimeUnit.MILLISECONDS);
			trackedJobSearchKey = jobSearchKey;
		}
		
		public void trackAsync(JobSearchKey jobSearchKey) {
			scheduler.execute(() -> track(jobSearchKey));
		}

		public void track(JobSearchKey jobSearchKey) {
			fetchJobAndNotify(jobSearchKey);
			poll(jobSearchKey);
		}
		
		private void fetchJobAndNotify(JobSearchKey jobSearchKey) {
			Utils.log("Fetching details for: " + jobSearchKey);
			try {
				JobMetadata jobMetadata = notificationService.fetch(jobSearchKey);
				notificationService.notifyListeners(jobMetadata);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void stopTracking() {
			pollingTask.cancel(true);
		}
	}
}
