package org.intellij.jenkinsplugin.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by idnnrj on 7/31/2017.
 */
public class GUIUtils {
    static Icon getIcon(String image) {
        return IconLoader.findIcon("/images/" + image);
    }
}
