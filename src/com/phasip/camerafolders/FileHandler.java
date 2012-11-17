package com.phasip.camerafolders;

import java.io.File;
import java.util.Comparator;

public interface FileHandler extends Comparator<File> {
	public void handleFileClick(File f);
	public void handleFolderChange(File f);
	public void handleFileSymlink(File f);
}
