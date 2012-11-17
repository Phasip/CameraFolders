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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A small file browser view
 * 
 * @author Pasi Saarinen
 * 
 */
public class FileBrowser extends ListView implements ViewHandler<File>, OnItemClickListener {
	private static final int ID_RENAME = 0;
	private static final int ID_SYMLINK = 1;
	private static final int ID_ADDFAV = 3;
	private static final int ID_DELETE = 4;
	private boolean showHidden = false;
	private final ArrayList<File> files = new ArrayList<File>();
	private File currFile = new File("/");
	private SpecArrayAdapter<File> arrayAdapter;
	private FileHandler fh = null;
	private FavList fl = null;
	

	public void showHidden(boolean yes) {
		showHidden = yes;
	}

	public void updateFileList() {
		updateFileList(currFile);
	}

	private void shortToast(String msg) {
		Context context = this.getContext();
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	public File getFile() {
		return currFile;
	}

	public boolean canSavePicture() {
		if (currFile == null)
			return false;
		return currFile.canWrite();
	}

	public void updateFileList(File f) {
		// views.clear();
		if (f == null)
			f = new File("/");
		if (!f.isDirectory()) {
			if (fh == null)
				shortToast("Trying to enter non-directory");
			else
				fh.handleFileClick(f);

			return;
		}
		if (!f.canRead()) {
			shortToast("Cannot Read Directory");
			return;
		}
		if (!f.canWrite()) {
			shortToast("This directory is Read-Only.");
		}
		// Log.i("qweqwe", "File list updated!");

		currFile = f;
		if (fh != null)
			fh.handleFolderChange(f);
		files.clear();
		File arr[] = f.listFiles();
		if (showHidden) {
			if (arr != null)
				files.addAll(Arrays.asList(arr));
		} else {
			for (File i : arr) {
				if (i == null)
					continue;
				if (i.getName().startsWith("."))
					continue;
				files.add(i);

			}
		}
		if (fh != null)
			Collections.sort(files, fh);
		arrayAdapter.notifyDataSetChanged();
	}

	public void setHandlers(FileHandler f, FavList l) {
		fh = f;
		fl = l;
	}

	private void initMe(Context c) {
		arrayAdapter = new SpecArrayAdapter<File>(c, R.layout.listitem, files, this);

		this.setAdapter(arrayAdapter);
		this.setTextFilterEnabled(true);
		this.setOnItemClickListener(this);
		// updateFileList();

	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		ArrayList<File> list = files;
		File f = list.get(info.position);
		String name = f.getName();
		menu.setHeaderTitle(name);
		menu.add(Menu.NONE, ID_RENAME, 0, "Rename");
		if (f.isDirectory()) {
			menu.add(Menu.NONE, ID_ADDFAV, 0, "Add to fav");
		} else {
			//menu.add(Menu.NONE, ID_DELETE, 0, "Delete");
		}

	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final File f = files.get(info.position);
		if (item.getItemId() == ID_SYMLINK) {
			if (fh != null)
				fh.handleFolderChange(f);

			return true;
		}
		if (item.getItemId() == ID_ADDFAV) {
			fl.addFav(f);
			return true;
		}
		if (item.getItemId() == ID_DELETE) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
			builder.setTitle(f.getName());
			builder.setMessage(R.string.delete_file);

			builder.setPositiveButton(R.string.ok_menu_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					f.delete();
					shortToast("File deleted");
					updateFileList();
				}
			});
			builder.setNegativeButton(R.string.abort_menu_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shortToast("Aborted");
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		if (item.getItemId() == ID_RENAME) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
			builder.setTitle(f.getName());
			builder.setMessage(R.string.rename_file);
			// Set an EditText view to get user input
			final EditText input = new EditText(this.getContext());
			String fileName = f.getName();
			int ext = fileName.lastIndexOf('.');
			final String fileExt = (ext > 0) ? fileName.substring(ext) : "";
			if (f.isDirectory())
				input.setText(fileName);
			else
				input.setText((ext > 0) ? fileName.substring(0, ext) : fileName);
			builder.setView(input);
			builder.setPositiveButton(R.string.ok_menu_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					File parent = f.getParentFile();
					if (parent == null) {
						shortToast("Fails to rename " + f.getName());
						return;
					}
					String newname = parent.getAbsolutePath() + File.separatorChar + input.getText() + fileExt;
					if (f.isDirectory())
						newname = parent.getAbsolutePath() + File.separatorChar + input.getText();
					// Log.d("FileBrowser","Renaming from " +
					// f.getAbsolutePath());
					// Log.d("FileBrowser","Renaming from " +
					// parent.getAbsolutePath());
					// Log.d("FileBrowser","Renaming to " + newname);
					File n = new File(newname);
					if (f.renameTo(n))
						shortToast("Renamed " + f.getName() + " to " + n.getName());
					else
						shortToast("Fails to rename " + f.getName() + " - Check file permissions");
					updateFileList();
				}
			});
			builder.setNegativeButton(R.string.abort_menu_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shortToast("File rename aborted");
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		return true;
	}

	public File[] getFiles() {
		if (getFile() == null)
			return null;

		return getFile().listFiles();
	}

	public boolean hasFile(String name) {
		File arr[] = getFiles();
		for (int a = 0; a < arr.length; a++) {
			File f = arr[a];
			// Log.i("dfweqwe","Checking f: " + arr[a] + " - name: " + name);
			// Yes case sensitive filesystem, but windows users use ntfs...
			if (f.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public FileBrowser(Context context) {
		super(context);
		initMe(context);
	}

	public FileBrowser(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMe(context);
	}

	public FileBrowser(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMe(context);
	}

	@Override
	public File handle(View v, File object) {
		View t = v.findViewById(R.id.listitemSUB);
		TextView main = (TextView) t.findViewById(R.id.maintext);
		TextView desc = (TextView) t.findViewById(R.id.desctext);
		ImageView img = (ImageView) v.findViewById(R.id.icon);

		main.setText(" " + object.getName());
		if (object.isFile()) {
			img.setImageResource(android.R.drawable.ic_menu_save);
			desc.setText(" File");
			/*
			 * if(object.getName().endsWith(".jpg")) { //Bitmap b =
			 * BitmapFactory.decodeFile(object.getAbsolutePath()); // to get
			 * your Bitmap object and set it to an ImageView with
			 * ImageView.setImageBitmap(). //Bitmap c =
			 * Bitmap.createScaledBitmap(b, 96, 96, false); Bitmap b =
			 * views.get(object); if (b == null) {
			 * Log.d("Decoding!!","QWEQWEQW"); b = decodeFile(object);
			 * views.put(object,b); } img.setImageBitmap(decodeFile(object)); }
			 */
		}
		if (object.isDirectory()) {
			img.setImageResource(android.R.drawable.ic_menu_slideshow);
			desc.setText(" Directory");
		}
		return null;
	}

	// decodes image and scales it to reduce memory consumption
	@SuppressWarnings("unused")
	private Bitmap decodeFile(File f) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 65;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final File f = (File) parent.getItemAtPosition(position);
		updateFileList(f);

	}

}
