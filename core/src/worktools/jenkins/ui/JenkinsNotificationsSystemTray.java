package worktools.jenkins.ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import worktools.jenkins.JenkinsService;
import worktools.jenkins.JobKey;
import worktools.jenkins.JobMetadata;
import worktools.jenkins.ui.PreferencesHelper.Settings;

public class JenkinsNotificationsSystemTray {
	
	private final JenkinsService jenkinsService;
	private final SettingsPopupWindow settingsPopupWindow;
	private TrayIcon trayIcon;
	private volatile JobMetadata jobStatus;
	private String owner;
	private String ownerFirstName;
	private String jobCategory;
	
	public JenkinsNotificationsSystemTray(JenkinsService jenkinsService, String owner, String jobCategory) {
		this.jenkinsService = jenkinsService;
		this.owner = owner;
		this.jobCategory = jobCategory;
		parseFirstName();
		this.jenkinsService.track(jobCategory);
		this.settingsPopupWindow = new SettingsPopupWindow(this);
	}

	private void parseFirstName() {
		this.ownerFirstName = owner.split("\\.")[0];
	}
	
	public static void main(String[] args) throws AWTException {
		
//		JenkinsNotificationsSystemTray systemTray = new JenkinsNotificationsSystemTray(new JenkinsService(new MockJenkinsDataService()), owner);
		Settings settings = PreferencesHelper.load();
		String owner = settings.getOwner();
		String jobCategory = settings.getJobCategory();
		JenkinsNotificationsSystemTray systemTray = new JenkinsNotificationsSystemTray(new JenkinsService(), owner, jobCategory);
		SwingUtilities.invokeLater(systemTray::createGUI);
		
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(systemTray::refresh), 5, 15, TimeUnit.SECONDS);
	}
	
	public void createGUI() {
		PopupMenu popMenu = new PopupMenu();
		
		MenuItem exitButtonItem = new MenuItem("Exit");
		exitButtonItem.addActionListener((e) -> exit());
		popMenu.add(exitButtonItem);
		
		
		String defaultImage = "jenkins.png";
		Image img = loadImage(defaultImage);
		trayIcon = new TrayIcon(img, "Fetching Jobs", popMenu);
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException e1) {
			throw new RuntimeException(e1);
		}
		
		
		MenuItem settingsButtonItem = new MenuItem("Settings");
		settingsButtonItem.addActionListener((e) -> showSettingsWindow());
		popMenu.add(settingsButtonItem);
		
		SwingUtilities.invokeLater(this::refresh);  
	}

	private Image loadImage(String imageFileName) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(imageFileName);
		try {
			return ImageIO.read(resource);
		} catch (IOException e) {
			throw new RuntimeException("could not load image", e);
		}
	}
	
	void fetchImage(String imageName) {
		
	}

	private void showSettingsWindow() {
		settingsPopupWindow.showPopup();
	}

	void refresh() {
		Optional<JobKey> jobKey = fetchJobKey();
		if(jobKey.isPresent()){
			JobMetadata jobStatus = jenkinsService.getJobStatus(jobKey.get());
			SwingUtilities.invokeLater(() -> updateGUI(jobStatus));
		} else {
			SwingUtilities.invokeLater(() -> updateGUI(null));
		}
	}
	
	void settingsChanged(Settings settings) {
		
		if(!Objects.equals(jobCategory, settings.getJobCategory())) {
			System.out.println("Tracking : " + settings.getJobCategory());
			jenkinsService.track(settings.getJobCategory());
		}
		
		this.jobCategory = settings.getJobCategory();
		this.setOwner(settings.getOwner());
		
		refresh();
	}
	
	public void updateGUI(JobMetadata jobStatus) {
		
		try {
			String defaultImage = "jenkins.png";
			Image img = loadImage(defaultImage);
			trayIcon.setImage(img);
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
		}
		
		String imageFile = imageFile(jobStatus);
		Image img = loadImage(imageFile);
		trayIcon.setImage(img);
		if(!Objects.equals(this.jobStatus, jobStatus)) {		
			displayNotification(jobStatus);
		}
		
		trayIcon.setToolTip(createToolTipText(jobStatus));
		this.jobStatus = jobStatus;
	}

	public String createToolTipText(JobMetadata jobStatus) {
		if(jobStatus == null) {
			return "No jobs for " + ownerFirstName + " in " + jobCategory;
		}
		String firstLine = String.format("%s : %s", jobStatus.getOwner(), jobStatus.getGerritComment());
		String secondLine = String.format("%s : %s", jobStatus.getJobCategory(), jobStatus.getJobId());
		String toolTip = String.format("%s%n%s", firstLine.length() > 100 ? firstLine.substring(0, 100) : firstLine, secondLine);
		return toolTip;
	}

	public void displayNotification(JobMetadata jobStatus) {
		String template = "%s : %s/%s = %s";
		String text = null;
		if(jobStatus == null) {
			text = "No jobs for " + ownerFirstName + " in " + jobCategory;
		} else {
			text = String.format(template, ownerFirstName, jobStatus.getJobCategory(), jobStatus.getJobId(), jobStatus.getResult());
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
			case ABORTED:
				return "running.png";
			default:
				throw new IllegalStateException("Dont have an icon image for - " + jobStatus.getResult());
		}
	}

	private Optional<JobKey> fetchJobKey() {
		List<JobMetadata> jobsByOwner = jenkinsService.getJobsByOwner(owner);
		return jobsByOwner.stream()
			.filter(jobMetadata -> Objects.equals(jobMetadata.getJobCategory(), jobCategory))
			.findFirst()
			.map(JobMetadata::getJobKey);

	}

	public void exit() {
		System.exit(0);
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
		parseFirstName();
	}
	
	public void setJobCategory(String jobCategory) {
		this.jobCategory = jobCategory;
	}
}
