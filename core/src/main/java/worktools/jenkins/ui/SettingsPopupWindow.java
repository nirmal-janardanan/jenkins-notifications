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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import worktools.jenkins.models.Settings;

public class SettingsPopupWindow extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 291100554956339935L;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 100;
	private static final int OFFSET = 50;
	private final JTextField projectTextField = new JTextField();
	private final JTextField ownerTextField = new JTextField();
	private final JTextField jobCategoryTextField = new JTextField();
	private final JButton saveButton = new JButton("Save");
	private final JButton cancelButton = new JButton("Cancel");
	
	final Set<Settings.SettingsListener> listeners = new CopyOnWriteArraySet<>();
	
	public SettingsPopupWindow() {
		
		createEntryPanel();
		applyButtonBehaviours();
		setDefaultValues();
		setSize(new Dimension(WIDTH, HEIGHT));
		setTitle("Settings");
		position();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		overrideWindowClosingOperation();
	}

	private void createEntryPanel() {
		GridLayout entryLayout = new GridLayout(4, 2);
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(entryLayout);
		
		entryPanel.add(new JLabel("Project"));
		entryPanel.add(projectTextField);
		
		entryPanel.add(new JLabel("Job Category"));
		entryPanel.add(jobCategoryTextField);
		
		entryPanel.add(new JLabel("Owner"));
		entryPanel.add(ownerTextField);
		
		entryPanel.add(saveButton);
		entryPanel.add(cancelButton);
		
		entryPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		add(entryPanel, BorderLayout.CENTER);
		
		projectTextField.setToolTipText("G3/NGI");
		jobCategoryTextField.setToolTipText("G3Build-Branch/G3Build-Master/NGIBuild-Branch/NGIBuild-Master");
		ownerTextField.setToolTipText("nirmal.janardanan@ideas.com");
	}
	
	private void position() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(new Point((int)screenSize.getWidth() - WIDTH - OFFSET, (int)screenSize.getHeight() - HEIGHT - OFFSET));
		this.setAlwaysOnTop(true);
	}

	private void setDefaultValues() {
		Settings settings = Settings.load();
		jobCategoryTextField.setText(settings.getJobCategory());
		ownerTextField.setText(settings.getOwner());
		projectTextField.setText(settings.getProject());
		
	}

	private void applyButtonBehaviours() {
		saveButton.addActionListener(e -> {
			exit();
			SwingUtilities.invokeLater(this::save);
		});
		
		cancelButton.addActionListener(e -> exit());
	}
	
	private void overrideWindowClosingOperation() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.out.println("window closing");
				exit();
			}
		});
	}

	public void save() {
		
		Settings settings = new Settings(projectTextField.getText().trim(), ownerTextField.getText().trim().toLowerCase(), jobCategoryTextField.getText().trim());
		Settings.save(settings);
		
		listeners.stream().forEach(listener -> listener.onChange(settings));
	}
	
	public void exit() {
		hidePopup();
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

	public void addSettingsUpdateListener(Settings.SettingsListener listener) {
		listeners.add(listener);
	}
}
