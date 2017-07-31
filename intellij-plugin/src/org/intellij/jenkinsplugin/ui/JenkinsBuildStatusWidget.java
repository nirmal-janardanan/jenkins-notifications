package org.intellij.jenkinsplugin.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Date;

/**
 * Created by idnnrj on 7/27/2017.
 */
class JenkinsBuildStatusWidget extends NonOpaquePanel implements CustomStatusBarWidget {

    private final Project project;
    private JLabel myLabel = new JLabel("Jenkins Build");

    public static JenkinsBuildStatusWidget getInstance(Project project) {
        return ServiceManager.getService(project, JenkinsBuildStatusWidget.class);
    }


    public JenkinsBuildStatusWidget(Project project) {
        this.project = project;
        JComponent buildStatusIcon = createStatusIcon();
        setLayout(new BorderLayout());
        add(buildStatusIcon, BorderLayout.CENTER);
    }

    private JComponent createStatusIcon() {
        JComponent statusIcon = BuildStatusIcon.createIcon();
        setBorder(StatusBarWidget.WidgetBorder.INSTANCE);
        return statusIcon;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @NotNull
    @Override
    public String ID() {
        return this.getClass().getName();
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return new StatusBarWidget.IconPresentation(){

            @Nullable
            @Override
            public String getTooltipText() {
                return "Build status @ " + new Date();
            }

            @Nullable
            @Override
            public Consumer<MouseEvent> getClickConsumer() {
                return null;
            }

            @NotNull
            @Override
            public Icon getIcon() {
                return IconLoader.findIcon("/images" + "jenkins.png");
            }
        };
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }
}
