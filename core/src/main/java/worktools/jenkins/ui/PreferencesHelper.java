package worktools.jenkins.ui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesHelper {
	
	public static class Settings {
		private final String owner;
		private final String jobCategory;
		public Settings(String owner, String jobCategory) {
			this.owner = owner;
			this.jobCategory = jobCategory;
		}

		public String getJobCategory() {
			return jobCategory;
		}
		
		public String getOwner() {
			return owner;
		}
	}
	
	public static void save(Settings settings) {
		Preferences preferences = Preferences.userNodeForPackage(JenkinsNotificationsSystemTray.class);
		preferences.put("owner", settings.getOwner());
		preferences.put("jobCategory", settings.getJobCategory());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Settings load() {
		Preferences preferences = Preferences.userNodeForPackage(JenkinsNotificationsSystemTray.class);
		String owner = preferences.get("owner", "");
		String jobCategory = preferences.get("jobCategory", "");
		return new Settings(owner, jobCategory);
	}
}
