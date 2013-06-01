package com.phasip.camerafolders;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

/**
 * ImageCapturer a Camera intent extension that handles faulty android camera
 * implementations
 * 
 * Usage: Start activity with: Note that CAMERA_ACTIVITY is a local int that you
 * specify to be able to identify the result in onActivityResult
 * ImageCapturer.takePicture(this, CAMERA_ACTIVITY, "/mnt/sdcard/DCIM/" +
 * System.currentTimeMillis() + ".jpg"); and capture the result in
 * onActivityResult: protected void onActivityResult(int requestCode, int
 * resultCode, Intent data) { if (requestCode == CAMERA_ACTIVITY) { String file
 * = ImageCapturer.getResult(resultCode, data); shortToast(file); } }
 * 
 * 
 * @author Pasi Saarinen
 * 
 */

public class ImageCapturer extends Activity {
	private static final int CAPTURE_IMAGE = 2;
	private static final int CAPTURE_VIDEO = 123;
	private static final String APP_NAME = "IMAGE_CAPTURER";
	private static final String EXTRA_ERROR = null;
	private static final String EXTRA_VIDEO_PATH = "VIDEO_PATH";
	private String imagelocation;
	private int lastImageId;
	private String error;
	public static String EXTRA_IMAGE_PATH = "IMAGE_PATH";

	/**
	 * Launches an ImageCapturer activity
	 * 
	 * @param a
	 *            Parent activity
	 * @param activityid
	 *            The request code that will be used in onActivityResult
	 * @param path
	 *            Full path to the location you want the picture in
	 */
	public static void takePicture(Activity a, int activityid, String path) {
		Intent i = new Intent(a.getApplicationContext(), ImageCapturer.class);
		if (path != null) {
			i.putExtra(EXTRA_IMAGE_PATH, path);
		}
		a.startActivityForResult(i, activityid);
		return;
	}

	/**
	 * Launches an ImageCapturer activity
	 * 
	 * @param a
	 *            Parent activity
	 * @param activityid
	 *            The request code that will be used in onActivityResult
	 * @param path
	 *            Full path to the location you want the picture in
	 */
	public static void takeVideo(Activity a, int activityid, String path) {
		Intent i = new Intent(a.getApplicationContext(), ImageCapturer.class);
		if (path != null) {
			i.putExtra(EXTRA_VIDEO_PATH, path);
		}
		a.startActivityForResult(i, activityid);
		return;
	}

	/**
	 * Parses the result from a ImageCapturer activity that has been returned to
	 * onActivityResult
	 * 
	 * @param resultCode
	 *            As passed to onActivityResult
	 * @param data
	 *            As passed to onActivityResult
	 * @return The path of the image or null if capture is canceled or fails
	 */
	public static String getResult(int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return null;
		}
		if (data == null) {
			return null;
		}
		Bundle b = data.getExtras();
		if (b == null) {
			return null;
		}

