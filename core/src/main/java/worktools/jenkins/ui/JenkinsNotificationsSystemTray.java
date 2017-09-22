package worktools.jenkins.ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import worktools.jenkins.AppConstants;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobSearchKey;
import worktools.jenkins.models.Settings;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.models.Settings.SettingsListener;
import worktools.jenkins.services.CachedJenkinsDataService;
import worktools.jenkins.services.JenkinsHttpDataService;
import worktools.jenkins.services.JenkinsNotificationService;
import worktools.jenkins.services.NotificationsListener;
import worktools.jenkins.utils.Utils;

public class JenkinsNotificationsSystemTray implements SettingsListener, NotificationsListener {
	
	private final JenkinsNotificationService jenkinsService;
	private final SettingsPopupWindow settingsPopupWindow;
	
	private volatile JobSearchKey jobSearchKey;
	private volatile JobMetadata jobStatus;
	
	private TrayIcon trayIcon;
	
	private static final String DEFAULT_IMAGE = "jenkins.png";
	
	public JenkinsNotificationsSystemTray(JenkinsNotificationService jenkinsService, JobSearchKey jobSearchKey) {
		this.jenkinsService = jenkinsService;
		this.jobSearchKey = jobSearchKey;
		this.settingsPopupWindow = new SettingsPopupWindow();
	}

	private void init() {
		jenkinsService.trackAsync(jobSearchKey);
		jenkinsService.addNotificationsListener(this);
		settingsPopupWindow.addSettingsUpdateListener(this);
	}

	public static void main(String[] args) throws AWTException {
		Settings settings = Settings.load();
		JenkinsNotificationService jenkinsNotificationService = new JenkinsNotificationService(new CachedJenkinsDataService(new JenkinsHttpDataService()), AppConstants.POLLING_INTERVAL_IN_MILLI_SECONDS);
		JenkinsNotificationsSystemTray systemTray = new JenkinsNotificationsSystemTray(jenkinsNotificationService, settings.toJobSearchKey());
		systemTray.init();
		SwingUtilities.invokeLater(systemTray::createGUI);
	}
	
	public void createGUI() {
		PopupMenu popMenu = new PopupMenu();
		createTrayIcon(popMenu);
		popMenu.add(createExitButton());
		popMenu.add(createSettingsButton());
	}

	private void createTrayIcon(PopupMenu popMenu) {
		trayIcon = new TrayIcon(loadImage(DEFAULT_IMAGE), "Fetching Jobs", popMenu);
		
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException e1) {
			throw new RuntimeException(e1);
		}
	}

	private MenuItem createExitButton() {
		MenuItem exitButtonItem = new MenuItem("Exit");
		exitButtonItem.addActionListener((e) -> exit());
		return exitButtonItem;
	}

	private MenuItem createSettingsButton() {
		MenuItem settingsButtonItem = new MenuItem("Settings");
		settingsButtonItem.addActionListener((e) -> showSettingsWindow());
		return settingsButtonItem;
	}

	Image loadImage(String imageFileName) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource("images/" + imageFileName);
		try {
			return ImageIO.read(resource);
		} catch (IOException e) {
			throw new RuntimeException("could not load image", e);
		}
	}

	private void showSettingsWindow() {
		settingsPopupWindow.showPopup();
	}

	void updateGUI(JobMetadata jobStatus) {
		
		blink();
		updateTrayIconImage(jobStatus);
		
		if(anyNews(jobStatus)) {		
			displayNotification(jobStatus);
		}
		
		updateToolTip(jobStatus);
		this.jobStatus = jobStatus;
	}

	private void blink() {
		updateTrayIconImage(DEFAULT_IMAGE);
		sleepQuietly(2);
	}

	private void sleepQuietly(int timeout) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
		}
	}
	
	private boolean anyNews(JobMetadata jobStatus) {
		return !Objects.equals(this.jobStatus, jobStatus);
	}

	private void updateTrayIconImage(JobMetadata jobStatus) {
		updateTrayIconImage(imageFile(jobStatus));
	}
	
	private void updateTrayIconImage(String imageFile) {
		Image img = loadImage(imageFile);
		trayIcon.setImage(img);
	}
	
	private void updateToolTip(JobMetadata jobStatus) {
		trayIcon.setToolTip(createToolTipText(jobStatus));
	}

	public String createToolTipText(JobMetadata jobStatus) {
		if(jobStatus == null || jobStatus.getResult() == Result.UNKNOWN) {
			return "No jobs for " + getOwnerFirstName() + " in " + jobSearchKey.getJobCategory();
		}
		
		jobStatus.getBuildTime();
		String msg = String.format("%s : %s", jobStatus.getOwner(), jobStatus.getGerritComment());
		String firstLine = msg.substring(0, Math.min(75, msg.length()));
		String secondLine = String.format("%s : %s", jobStatus.getJobCategory(), jobStatus.getJobId());
		String thirdLine = String.format("Stage : %s. ", jobStatus.getBuildStage());
		
		return String.join("\n", firstLine, secondLine + ":  " + thirdLine);
	}

	public void displayNotification(JobMetadata jobStatus) {
		String template = "%s : %s/%s = %s";
		String text = null;
		if(jobStatus == null || jobStatus.getResult() == Result.UNKNOWN) {
			text = "No jobs for " + getOwnerFirstName() + " in " + jobSearchKey.getJobCategory();
		} else {
			text = String.format(template, getOwnerFirstName(), jobStatus.getJobCategory(), jobStatus.getJobId(), jobStatus.getResult());
		}
		trayIcon.displayMessage("Jenkins", text, TrayIcon.MessageType.INFO);
	}

	public String imageFile(JobMetadata jobStatus) {
		
		if(jobStatus == null)
			return "jenkins.png";
		
		switch(jobStatus.getResult()) {
			case SUCCESS:
				return "success.png";
			case FAILURE:
				return "failure.png";
			case RUNNING:
				return "running.png";
			case ABORTED:
			case UNKNOWN:
				return "abandoned.png";
			default:
				throw new IllegalStateException("Dont have an icon image for - " + jobStatus.getResult());
		}
	}

	public void exit() {
		System.exit(0);
	}
	
	private String getOwnerFirstName() {
		return this.jobSearchKey.getOwner().split("\\.")[0];
	}
	
	@Override
	public void onChange(Settings settings) {
		if(Objects.equals(jobSearchKey, settings.toJobSearchKey())) {
			return;
		}
		
		if(!Objects.equals(jobSearchKey, settings.toJobSearchKey())) {
			Utils.log("Tracking : " + settings.getJobCategory());
			jenkinsService.trackAsync(settings.toJobSearchKey());
		}
		
		this.jobSearchKey = settings.toJobSearchKey();
		
	}

	@Override
	public void onUpdate(JobMetadata jobMetadata) {
		Utils.log("Received job update - %s", jobMetadata);
		SwingUtilities.invokeLater(() -> updateGUI(jobMetadata));
	}
}