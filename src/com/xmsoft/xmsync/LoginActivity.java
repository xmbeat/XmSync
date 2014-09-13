package com.xmsoft.xmsync;

import java.io.IOException;
import java.net.Socket;

import com.xmsoft.xmsync.SessionUtils.HeadAttrib;
import com.xmsoft.xmsync.SessionUtils.MetadataList;
import com.xmsoft.xmsync.TaskSafe.TaskSafeListener;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;

public class LoginActivity extends Activity implements OnCancelListener, TaskSafeListener{
	private EditText mUser, mPass, mHost, mPort;
	private ProgressDialog mDialog;
	private AlertDialog mAlert;
	private HostScanner mScanner;
	private static final int DEFAULT_PORT = 15000;
	public static final String PREFS_NAME = "XmSyncPreferences";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		mUser = (EditText)findViewById(R.id.user);
		mPass = (EditText)findViewById(R.id.password);
		mHost = (EditText)findViewById(R.id.remote_ip);
		mPort = (EditText)findViewById(R.id.port);
		mDialog = new ProgressDialog(this);
		mDialog.setOnCancelListener(this);
		mDialog.setTitle("Escaneando en la red LAN");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		alertDialogBuilder
			.setTitle("¿Desea escanear en la red LAN?")
			.setCancelable(false)
			.setPositiveButton("Si", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int port = DEFAULT_PORT;
							try{
								port = Integer.parseInt(mPort.getText().toString());
							}catch(NumberFormatException e){
								alertar("Puerto no válido, buscando en el puerto por defualt");
								mPort.setText(port + "");
							}
							mScanner = new HostScanner(LoginActivity.this, port);
							mScanner.execute();
						}
					})
			.setNegativeButton("No", null);
		
		mAlert = alertDialogBuilder.create();
        Object obj = getLastNonConfigurationInstance();
        if (obj instanceof HostScanner){
        	mScanner = (HostScanner)obj;
        	mDialog.show();
        	mScanner.setTaskSafeListener(this);
        }
        if (savedInstanceState == null){//Se carga por primera vez
            loadPreferences(); //Recuperamos lo guardado
        }
	}
	private void savePreferences(){
	     // We need an Editor object to make preference changes.
	      // All objects are from android.context.Context
	      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString("port", mPort.getText().toString());
	      editor.putString("host", mHost.getText().toString());
	      editor.putString("user", mUser.getText().toString());
	      editor.putString("pass", mPass.getText().toString());
	      // Commit the edits!
	      editor.commit();
	}
	private void loadPreferences(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    String port = settings.getString("port", DEFAULT_PORT + "");
	    String host = settings.getString("host", "");
	    String user = settings.getString("user", "");
	    String pass = settings.getString("pass", "");
	    mHost.setText(host);
	    mPort.setText(port);
	    mUser.setText(user);
	    mPass.setText(pass);   
	}
	 //Ejecutado cuando la Actividad interactua con el usuario.
	@Override
	public void onResume() {
		super.onResume();
		if (mScanner!=null){
			mScanner.setTaskSafeListener(this);
		}
	}
	@Override 
	public void onStop(){
		super.onStop();
		savePreferences();
	}
	
	//Ejecutado cuando cambia la configuracion del sistema
	 @Override
   public Object onRetainNonConfigurationInstance(){
    	if (mScanner != null){
    		mScanner.setTaskSafeListener(null);
    	}
    	return mScanner;
    }
	 
	public void btnClickLogin(View view){
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifi.isConnected()) {
			if (mUser.getText().toString().equals("")){
				alertar("Ingrese el nombre de usuario");
			}
			else if(mPass.getText().toString().equals("")){
				alertar("Ingrese la contraseña");
			}else if(mPort.getText().toString().equals("")){
				alertar("Ingrese el puerto del servidor");
			}else if (mHost.getText().toString().equals("")){
					mAlert.show();
			}else{
				try{
					int puerto = Integer.parseInt(mPort.getText().toString());
					Socket socket = new Socket(mHost.getText().toString(),puerto);
					MetadataList metadata = new MetadataList();
					metadata.add("USER", mUser.getText().toString());
					metadata.add("PASS", mPass.getText().toString());
					socket.getOutputStream().write(metadata.toString().getBytes());
					socket.getOutputStream().flush();
					metadata = SessionUtils.extractMetadata(socket.getInputStream());
					socket.close();
					if (metadata.size()>0 && metadata.get(0).getValue().equals("GOOD")){
						Intent intent = new Intent();
						intent.setClass(this, MenuActivity.class);
						intent.putExtra("com.xmsoft.xsync.Conexion", new Conexion(
								mHost.getText().toString(),
								puerto,
								mUser.getText().toString(),
								mPass.getText().toString()
								));
						startActivity(intent);
					}else{
						HeadAttrib attrib = metadata.getAttribByName("ERROR");
						if (attrib != null){
							alertar(attrib.getValue());
						}else{
							alertar("No se pudo conectar al servidor, compruebe su usuario y contraseña");
						}
					}
				}catch(NumberFormatException e){
					alertar("Ingrese un puerto válido");
				}catch(IOException e){
					alertar("No se pudo conectar con el servidor!");
				}
			}
		}else{
			Toast.makeText(this, "No está conectado al WIFI", Toast.LENGTH_SHORT).show();
		}
	}

	private void alertar(String alert){
		Toast.makeText(this, alert, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onCancel(DialogInterface dialog) {
		mScanner.cancel(true);
	}

	@Override
	public void onTaskStart() {
		mDialog.setMessage("");
		mDialog.show();
	}

	@Override
	public void onTaskProgress(Object... params) {
		mDialog.setMessage(params[0].toString());
	}

	@Override
	public void onTaskFinish(Object result) {
		mDialog.dismiss();
		mScanner = null;
		if (result != null){
			mHost.setText(result.toString());
		}else{
			Toast.makeText(this, "No se encontraron servidores", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onTaskCancel(Object result) {
		
		mScanner = null;
		Toast.makeText(this, "Busqueda Cancelada", Toast.LENGTH_SHORT).show();
	}
	/**
	 * Clase usada para hacer una busqueda de servidores, 
	 * primer parametro, param para doInBack, tec para lo que regresa, tercero progress
	 * @author xmbeat
	 *
	 */
	private class HostScanner extends TaskSafe<Void, String, String>{
		private int mPort;
		public HostScanner(TaskSafeListener listener, int port) {
			super(listener);
			mPort = port;
		}
		@Override
		protected String doInBackground(Void ...unused)
		{
			String ip = SessionUtils.getIPAddress(true);
			String prefix = ip.substring(0,ip.lastIndexOf(".")+1);
			for (int i = 0; i < 256 && !isCancelled(); i++){
				try{
					publishProgress("Escaneando: " + prefix + i + ":" + mPort);
					Socket socket = new Socket(prefix + i, mPort);
					socket.close();
					return prefix + i;
				}catch(IOException e){}
			}return null;
		}
	}
}
