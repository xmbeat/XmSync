package com.xmsoft.xmsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import static com.xmsoft.xmsync.SessionUtils.MetadataList;
public class FileViewActivity extends Activity implements OnItemClickListener {
	private ListView mList;
	private String mCurrentPath = "/";
	private final String KEY_CURRENT_PATH = "current_path";
	private Conexion mConexion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_view);
		mList = (ListView)findViewById(R.id.list_view_files);
		mList.setOnItemClickListener(this);
		mConexion  = (Conexion)getIntent().getSerializableExtra("com.xmsoft.xsync.Conexion");
		if (savedInstanceState!=null){
			mCurrentPath = savedInstanceState.getString(KEY_CURRENT_PATH);
		}else{
			populateList();
		}
	}

	private void populateList(){
		try{
			Socket socket = SessionUtils.connectToServer(mConexion);
			if (socket == null){
				throw new IOException();
			}
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			MetadataList headers = new MetadataList();
			headers.add("LS", mCurrentPath);
			os.write(headers.toString().getBytes());
			os.flush();
			headers = SessionUtils.extractMetadata(is);
			is.close();
			os.close();
			socket.close();
			/*
			 * 	XmServer-Linux/1.0 GOOD
			 *	FILE-COUNT 25
			 */
			ArrayList<FileView> lista = new ArrayList<FileView>();
			for (int i = 2;i < headers.size(); i++){
				String description = headers.get(i).getName();
				String title = headers.get(i).getValue();
				FileView element = new FileView();
				element.setDescription(description);
				element.setTitle(title);
				if (description.charAt(0)=='d'){
					element.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.folder));
				}
				lista.add(element);
			}
			mList.setAdapter(new FileViewAdapter(this, lista));
			
		}catch(IOException e){
			Toast.makeText(this, "Error en la conexion", Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_file_view, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
		
	}

}
