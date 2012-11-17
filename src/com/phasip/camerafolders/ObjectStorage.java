package com.phasip.camerafolders;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


import android.content.Context;

public class ObjectStorage {
	public static boolean storeObject(Context c,Object o, String fileName) {
		try {
			FileOutputStream fos = c.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
			return true;
		} catch (IOException e) {
			//Log.d("Debugging","storeObject",e);
			return false;
		}
	}
	
	public static Object readObject(Context c,String fileName) {
			ObjectInputStream ois;
			FileInputStream fos;
			try {
				fos = c.openFileInput(fileName);
				ois = new ObjectInputStream(fos);
				Object ret =  ois.readObject();
				ois.close();
				return ret;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
}
