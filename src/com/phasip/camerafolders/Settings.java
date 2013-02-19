package com.phasip.camerafolders;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;

public class Settings {
	private static final String PREFS_NAME = "camfolders";
	private static final String DEFAULT_FOLDER = "default_folder";
	private static final String VERSION = "VERSION";
	private static final String FILE_PATTERN = "filePattern";
	private static final String LAST_NEWDIR = "mkdir";
	private static final String SORT_TYPE = "sortType";
	private static final String SHOW_HIDDEN = "showHidden";
	private static final String ALLOW_ROOT = "allowRoot";
	private static final String SHOW_VIDEO = "showVideo";
	private static final String START_CAMERA = "autostartCam";
	private String filePattern = "dd-MM-yy kk.mm";
	private File sdCardFile;

	public void toggleSortType() {
		setSortType(!isSortType());
	}

	public void toggleAllowRoot() {
		setAllowRoot(!isAllowRoot());
	}

	public void toggleShowHidden() {
		setShowHidden(!isShowHidden());
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
		saveSetting(FILE_PATTERN,filePattern);
	}

	public File getSdCardFile() {
		return sdCardFile;
	}

	/*public void setSdCardFile(File sdCardFile) {
		this.sdCardFile = sdCardFile;
	}*/

	public File getDefaultFolder() {
		return defaultFolder;
	}

	public void setDefaultFolder(File defaultFolder) {
		this.defaultFolder = defaultFolder;
		saveSetting(DEFAULT_FOLDER, defaultFolder.getAbsolutePath());
	}

	public boolean isAllowRoot() {
		return allowRoot;
	}

	public void setAllowRoot(boolean allowRoot) {
		this.allowRoot = allowRoot;
		saveSetting(ALLOW_ROOT, allowRoot);
	}

	public boolean isShowHidden() {
		return showHidden;
	}

	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden;
		saveSetting(SHOW_HIDDEN, showHidden);
	}

	public boolean isSortType() {
		return sortType;
	}

	public void setSortType(boolean sortType) {
		this.sortType = sortType;
		saveSetting(SORT_TYPE, sortType);
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
		saveSetting(VERSION, version);
	}

	public String getLastNewFoldername() {
		return lastFolderName;
	}

	public void setLastNewFoldername(String s) {
		lastFolderName = s;
		saveSetting(LAST_NEWDIR, lastFolderName);
	}

	private File defaultFolder;
	private boolean allowRoot = false;
	private boolean showHidden = false;
	private boolean sortType = true; // True = a-z, false = date
	private int version = -1;
	private SharedPreferences mPrefs;
	private Activity a;
	private String lastFolderName;
	private boolean showVideo;
	private boolean startCamera;
	private static Settings settings;
	public static Settings getInstance(Activity a) {
		if (settings == null)
			settings = new Settings(a);
		return settings;
	}
	
	private Settings(Activity me) {
		a = me;
		loadSettings();
	}

	private void loadSettings() {
		sdCardFile = Environment.getExternalStorageDirectory();
		mPrefs = a.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
		defaultFolder = new File(mPrefs.getString(DEFAULT_FOLDER, sdCardFile.getAbsolutePath()));
		if (!defaultFolder.exists())
			defaultFolder = Environment.getRootDirectory();

		allowRoot = mPrefs.getBoolean(ALLOW_ROOT, allowRoot);
		showHidden = mPrefs.getBoolean(SHOW_HIDDEN, showHidden);
		sortType = mPrefs.getBoolean(SORT_TYPE, sortType);
		lastFolderName = mPrefs.getString(LAST_NEWDIR, "New Folder");
		filePattern = mPrefs.getString(FILE_PATTERN, filePattern);
		showVideo = mPrefs.getBoolean(SHOW_VIDEO, false);
		startCamera = mPrefs.getBoolean(START_CAMERA, false);
		version = mPrefs.getInt(VERSION, -1);
	}

	private void saveSetting(String key, boolean data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean(key, data);
		ed.commit();
	}

	private void saveSetting(String key, int data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putInt(key, data);
		ed.commit();
	}

	private void saveSetting(String key, String data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString(key, data);
		ed.commit();
	}

	public boolean isShowVideo() {
		return showVideo;
	}

	public void toggleShowVideo() {
		setShowVideo(!isShowVideo());
	}
	public void setShowVideo(boolean v) {
		showVideo = v;
		saveSetting(SHOW_VIDEO,v);
	}

	public boolean isStartCamera() {
		return startCamera;
	}

	public void toggleStartCamera() {
		setStartCamera(!isStartCamera());
	}

	private void setStartCamera(boolean b) {
		startCamera = b;
		saveSetting(START_CAMERA,b);
	}

}
