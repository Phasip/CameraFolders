/**
    Copyright (C) 2011  Pasi Saarinen - phasip@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.phasip.camerafolders;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A application that takes pictures and puts them in a folder
 * 
 * @author Pasi Saarinen
 * 
 */
public class CameraFolders extends Activity implements OnClickListener,
		FileHandler {
	// private static final String APP_NAME = "Lecture Cam";
	private final int CAPTURE_IMAGE = 2;
	private final int CAPTURE_VIDEO = 3;

	private FileBrowser fb;
	private Settings settings;
	TabHost tabs;
	public static final String TAB_BROWSE = "browse";
	private static final String TAB_FAV = "fav";
	private FavList fl = null;
	private String APP_NAME = "CameraFolders";

	public void tabCreate() {
		setContentView(R.layout.tablayout);

		tabs = (TabHost) findViewById(R.id.TabHost);

		tabs.setup();

		TabHost.TabSpec browseTab = tabs.newTabSpec(TAB_BROWSE);
		browseTab.setIndicator("Browse");
		browseTab.setContent(R.id.mainTab);

		TabHost.TabSpec favTab = tabs.newTabSpec(TAB_FAV);
		browseTab.setIndicator(getLayoutInflater().inflate(
				R.layout.browsetabind, null));
		favTab.setIndicator(getLayoutInflater().inflate(R.layout.favtabind,
				null));
		favTab.setContent(R.id.favTab);

		tabs.addTab(browseTab);
		tabs.addTab(favTab);
	}

	/* Load the default folder */
	private void loadSettings() {
		settings = Settings.getInstance(this);
		fb.showHidden(settings.isShowHidden());
		fb.updateFileList(settings.getDefaultFolder());
		ImageButton b = (ImageButton) findViewById(R.id.video);
		b.setVisibility(settings.isShowVideo() ? View.VISIBLE : View.GONE);
	}

	protected void onPause() {
		super.onPause();
		Log.d(APP_NAME, "ONPAUSE CALLED!");
		settings.setDefaultFolder(fb.getFile());
		// settings.saveSettings(); //saved on write
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (TAB_BROWSE.contentEquals(tabs.getCurrentTabTag())) {
			fb.onCreateContextMenu(menu, v, menuInfo);
		} else {
			fl.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (TAB_BROWSE.contentEquals(tabs.getCurrentTabTag())) {
			return fb.onContextItemSelected(item);
		} else {
			return fl.onContextItemSelected(item);
		}
	}

	private int getVersion() {
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			return -2;
		}
	}

	private void versionCheck() {
		final int myVer = getVersion();
		if (myVer > settings.getVersion()) {
			/*
			 * Stolen from
			 * http://stackoverflow.com/questions/4300012/displaying-
			 * a-dialog-in-oncreate
			 */
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.first_run_version_title);
			builder.setMessage(R.string.first_run_message);
			builder.setNeutralButton(R.string.ok_menu_button,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							settings.setVersion(myVer);
							// settings.saveSettings(); //Saved on write
						}
					});
			AlertDialog alert = builder.create();
			alert.show(); // <-- Forgot this in the original post
		}
	}

	/** Move one folder back and if the current folder is root end application */
	public void onBackPressed() {
		File f = fb.getFile();
		File p = f.getParentFile();
		if (p == null
				|| (f.compareTo(settings.getSdCardFile()) == 0 && !settings
						.isAllowRoot())) {
			this.finish();
			return;
		}
		fb.updateFileList(p);
	}

	/**
	 * Show a short toast
	 * 
	 * @param msg
	 *            the toast to show
	 */
	private void shortToast(String msg) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Called when the setdefault button in the menu is clicked
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.changeSort) {
			settings.toggleSortType();
			setSort(item);
		} else if (item.getItemId() == R.id.allowRoot) {
			settings.toggleAllowRoot();
			int icon = settings.isAllowRoot() ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
		} else if (item.getItemId() == R.id.showVideoButton) {
			settings.toggleShowVideo();
			int icon = settings.isShowVideo() ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
			ImageButton b = (ImageButton) findViewById(R.id.video);
			b.setVisibility(settings.isShowVideo() ? View.VISIBLE : View.GONE);

		} else if (item.getItemId() == R.id.showHidden) {
			settings.toggleShowHidden();
			fb.showHidden(settings.isShowHidden());
			fb.updateFileList();
			int icon = settings.isShowHidden() ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
		} else if (item.getItemId() == R.id.changePattern) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.new_file_pattern_title);
			builder.setMessage(R.string.new_file_pattern);
			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setText(settings.getFilePattern());
			builder.setView(input);

			builder.setPositiveButton(R.string.ok_menu_button,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String s = input.getText() + "";
							String bad = isGoodPattern(s);
							if (bad == null)
								settings.setFilePattern("" + input.getText());
							else
								shortToast("Pattern contains reserved character(s): "
										+ bad);
						}

					});
			builder.setNegativeButton(R.string.abort_menu_button,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							shortToast("Pattern change aborted");
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else if (item.getItemId() == R.id.startCamera) {
			settings.toggleStartCamera();
			int icon = !settings.isStartCamera() ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
			ImageButton b = (ImageButton) findViewById(R.id.video);
			b.setVisibility(settings.isStartCamera() ? View.VISIBLE : View.GONE);

		}
		return true;
	}

	private static final String ReservedChars = "|\\?*\"/";

	private String isGoodPattern(String s) {
		String bad = "";
		for (int i = 0; i < ReservedChars.length(); i++) {
			int c = ReservedChars.charAt(i);
			if (s.indexOf(c) != -1)
				bad = bad + (char) c;
		}
		if (bad.length() == 0)
			return null;
		return bad;
	}

	public void setSort(MenuItem item) {
		int icon = settings.isSortType() ? android.R.drawable.ic_menu_sort_alphabetically
				: android.R.drawable.ic_menu_sort_by_size;
		item.setIcon(icon);
		item.setTitle(settings.isSortType() ? "Sorting by name"
				: "Sorting by date");
		fb.updateFileList();
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (settings.isAllowRoot()) // TODO: How is this constant found in
									// variables? (0?)
			menu.getItem(0).setIcon(
					android.R.drawable.button_onoff_indicator_on);
		if (settings.isShowHidden()) // TODO: How is this constant found in
										// variables? (0?)
			menu.getItem(3).setIcon(
					android.R.drawable.button_onoff_indicator_on);
		if (settings.isShowVideo()) {
			menu.getItem(4).setIcon(
					android.R.drawable.button_onoff_indicator_on);
		}
		if (!settings.isStartCamera()) {
			menu.getItem(5).setIcon(
					android.R.drawable.button_onoff_indicator_on);
		}
		setSort(menu.getItem(2));

		return true;

	}

	/**
	 * Show the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Sets the resources clickListener to this.
	 * 
	 * @param id
	 *            Id of the resource.
	 */
	private void setClickListener(int id) {
		View v = this.findViewById(id);
		v.setOnClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		tabCreate();
		// setContentView(R.layout.tablayout);
		setClickListener(R.id.camera);
		// setClickListener(R.id.gallery); //Removed, didn't get it to work
		// right.
		setClickListener(R.id.newdir);
		setClickListener(R.id.video);
		fl = (FavList) this.findViewById(R.id.favView);
		fl.setHandler(this);

		fb = (FileBrowser) this.findViewById(R.id.fileView);
		fb.setHandlers(this, fl);

		registerForContextMenu(fb);
		registerForContextMenu(fl);
		loadSettings();
		versionCheck();
	}

	private void promptNewDir() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Create Folder");
		alert.setMessage("Type name of new folder");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);

		input.setText(settings.getLastNewFoldername());
		alert.setView(input);
		DialogInterface.OnClickListener cl = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (AlertDialog.BUTTON_POSITIVE == whichButton) {
					String value = input.getText().toString();
					createFolder(value);
				}
			}
		};

		alert.setPositiveButton("Ok", cl);
		alert.setNegativeButton("Cancel", cl);

		alert.show();
	}

	private void createFolder(String name) {
		String bad = isGoodPattern(name);
		if (bad != null) {
			shortToast("Filename contains reserved character(s): " + bad);
			return;
		}

		settings.setLastNewFoldername(name);

		File f = fb.getFile();
		File n = new File(f.getAbsolutePath() + "/" + name);
		if (!f.canWrite()) {
			shortToast("Cannot write to folder");
			return;
		}
		if (!n.mkdir()) {
			shortToast("Folder creation fails");
			return;
		}
		shortToast("Folder created!!");
		fb.updateFileList();
	}

	/**
	 * @return String name that follows format and is unique for folder
	 */
	private String nextFilename(String ext) {
		Date date = new Date();
		String s = settings.getFilePattern();
		/*
		 * Ugly hack to make dateformat take %XX instead of just XX
		 * letting people easily have normal text in their filename
		 -- Actually skipping this hack
		/*
        s = s.replaceAll("'", "'''");
		s = "'" + s + "'";
		s = s.replaceAll("%(([a-zA-Z])\\2+)", "'$1'");
        */
		
		String orig = ""
				+ android.text.format.DateFormat.format(
						s, date);
		String ret = orig + ext;

		int i = 1;
		while (fb.hasFile(ret)) // So you have a folder full of
		{
			ret = orig + "-" + i + ext;
			i++;
		}
		return ret;

	}
