package com.xmsoft.xmsync;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.xmsoft.xmsync.SessionUtils.MetadataList;

import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

public class MouseActivity extends Activity implements TouchPad.TouchPadListener{
	private Socket mSocket; 
	private Conexion mConexion;
	private TouchPad mTouch;
	private OutputStream mOut;
	private int mWidth;
	private int mHeight;
	private float mCurX;
	private float mCurY;
	private MouseMoveSender mSender;
	private static final byte[] CPOS ={'C','P','O','S', ' ', 'R', 'A', 'L', 'A', 'T','I', 'V', 'E'};
	private static final byte[] POSY = {'P','O','S', 'Y', ' '};
	private static final byte[] POSX = {'P','O','S','X', ' '};
	private static final byte[] SCROLL = {'S', 'C','R','O','L','L', ' '};
	private static final byte[] CLICK= {'C','L','I','C','K', ' '};
	private byte mBufferX[] = new byte[5];
	private byte mBufferY[] = new byte[5];
	private int mBrillo;
	private int mMinBrillo = 5;
	private static final int COLOR_BAD = 0xFFFF0000;
	private static final int COLOR_OK = 0xFFEAEAEA;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
        		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mTouch = new TouchPad(this,this);
		mTouch.setMinDistanceToReact(1.0f);
		mTouch.setMinVeloc(0.05f);//100px * segundo
		mTouch.setScrollDistance(20.0f);
		this.addContentView(mTouch, param);
		try {
			mBrillo = android.provider.Settings.System.getInt(getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS);
			
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		if (savedInstanceState != null){
			mConexion = (Conexion) savedInstanceState.getSerializable("com.xmsoft.xsync.Conexion");
		}
		else{
			mConexion = (Conexion) getIntent().getSerializableExtra("com.xmsoft.xsync.Conexion");
		}
	}
	
	@Override
	public void onStop(){
		super.onStop();
		mSender.terminar();
		disconnect(false);		
	}
	public void onResume(){
		super.onResume();
		android.provider.Settings.System.putInt(getContentResolver(), 
				android.provider.Settings.System.SCREEN_BRIGHTNESS,
				mMinBrillo);
		android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = mMinBrillo/255.0f;
		getWindow().setAttributes(params);
	}
	public void onPause(){
		super.onPause();
		android.provider.Settings.System.putInt(getContentResolver(), 
				android.provider.Settings.System.SCREEN_BRIGHTNESS,
				mBrillo);
	}
	@Override 
	public void onStart(){
		super.onStart();
		mSender = new MouseMoveSender(1);
		mSender.start();
		connect();
	}
	@Override
	public void onSaveInstanceState(Bundle bundle){
		bundle.putSerializable("com.xmsoft.xsync.Conexion", mConexion);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_mouse, menu);
		return true;
	}

	@Override
	public void onClick(int button) {
		try{
			if (mOut != null){
				mOut.write(CPOS);
				mOut.write('\n');
				mOut.write(CLICK);
				mOut.write('0' + button);
				mOut.write('\n');
				mOut.write('\n');
				mOut.flush();
			}
		}
		catch(IOException e){
			disconnect(true);
		}
	}
	private void disconnect(boolean colorear){
		try{
			mOut.close();
			mSocket.close();
			mSocket = null;
			mOut = null;
		}catch(Exception e){
			
		}
		if (colorear)
			mTouch.setBackgroundColor(COLOR_BAD);
	}
	private boolean connect(){
		//Si no se ha establecido la conexion, ya sea por que no hay red o porque es un usuario invalido
		if (mSocket == null){
			try{
				//Nos tratamos de conectar, si arroja excepcion es que se fue la red, si el valor devuelto es null, es usuario y/o pass erroneos
				mSocket = SessionUtils.connectToServer(mConexion);
				if (mSocket != null){
					mOut = mSocket.getOutputStream();
					mOut.write("DISPLAY PROPS\n\n".getBytes());
					MetadataList headers = SessionUtils.extractMetadata(mSocket.getInputStream());
					if (headers.get(0).getValue().equals("GOOD")){
						mWidth = headers.getAttribByName("WIDTH").getIntValue();
						mHeight = headers.getAttribByName("HEIGHT").getIntValue();
					}else{
						throw new IOException("El servidor no puede regresar informacion necesaria");
					}
					mTouch.setBackgroundColor(COLOR_OK);
					return true;
				}
			}
			catch(IOException e){
				mSocket = null;
				mOut = null;
			}
			mTouch.setBackgroundColor(COLOR_BAD);
			return false;
		}
		mTouch.setBackgroundColor(COLOR_OK);
		return true;
	}
	@Override
	public void onMouseDown(int button) {
		
	}

	@Override
	public void onMouseUp(int button) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onScroll(boolean isUp) {
		try{
			if (mOut != null){
				mOut.write(CPOS);
				mOut.write('\n');
				mOut.write(SCROLL);
				mOut.write(isUp?'1':'0');
				mOut.write('\n');
				mOut.write('\n');
				mOut.flush();
			}
		}
		catch(IOException e){
			disconnect(true);
		}
	}
	private int numberToCharArray(int number, byte[]buffer){
		boolean negative = number < 0;
		if (negative){
			number = (0xFFFFFFFF ^ number)+1;
		}
		int index = 4;//Donde colocaremos el siguiente numero
		do{
			int residuo = number % 10;
			buffer[index--] = (byte)('0' + residuo);
			number = number / 10;
			
		}while(number!=0);
	
		if (negative){
			buffer[index--] = '-';
		}
		return index+1;
	}
	@Override
	public void onMouseMove(float distanceX, float distanceY) {
		mCurX += distanceX;
		mCurY += distanceY;
		if (mCurX < 0){
			mCurX = 0;
		}else if (mCurX > mWidth){
			mCurX = mWidth;
		}
		if (mCurY < 0){
			mCurY = 0;
		}else if (mCurY > mHeight){
			mCurY = mHeight;
		}
		mSender.sendMove((int)mCurX, (int)mCurY);
		//mSender.sendMove((int)distanceX, (int) distanceY);
	}
	
	private class MouseMoveSender extends Thread{
		private boolean mFinish;
		private ColaCircular mColaX, mColaY;
		public MouseMoveSender(int maxEncolados){
			mColaX = new ColaCircular(maxEncolados);
			mColaY = new ColaCircular(maxEncolados);

		}
		public void run(){
			while(!mFinish){
				if (!mColaX.estaVacia()){
					try{
						if (mOut != null){
							int indexX, indexY;
							synchronized (this) {
								indexX = numberToCharArray(mColaX.eliminar(), mBufferX);
								indexY = numberToCharArray(mColaY.eliminar(), mBufferY);
							}

							mOut.write(CPOS);
							mOut.write('\n');
							mOut.write(POSX);
							mOut.write(mBufferX, indexX, 5 - indexX);
							mOut.write('\n');
							mOut.write(POSY);
							mOut.write(mBufferY, indexY, 5 - indexY);
							mOut.write('\n');
							mOut.write('\n');
							mOut.flush();
						}
					}
					catch(IOException e){
						if (!mFinish)
							disconnect(true);
					}
				}
				
			}
		}
		public synchronized void sendMove(int x, int y){
			if(!mColaX.estaLlena()){
				mColaX.insertar(x);
				mColaY.insertar(y);
			}
		}		
		public void terminar(){
			mFinish = true;
		}
	}
}
