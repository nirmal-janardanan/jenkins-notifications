package worktools.jenkins.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import worktools.jenkins.ui.PreferencesHelper.Settings;

public class SettingsPopupWindow extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 291100554956339935L;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 100;
	private final JTextField ownerTextField;
	private final JTextField jobCategoryTextField;
	private final JButton saveButton;
	private final JButton cancelButton;
	
	private final JenkinsNotificationsSystemTray notificationsUI;
	
	public SettingsPopupWindow(JenkinsNotificationsSystemTray notificationsUI) {
		
		this.notificationsUI = notificationsUI;
		
		GridLayout entryLayout = new GridLayout(3, 2);
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(entryLayout);
		
		entryPanel.add(new JLabel("Job Category"));
		jobCategoryTextField = new JTextField();
		entryPanel.add(jobCategoryTextField);
		
		entryPanel.add(new JLabel("Owner"));
		ownerTextField = new JTextField();
		entryPanel.add(ownerTextField);
		
		saveButton = new JButton("Save");
		entryPanel.add(saveButton);
		
		cancelButton = new JButton("Cancel");
		entryPanel.add(cancelButton);
		
		entryPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		add(entryPanel, BorderLayout.CENTER);
		
		pack();
		
		applyButtonBehaviours();
		setValues();
		setSize(new Dimension(WIDTH, HEIGHT));
		setTitle("Settings");
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(new Point((int)screenSize.getWidth() - WIDTH - 50, (int)screenSize.getHeight() - HEIGHT - 50));
		this.setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.out.println("window closing");
				exit();
			}
		});
	}
	
	private void setValues() {
		Settings settings = PreferencesHelper.load();
		jobCategoryTextField.setText(settings.getJobCategory());
		ownerTextField.setText(settings.getOwner());
		
	}

	private void applyButtonBehaviours() {
		saveButton.addActionListener(e -> {
			exit();
			SwingUtilities.invokeLater(this::save);
		});
		
		cancelButton.addActionListener(e -> exit());
	}

	public void save() {
		
		Settings settings = new Settings(ownerTextField.getText().trim().toLowerCase(), jobCategoryTextField.getText().trim());
		PreferencesHelper.save(settings);
		
		notificationsUI.settingsChanged(settings);
	}
	
	public void exit() {
		hidePopup();
	}
	
	public static void main(String[] args) {
		SettingsPopupWindow ui = new SettingsPopupWindow(null);
		ui.showPopup();
	}

	void showPopup() {
		setVisible(true);
	}
	
	void hidePopup() {
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if("exit".equals(event.getActionCommand())){
			exit();
		}
	}
}