/*
	protected void launchGallery(File f) { // TODO
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f), "image/*");
		startActivity(intent);
		return;
	}

	protected void launchVideo(File f) { // TODO
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f), "video/*");
		startActivity(intent);
		return;
	} */

	public void onResume() {
		super.onResume();

	}

	public void setFolder(File f) {
		fb.updateFileList(f);
	}

	public void launchCamera(boolean video) {
		if (!fb.canSavePicture()) {
			shortToast("You cannot take pictures into read-only directories!");
			return;
		}

		String nextImage;
		if (!video) {
			nextImage = fb.getFile().getAbsolutePath() + "/"
					+ nextFilename(".jpg");
			ImageCapturer.takePicture(this, CAPTURE_IMAGE, nextImage);
		} else {
			// currently ignored
			nextImage = fb.getFile().getAbsolutePath() + "/"
					+ nextFilename(".m4v");
			ImageCapturer.takeVideo(this, CAPTURE_VIDEO, nextImage);
		}

		tabs.setCurrentTabByTag(CameraFolders.TAB_BROWSE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE || requestCode == CAPTURE_VIDEO) {
			String imagefile = ImageCapturer.getResult(resultCode, data);
			if (imagefile == null) {
				shortToast(ImageCapturer.getError(resultCode, data));
			} else if (settings.isStartCamera()) {
				launchCamera(requestCode == CAPTURE_VIDEO);
				return;
			}
			fb.updateFileList();
		}
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.camera:
			launchCamera(false);
			break;
		case R.id.video:
			launchCamera(true);
			break;
		case R.id.newdir:
			promptNewDir();
			break;
		}

	}

	@Override
	public void handleFileClick(File f) {
		if (f == null)
			return;
		if (!f.canRead()) {
			shortToast("Cannot read file");
			return;
		}
        //launch intent
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(f); 
        String url = uri.toString();
        //grab mime
        String newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(url));

        i.setDataAndType(uri, newMimeType);
        startActivity(i);
        return;

	}

	private void setTitle(String t) {
		TextView v = (TextView) findViewById(R.id.browsTitle);
		v.setText(t);
	}

	@Override
	public void handleFolderChange(File f) {
		String title = f.getAbsolutePath();
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();

		if (title.length() * 12 >= width) {
			width = (title.length() * 12 - width) / 12;
			if (width >= 0 && width <= title.length())
				title = "..." + title.substring(width);
		}
		this.setTitle(title);
	}

	public void handleFileSymlink(File to) {
		return;
	}

	@Override
	public int compare(File arg0, File arg1) {
		if (arg0 == null)
			return arg1 == null ? 0 : 1;
		if (arg1 == null)
			return -1;

		if (settings.isSortType())
			return arg0.getName().compareToIgnoreCase(arg1.getName());

		return Long.valueOf(arg1.lastModified()).compareTo(arg0.lastModified());

	}
}