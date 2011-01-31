package com.phasip.camerafolders;

import android.view.View;

public interface ViewHandler<T> {
	public T handle(View v, T object);
}
