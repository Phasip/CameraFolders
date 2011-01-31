package com.phasip.camerafolders;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/* Okay I should probably write a own listadapter but I'm lazy!
 * This class works like an arrayadapter except it takes a ViewHandler (see interface)
 * that is called handle(View v, T object) so that you can edit the values of v based
 * on what you want to show from T.
 * eg:
 * 
 * 
 * public File handle(View v, File object) {
		TextView main = (TextView) v.findViewById(R.id.maintext);
		TextView desc = (TextView) v.findViewById(R.id.desctext);
		ImageView img = (ImageView) v.findViewById(R.id.icon);

		main.setText(object.getName());
		if (object.isFile()) {
			img.setImageResource(android.R.drawable.ic_menu_save);
			desc.setText("File");
		}
		if (object.isDirectory()) {
			img.setImageResource(android.R.drawable.ic_menu_slideshow);
			desc.setText("Directory");
		}
		return null;
	}
	
 */
public class SpecArrayAdapter<T> extends ArrayAdapter<T> {
	/* Theese two are from the original ArrayAdapter,not public so we do what they do. */
	private int mFieldId; 
	private LayoutInflater mInflater;
	private ViewHandler<T> vHandler;
	public SpecArrayAdapter(Context context, int textViewResourceId,List<T> objects,ViewHandler<T> vHandler) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFieldId = textViewResourceId;
		this.vHandler = vHandler;
	}
	
	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mFieldId);
    }

	public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mFieldId);
    }
	
	protected View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        View view;
        
        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        T item = getItem(position);
        vHandler.handle(view,item);
        return view;
    }


}
