package worktools.jenkins.models;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import worktools.jenkins.ui.JenkinsNotificationsSystemTray;

public class Settings {
	private final JobSearchKey jobSearchKey;
	public Settings(String project, String owner, String jobCategory) {
		this.jobSearchKey = new JobSearchKey(project, jobCategory, owner);
	}

	public String getJobCategory() {
		return this.jobSearchKey.getJobCategory();
	}
	
	public String getOwner() {
		return this.jobSearchKey.getOwner();
	}
	
	public String getProject() {
		return this.jobSearchKey.getProject();
	}
	
	public JobSearchKey toJobSearchKey() {
		return this.jobSearchKey;
	}
	
	public static Settings load() {
		Preferences preferences = Preferences.userNodeForPackage(JenkinsNotificationsSystemTray.class);
		String owner = preferences.get("owner", "");
		String jobCategory = preferences.get("jobCategory", "");
		String project = preferences.get("project", "");
		return new Settings(project, owner, jobCategory);
	}

	public static void save(Settings settings) {
		Preferences preferences = Preferences.userNodeForPackage(JenkinsNotificationsSystemTray.class);
		preferences.put("owner", settings.getOwner());
		preferences.put("jobCategory", settings.getJobCategory());
		preferences.put("project", settings.getProject());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static interface SettingsListener {
		void onChange(Settings settings);
	}
	
}