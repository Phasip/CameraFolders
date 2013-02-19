package com.phasip.camerafolders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FavList extends ListView implements ViewHandler<File>, OnItemClickListener {
	private static final int ID_DELFAV = 132;
	private SpecArrayAdapter<File> arrayAdapter;
	private List<File> files = new ArrayList<File>();
	private CameraFolders cam;
	public static final String FAV_STORAGE = "Favourites";
	
	public boolean saveFiles() {
		return ObjectStorage.storeObject(cam, files, FAV_STORAGE);
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadFiles() {
		List<File> files_new = (List<File>)ObjectStorage.readObject(cam, FAV_STORAGE);
		files.clear();
		if (files_new == null) {
			return false;
		}
		files.addAll(files_new);
		return true;
	}
	public FavList(Context context) {
		super(context);
		initMe(context);
	}

	public FavList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMe(context);
			}

	public FavList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMe(context);
			}
	
	public void initMe(Context context) {
		arrayAdapter = new SpecArrayAdapter<File>(context, R.layout.listitem, files, this);
		this.setAdapter(arrayAdapter);
		this.setTextFilterEnabled(true);
		this.setOnItemClickListener(this);
	}
	public void setHandler(CameraFolders cam) {
		this.cam = cam;
		loadFiles();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final File f = (File) parent.getItemAtPosition(position);
		cam.setFolder(f);
		Settings settings = Settings.getInstance(cam);
		if (settings.isShowVideo() || !settings.isStartCamera()) {
			cam.tabs.setCurrentTabByTag(CameraFolders.TAB_BROWSE);
		} else {
			cam.launchCamera(false);
		}

	}

	@Override
	public File handle(View v, File object) {
		View t = v.findViewById(R.id.listitemSUB);
		TextView main = (TextView) t.findViewById(R.id.maintext);
		TextView desc = (TextView) t.findViewById(R.id.desctext);
		ImageView img = (ImageView) v.findViewById(R.id.icon);

		main.setText(" " + object.getName());
		if (object.isDirectory()) {
			img.setImageResource(android.R.drawable.star_big_off);
			desc.setText(object.getAbsolutePath());
		} else {
			desc.setText("Bad Favorite!");
		}
		return null;
	}
	public boolean removeFav(File f) {
		if (files.remove(f)) {
			arrayAdapter.notifyDataSetChanged();
			saveFiles();
			return true;
		}
		return false;
	}
	public boolean addFav(File f) {
		if (files.contains(f)) {
			return false;
		}
		files.add(f);
		Collections.sort(files);
		saveFiles();
		arrayAdapter.notifyDataSetChanged();
		return true;
	}
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final File f = files.get(info.position);
		if (item.getItemId() == ID_DELFAV) {
			removeFav(f);
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		File f = files.get(info.position);
		String name = f.getName();
		menu.setHeaderTitle(name);
		menu.add(Menu.NONE, ID_DELFAV, 0, "Remove Favourite");
	}
}
