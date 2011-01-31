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
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
/**
 * A application that takes pictures and puts them in a folder
 * @author Pasi Saarinen
 *
 */
public class CameraFolders extends Activity implements OnClickListener {
	// private static final String APP_NAME = "Lecture Cam";
	private final int CAPTURE_IMAGE = 2;
	Uri imageUri = null;
	private FileBrowser fb;

	/* Load the default folder */
	private void loadSettings() {
		final SharedPreferences settings = getPreferences(0);
		File f = new File(settings.getString("default_folder", Environment
				.getExternalStorageDirectory().toString()));
		if (!f.exists())
			f = Environment.getRootDirectory();
		fb.updateFileList(f);
	}
	
	/** Move one folder back and if the current folder is root end application */
	public void onBackPressed() {
		File f = fb.getFile();
		File p = f.getParentFile();
		if (p == null) {
			this.finish();
			return;
		}
		fb.updateFileList(p);
	}
	
	/**
	 * Show a short toast
	 * @param msg the toast to show
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
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		if (item.getItemId() == R.id.setdefault) {
			editor.putString("default_folder", fb.getFile().getAbsolutePath());
			shortToast("Default folder: " + fb.getFile().getAbsolutePath());
		}
		editor.commit();
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
	 * @param id Id of the resource.
	 */
	private void setClickListener(int id) {
		View v = this.findViewById(id);
		v.setOnClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fb = (FileBrowser) this.findViewById(R.id.fileView);
		setClickListener(R.id.camera);
		// setClickListener(R.id.gallery); //Removed, didn't get it to work right.
		setClickListener(R.id.newdir);
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

	protected void launchCamera() {

		// create parameters for Intent with filename
		ContentValues values = new ContentValues();
		// values.put(MediaStore.Images.Media.DATA,"/sdcard/file_moo.jpg");
		values.put(MediaStore.Images.Media.TITLE, "Image");
		values.put(MediaStore.Images.Media.DESCRIPTION,
				"Image capture by camera");
		// imageUri is the current activity attribute, define and save it for
		// later usage (also in onSaveInstanceState)
		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(fb.getFile().getAbsolutePath() + "/"
				+ nextPhotoName());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		// MediaStore.EX
		startActivityForResult(intent, CAPTURE_IMAGE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE) {
			if (resultCode == RESULT_OK) {
				new PictureTaker().execute();
				fb.updateFileList();
			} else if (resultCode == RESULT_CANCELED) {
				shortToast("Picture was not taken");
				fb.updateFileList();
			} else {
				shortToast("Picture was not taken");
			}
		}
	}

	private class PictureTaker extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... urls) {
			launchCamera();
			return null;
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