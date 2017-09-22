package worktools.jenkins.ui;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JenkinsNotificationsSystemTrayTest {

	 JenkinsNotificationsSystemTray systemTray = new  JenkinsNotificationsSystemTray(null, null);

	@Test
	public void imagesCouldBeLoaded() {
		assertNotNull(systemTray.loadImage("success.png"));
		assertNotNull(systemTray.loadImage("failure.png"));
		assertNotNull(systemTray.loadImage("running.png"));
		assertNotNull(systemTray.loadImage("abandoned.png"));
	}

}
