package org.intellij.jenkinsplugin.ui;

/**
 * Created by idnnrj on 7/27/2017.
 */
/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;


public class BuildStatusIcon extends JComponent {


    private static final int PIXEL_WIDTH = 8;

    private static final Color FOREGROUND_COLOR = new JLabel().getForeground();

    final Icon icon;
    final String toolTipText;

    final int numberToDisplay;
    private final int numberWith;

    public static JComponent createIcon() {

        String image = "jenkins.png";
        Icon icon = GUIUtils.getIcon(image);
        return new BuildStatusIcon(icon, "No broken builds", 0);
    }

    private BuildStatusIcon(Icon icon, String toolTipText, int numberToDisplay) {
        this.icon = icon;
        this.toolTipText = toolTipText;
        this.numberToDisplay = numberToDisplay;
        this.numberWith = numberToDisplay == 0 ? 0 : String.valueOf(numberToDisplay).length() * PIXEL_WIDTH;
        setOpaque(false);

    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        final Insets insets = getInsets();
        return new Dimension(
                icon.getIconWidth() + insets.left + insets.right + numberWith,
                icon.getIconHeight() + insets.top + insets.bottom
        );
    }

    protected void paintComponent(Graphics g) {

        g.setColor(UIUtil.getPanelBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        final Dimension size = getSize();
        int x = (size.width - icon.getIconWidth() - numberWith) / 2;
        int y = (size.height - icon.getIconHeight()) / 2;
        paintIcon(g, icon, x, y);
        setToolTipText(toolTipText);

        if (numberToDisplay > 0) {
            Font originalFont = g.getFont();
            Color originalColor = g.getColor();
            g.setFont(calcFont());
            y += icon.getIconHeight() - g.getFontMetrics().getDescent();
            x += icon.getIconWidth();

            g.setColor(FOREGROUND_COLOR);
            g.drawString(String.valueOf(numberToDisplay), x, y);

            g.setFont(originalFont);
            g.setColor(originalColor);
        }
    }


    private void paintIcon(Graphics g, Icon icon, int x, int y) {
        icon.paintIcon(this, g, x, y);
    }


    private Font calcFont() {
        return getFont().deriveFont(Font.BOLD).deriveFont((float) icon.getIconHeight() * 3 / 5);
    }
}