		return (String) b.getString(EXTRA_IMAGE_PATH);
	}

	/**
	 * Parses the result from a ImageCapturer activity that has been returned to
	 * onActivityResult
	 * 
	 * @param resultCode
	 *            As passed to onActivityResult
	 * @param data
	 *            As passed to onActivityResult
	 * @return The path of the image or null if capture is canceled or fails
	 */
	public static String getError(int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED) {
			return null;
		}
		if (data == null) {
			return null;
		}
		Bundle b = data.getExtras();
		if (b == null) {
			return null;
		}

		return (String) b.getString(EXTRA_ERROR);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		imagelocation = this.getIntent().getStringExtra(EXTRA_IMAGE_PATH);
		Log.d(APP_NAME, "Starting with imagelocation: " + imagelocation);
		Log.d(APP_NAME, "onCreate!");
		if (imagelocation != null) {
			launchCamera();
			Log.d(APP_NAME, "camera: " + imagelocation);
			return;
		}
		imagelocation = this.getIntent().getStringExtra(EXTRA_VIDEO_PATH);
		if (imagelocation != null) {
			Log.d(APP_NAME, "video: " + imagelocation);
			launchVideo();
			return;
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Launch the camera application
	 * 
	 * @return
	 */
	private String launchVideo() {
		// Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");

		lastImageId = getLastVideoId();
		if (imagelocation == null) {
			imagelocation = "/mnt/sdcard/DCIM/" + System.currentTimeMillis()
					+ ".m4v";
		}
		// File f = new File(imagelocation);
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		Log.d(APP_NAME, "Lastimageid is: " + lastImageId);
		try {
			startActivityForResult(intent, CAPTURE_VIDEO);
		} catch (ActivityNotFoundException ex) {
			Toast.makeText(
					this,
					"Your device does not contain an application to run this action",
					Toast.LENGTH_LONG).show();
		}

		return null;
	}

	/**
	 * Launch the camera application
	 * 
	 * @return
	 */
	private String launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		lastImageId = getLastImageId();
		if (imagelocation == null) {
			imagelocation = "/mnt/sdcard/DCIM/" + System.currentTimeMillis()
					+ ".jpg";
		}
		File f = new File(imagelocation);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		Log.d(APP_NAME, "Lastimageid is: " + lastImageId);
		startActivityForResult(intent, CAPTURE_IMAGE);
		return null;
	}

	private int getLastImageId() {
		return getLastId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	}

	private int getLastVideoId() {
		return getLastId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
	}

	/**
	 * Returns the id of the last image in database.
	 * 
	 * @return
	 */
	private int getLastId(Uri externalContentUri) {
		final String[] imageColumns = { MediaStore.MediaColumns._ID };
		final String imageOrderBy = MediaStore.MediaColumns._ID + " DESC";
		final String imageWhere = null;
		final String[] imageArguments = null;
		Cursor imageCursor = this.getContentResolver().query(
				externalContentUri, imageColumns, imageWhere, imageArguments,
				imageOrderBy);
		int id = Integer.MAX_VALUE;
		if (imageCursor.moveToFirst()) {
			id = imageCursor.getInt(imageCursor
					.getColumnIndex(MediaStore.MediaColumns._ID));
		}
		imageCursor.close();
		Log.d(APP_NAME, "getLastId found id: " + id);
		return id;

	}

	/**
	 * Handle result from camera
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri type;
		if (requestCode == CAPTURE_IMAGE) {
			type = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		} else if (requestCode == CAPTURE_VIDEO) {
			type = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		} else {
			return;
		}

		String msg = null;
		if (resultCode == RESULT_OK) {
			/* Camera thinks it got a good picture */
			msg = files_cleanup(type, lastImageId, imagelocation);
			Intent returnIntent = new Intent();
			returnIntent.putExtra(EXTRA_IMAGE_PATH, msg);
			setResult(RESULT_OK, returnIntent);
			finish();
			return;

		} else if (resultCode == RESULT_CANCELED) {
			Log.d(APP_NAME, "onActivityResult, canceled");
			if (imagelocation != null) {

				File f = new File(imagelocation);
				Log.d(APP_NAME, "onActivityResult, canceled, removing file: "
						+ imagelocation);
				f.delete();
			}
		} else {
			Log.d(APP_NAME, "onActivityResult, Unknown resultcode: "
					+ resultCode);
			if (imagelocation != null) {
				File f = new File(imagelocation);
				Log.d(APP_NAME,
						"onActivityResult, Unknown resultcode, removing file: "
								+ imagelocation);
				f.delete();
			}
		}
		Intent returnIntent = new Intent();
		returnIntent.putExtra(EXTRA_ERROR, error);
		setResult(RESULT_CANCELED, returnIntent);
		finish();

	}

	/**
	 * Handles the different bad cameras in android with many special cases,
	 * tries to remove duplicate and empty compies of the image and store the
	 * best available image in myPicPath
	 * 
	 * @param lastCaptureId
	 *            Result from getLastImageId() before camera launch
	 * @param myPicPath
	 *            Path you want to place picture in, the same as set in camera
	 *            extra
	 * @return
	 */
	private String files_cleanup(Uri extContUri, int lastCaptureId,
			String myPicPath) {
		/*
		 * Checking for duplicate images This is necessary because some camera
		 * implementation not only save where you want them to save but also in
		 * their default location.
		 */

		final String[] imageColumns = { MediaStore.MediaColumns.DATA,
				MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns._ID, };
		final String imageOrderBy = MediaStore.MediaColumns._ID + " DESC";
		final String imageWhere = MediaStore.MediaColumns._ID + ">?";
		final String[] imageArguments = { Integer.toString(lastCaptureId) };
		Cursor imageCursor = this.getContentResolver().query(extContUri,
				imageColumns, imageWhere, imageArguments, imageOrderBy);

		File goodPath = null;
		if (myPicPath != null) {
			goodPath = new File(myPicPath);
		}
		if ((imageCursor == null) || (!imageCursor.moveToFirst())) {
			Log.d(APP_NAME, "We fail to find a new image");
			imageCursor.close();
			if (goodPath != null && goodPath.exists()) {
				return goodPath.getAbsolutePath();
			} else {
				error = "We fail to capture image, please check remaining storage space and latest android update (1)";
				return null;
			}
		}
		String path = null;
		do {
			path = imageCursor.getString(imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
		} while (path.contentEquals(myPicPath) && imageCursor.moveToNext());

		if (imageCursor.isAfterLast()) {
			Log.d(APP_NAME, "We fail to find a new image (2)");
			imageCursor.close();
			if (goodPath != null && goodPath.exists()) {
				return goodPath.getAbsolutePath();
			} else {
				error = "We fail to capture image, please check remaining storage space and latest android update (2)";
				return null;
			}
		}

		int id = imageCursor.getInt(imageCursor
				.getColumnIndex(MediaStore.Images.Media._ID));
		Long size = imageCursor.getLong(imageCursor
				.getColumnIndex(MediaStore.Images.Media.SIZE));
		imageCursor.close();

		Log.d(APP_NAME, "From:" + path + " to " + myPicPath);
		File cameraSaved = new File(path);
		if (goodPath == null) {
			if (cameraSaved.exists()) {
				return cameraSaved.getAbsolutePath();
			} else {
				error = "Could not find captured image";
				return null;
			}
		}
		// Ensure it's there, check size, and delete!
		if ((cameraSaved.exists()) && (goodPath.length() < size)) {
			Log.d(APP_NAME, "CameraSaved is bigger!");
			if (goodPath.exists() && !goodPath.delete()) {
				Log.d(APP_NAME,
						"We fail to delete the smaller, saving smaller deleting bigger");
				cameraSaved.delete();
				error = "We fail to delete the smaller, saving smaller deleting bigger";
				return goodPath.getAbsolutePath();
			}

			if (!moveFile(cameraSaved, goodPath)) {
				if (goodPath.exists()) {
					Log.d(APP_NAME,
							"We fail to move the bigger, saving smaller deleting bigger");
					cameraSaved.delete();
					error = "We fail to move the bigger, saving smaller deleting bigger";
					return goodPath.getAbsolutePath();
				} else {
					error = "We fail to save picture, please upgrade your android version or email developer.";
					return null;
				}
			}
		}

		Log.d(APP_NAME, "Deleting camerasaved from database!!!");
		ContentResolver cr = getContentResolver();
		ContentValues s = new ContentValues();
		s.put(MediaStore.MediaColumns.DATA, goodPath.getAbsolutePath());
		cr.update(extContUri, s, MediaStore.Images.Media._ID + "=?",
				new String[] { Long.toString(id) });
		// cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		// MediaStore.Images.Media._ID + "=?", new String[] { Long.toString(id)
		// });
		return goodPath.getAbsolutePath();
	}

	/**
	 * Simple private function that moves a file from one location to another
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean moveFile(File from, File to) {
		if (from.renameTo(to)) {
			return true;
		}

		try {
			to.createNewFile();
			FileChannel source = null;
			FileChannel destination = null;
			try {
				source = new FileInputStream(from).getChannel();
				destination = new FileOutputStream(to).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
