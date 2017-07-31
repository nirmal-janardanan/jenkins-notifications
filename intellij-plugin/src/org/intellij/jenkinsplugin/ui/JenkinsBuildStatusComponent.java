package org.intellij.jenkinsplugin.ui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.jetbrains.annotations.NotNull;

/**
 * Created by idnnrj on 7/27/2017.
 */
public class JenkinsBuildStatusComponent extends NonOpaquePanel implements ProjectComponent {

    private final Project project;

    public static JenkinsBuildStatusComponent getInstance(Project project) {
        return ServiceManager.getService(project, JenkinsBuildStatusComponent.class);
    }

    public JenkinsBuildStatusComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        ServiceManager.getService(project, JenkinsBuildStatusWidget.class);
        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        final JenkinsBuildStatusWidget jenkinsWidget = JenkinsBuildStatusWidget.getInstance(project);
        statusBar.addWidget(jenkinsWidget);
        jenkinsWidget.install(statusBar);
    }

    @Override
    public void projectClosed() {
        // do nothing
    }

    @Override
    public void initComponent() {
/*        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        Content content = ContentFactory.SERVICE.getInstance().createContent(browserPanel, null, false);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(JENKINS_BROWSER, false, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(JENKINS_ICON);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);*/

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return this.getClass().getName();
    }
}
