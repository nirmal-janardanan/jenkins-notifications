package worktools.jenkins.services;

import worktools.jenkins.models.JobMetadata;

public interface NotificationsListener {
	void onUpdate(JobMetadata jobMetadata);

	default void addNotificationsListener(JenkinsNotificationService jenkinsNotificationService) {
		jenkinsNotificationService.listeners.add(this);
	}
}
