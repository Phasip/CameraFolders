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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore; //import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A application that takes pictures and puts them in a folder
 * 
 * @author Pasi Saarinen
 * 
 */
public class CameraFolders extends Activity implements OnClickListener {
	// private static final String APP_NAME = "Lecture Cam";
	private final int CAPTURE_IMAGE = 2;
	Uri imageUri = null;
	private FileBrowser fb;
	File sdCardFile;
	boolean allowRoot = false;
	private SharedPreferences mPrefs;

	/* Load the default folder */
	private void loadSettings() {
		sdCardFile = Environment.getExternalStorageDirectory();
		File f;
		mPrefs = getSharedPreferences("camfolders",MODE_PRIVATE);
		f = new File(mPrefs.getString("default_folder", sdCardFile.getAbsolutePath()));
		allowRoot = mPrefs.getBoolean("allowRoot", false);

		if (!f.exists())
			f = Environment.getRootDirectory();
		fb.updateFileList(f);
	}
	protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("default_folder", fb.getFile().getAbsolutePath());
        ed.putBoolean("allowRoot", allowRoot);
        ed.commit();
    }


	/** Move one folder back and if the current folder is root end application */
	public void onBackPressed() {
		File f = fb.getFile();
		File p = f.getParentFile();
		if (p == null || (f.compareTo(sdCardFile) == 0 && !allowRoot)) {
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
		//SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = mPrefs.edit();
		/*
		 * if (item.getItemId() == R.id.setdefault) {
			editor.putString("default_folder", fb.getFile().getAbsolutePath());
			shortToast("Default folder: " + fb.getFile().getAbsolutePath());
		} */
		if (item.getItemId() == R.id.allowRoot) {
			allowRoot = !allowRoot;
			editor.putBoolean("allowRoot", allowRoot);
			int icon = allowRoot ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
		}
		editor.commit();
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (allowRoot) //TODO: How is this constant found in variables? (0?)
			menu.getItem(0).setIcon(
					android.R.drawable.button_onoff_indicator_on);
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
		setContentView(R.layout.main);
		setClickListener(R.id.camera);
		// setClickListener(R.id.gallery); //Removed, didn't get it to work
		// right.
		setClickListener(R.id.newdir);
		fb = (FileBrowser) this.findViewById(R.id.fileView);
		loadSettings();
	}

	private void promptNewDir() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Create Folder");
		alert.setMessage("Type name of new folder");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);

		input.setText(folderLastName());
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
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("mkdir", name);
		editor.commit();
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

	private String folderLastName() {
		final SharedPreferences settings = getPreferences(0);
		String ret = settings.getString("mkdir", "New Folder");
		return ret;
	}

	private String nextPhotoName() {
		Date date = new Date();
		String orig = ""
				+ android.text.format.DateFormat.format("dd-MM-yy kk.mm", date);
		String ret = orig + ".jpg";

		int i = 1;
		while (fb.hasFile(ret)) // So you have a folder full of
		// Integer.MAX_VALUE pictures? Who cares! We go
		// negative too!
		{
			// Log.i("CameraFolders",ret + " exists!");
			ret = orig + "-" + i + ".jpg";
			i++;
		}
		return ret;

	}

	protected void launchGallery() { // TODO
		// if (1 == 1)
		// throw new RuntimeException("Fix gallery!!");
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		File f = fb.getFile();
		// Log.i("asdwqe",Uri.fromFile(list[i]) + "");
		// Uri.fromFile(list[i])
		intent.setDataAndType(Uri.fromFile(f), "image/*");
		// intent.putExtra("slideshow",true);
		startActivity(intent);
		/*
		 * File[] list = f.listFiles(); if (list == null || list.length < 0) {
		 * shortToast("No files in folder"); return; }
		 * 
		 * //TODO for (int i = 0; i < list.length; i++) { if (list[i].isFile())
		 * { //01-28 13:24:19.251: INFO/ActivityManager(1337): Displayed
		 * activity com.cooliris.media/.Gallery: 628 ms (total 628 ms)
		 * //Log.i("asdwqe",Uri.fromFile(list[i]) + ""); //Uri.fromFile(list[i])
		 * intent.setDataAndType(Uri.fromFile(list[i]), "image/jpg");
		 * intent.putExtra("slideshow",true); startActivity(intent); return; } }
		 */
		shortToast("Only folders in folder");
		return;

	}

	protected String launchCamera() {
		// create parameters for Intent with filename
		ContentValues values = new ContentValues();
		// values.put(MediaStore.Images.Media.DATA,"/sdcard/file_moo.jpg");
		values.put(MediaStore.Images.Media.TITLE, "Image");
		values.put(MediaStore.Images.Media.DESCRIPTION,
				"Image capture by camera");
		// imageUri is the current activity attribute, define and save it for
		// later usage (also in onSaveInstanceState)
		try {
			imageUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (IllegalStateException e) {
			return "Could not open picture file, are you in read only directory?";
		}
		catch (UnsupportedOperationException e)
		{
			return "Your phone does not seem to be supported, please email me!";
		}
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(fb.getFile().getAbsolutePath() + "/"
				+ nextPhotoName());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		// MediaStore.EX
		startActivityForResult(intent, CAPTURE_IMAGE);
		return null;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE) {
			if (resultCode == RESULT_OK) {
				new PictureTaker().execute();
			} else if (resultCode == RESULT_CANCELED) {
				shortToast("Picture was not taken");
			} else {
				shortToast("Picture was not taken");
			}
			fb.updateFileList();
		}
	}

	private class PictureTaker extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... urls) {
			return launchCamera();
		}

		protected void onPostExecute(String result) {
			if (result != null)
				shortToast(result);
		}

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.camera:
			new PictureTaker().execute();
			break;
		/*
		 * case R.id.gallery: launchGallery(); break;
		 */
		case R.id.newdir:
			promptNewDir();
			break;
		}

	}

}