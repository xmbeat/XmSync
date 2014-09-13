package com.xmsoft.xmsync;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class FileViewAdapter extends ArrayAdapter<FileView> {
	private List<FileView> mFiles;
	private boolean mStates[];
	public FileViewAdapter(Context context, List<FileView> files) {
		super(context, R.layout.file_row);
		mFiles = files;
		mStates = new boolean[files.size()];
	}

	@Override
	public int getCount(){
		return mFiles.size();
	}

	@Override
	public FileView getItem(int index){
		return mFiles.get(index);
	}
	public boolean []getStates(){
		return mStates;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		//Si nos toca crear un View, para ,mostrar una fila
		if (row == null){
			LayoutInflater inflater = (LayoutInflater) 
					this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.file_row, parent, false);
			
		}
		FileView file = getItem(position);
		((TextView)row.findViewById(R.id.file_title)).setText(file.getTitle());
		((TextView) row.findViewById(R.id.file_description)).setText(file.getDescription());
		((ImageView)row.findViewById(R.id.file_image)).setImageBitmap(file.getBitmap());
		CheckBox chk = (CheckBox)row.findViewById(R.id.file_check);
		chk.setChecked(mStates[position]);
		chk.setOnClickListener(mListener);
		chk.setTag(position);
		return row;
	}
	
	private OnClickListener mListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			CheckBox box = (CheckBox)v;
			Integer position = (Integer)box.getTag();
			mStates[position] = box.isChecked();
		}
	};
}
