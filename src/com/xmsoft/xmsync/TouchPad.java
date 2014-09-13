package com.xmsoft.xmsync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.view.*;
public class TouchPad  extends SurfaceView implements SurfaceHolder.Callback{
	private float mX1,mY1;//Usado par
	private long mLastTime;//Usado para medir el tiempo desde la ultima accion
	private Finger mDedo1;
	private Finger mDedo2;
	private float mFactor = 4.0f;
	private float mMinVeloc = 0.02f;
	private float mMinDistance = 2.0f;
	private float mScrollDistance = 20;
	private int mClickTime = 150;
	private TouchPadListener mListener;
	private Paint mPaint;
	private int mColor = 0xFFFFFFFF;
	private int mWidth, mHeight;
	private InternWidget mScrollTop, mScrollBottom, mRightClick,  mMouse;
	private boolean mIsDown, mIsDownScrollTop, mIsDownScrollBottom, mIsDownRight;
	private SurfaceHolder  mHolder;
	
	
	
	
	public static interface TouchPadListener{
		public void onClick(int button);
		public void onMouseDown(int button);
		public void onMouseUp(int button);
		public void onScroll(boolean isUp);
		public void onMouseMove(float distanceX, float distanceY);
	}
	
	public void setBackgroundColor(int color){
		mColor = color;
		refresh();
	}
	
	public TouchPad(Context context, TouchPadListener listener) {
		super(context);
		mListener = listener;
		mDedo1 = new Finger();
		mDedo2 = new Finger();
		Bitmap normal = BitmapFactory.decodeResource(getResources(), R.drawable.touch_right_normal);
		Bitmap pressed = BitmapFactory.decodeResource(getResources(), R.drawable.touch_right_pressed);
		mRightClick = new InternWidget(normal, pressed,  100, 100, 120, 120);
		normal = BitmapFactory.decodeResource(getResources(), R.drawable.touch_corner);
		pressed = BitmapFactory.decodeResource(getResources(), R.drawable.touch_corner_pressed);
		mScrollTop = new InternWidget(normal, pressed, 200, 0, 120, 120);
		mScrollBottom = new InternWidget(normal, pressed, 200, 200, 120, 120);
		normal = BitmapFactory.decodeResource(getResources(), R.drawable.touch_pressed);
		pressed = null;
		mMouse = new InternWidget(normal, null, 0, 0, 96, 96);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(1.0f);
		getHolder().addCallback(this);
	}
	
	public void setClickTime(int time){
		mClickTime = time;
	}
	public int getClickTime(){
		return mClickTime;
	}
	public void setMinDistanceToReact(float distance){
		mMinDistance = distance;
	}
	public float getMinDistanceToReact(){
		return mMinDistance;
	}
	public void setAccelerationFactor(float factor){
		mFactor = factor;
	}
	
	public float getAccelerationFactor(){
		return mFactor;
	}
	
	public void setMinVeloc(float veloc){
		mMinVeloc = veloc;
	}
	
	public float getMinVeloc(){
		return mMinVeloc;
	}
	
	public void setScrollDistance(float distance){
		mScrollDistance = distance;
	}
	public float getScrollDistance(){
		return mScrollDistance;
	}
	
	public void onDraw(Canvas canvas){
		canvas.drawColor(mColor);
		if (mIsDown){
			mMouse.onPaint(canvas, mPaint);
		}
		mScrollBottom.onPaint(canvas, mPaint);
		mScrollTop.onPaint(canvas, mPaint);
		mRightClick.onPaint(canvas, mPaint);

	}
	/**
	 * Interceptamos los movimientos del dedo en el Dispositivo
	 */
	public boolean onTouchEvent(MotionEvent event){
		int action = event.getAction();
		//dentro del "action" se encuentra el codigo de accion y el dedo que la hizo
		int actionCode = action & MotionEvent.ACTION_MASK;
		int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
				>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);
		switch (actionCode){
		//Un dedo toco el touch
		case MotionEvent.ACTION_DOWN://Un dedo abajo solo uno
			mDedo1.setID(event.getPointerId(pointerIndex));
			mDedo1.setXY(x,y);
			mX1 = x;
			mY1 = y;
			mLastTime = event.getEventTime();
			
			if(mScrollBottom.getRect().contains((int)x, (int) y)){
				mIsDownScrollBottom = true;
				mScrollBottom.onPressed();
			}
			else if (mScrollTop.getRect().contains((int)x, (int) y)){
				mIsDownScrollTop = true;
				mScrollTop.onPressed();
			}
			else if ( mRightClick.getRect().contains((int)x, (int)y)){
				mIsDownRight = true;
				mRightClick.onPressed();
			}
			mIsDown = true;
			mMouse.setCenter((int)x, (int)y);
			break;
		//Un segundo dedo toco el touch
		case MotionEvent.ACTION_POINTER_DOWN:
			mDedo2.setID(event.getPointerId(pointerIndex));
			mDedo2.setXY(x,y);
			break;
		//Algun dedo se ha movido
		case MotionEvent.ACTION_MOVE:
			//Solo un dedo moviendose, mover la grafica
			if (event.getPointerCount() == 1){
				
		        //Obtenemos la distancias recorridas de un momento al otro.
				float distanciaX = x - mDedo1.getX();
				float distanciaY = y - mDedo1.getY();
				//Actualizamos la posicion de la imagen a mostrar
				mMouse.setCenter((int)x, (int)y);
				if (mIsDownScrollBottom || mIsDownScrollTop){
					if (Math.abs(distanciaY) >= mScrollDistance){
						mDedo1.setY(y);
						int movs = (int)Math.abs(distanciaY / mScrollDistance);
						for (int i = 0; i < movs; i++){
							mListener.onScroll(distanciaY < 0);
						}
					}
					mDedo1.setX(x);
				}else{
					//Obtenemos la diferencia de tiempos
					long tiempo = event.getEventTime() - mLastTime;
					//Obtenemos la hipotenusa del triangulo, es decir, la distancia de un punto (x,y) a (a,b)
					float distancia = (float)Math.sqrt(distanciaX*distanciaX + distanciaY*distanciaY);
					//Si la distancia es menor a la que se supone reaccionaremos, entonces no hacemos nada
					if (distancia < mMinDistance) 
						return true; 
					//El angulo del vector formado, se puede con arctan, pero las medidas las da de 90° a -90°
					double angulo = Math.atan2(distanciaY, distanciaX);
					//Actualizamos la nueva distancia en base a su velocidad y factor de aceleracion (ya está resumida la ecuacion)
					float newDistance = 
							tiempo * (mMinVeloc - mFactor*mMinVeloc) + distancia*mFactor;
					//Si la nueva distancia es menor que la distancia original (debido a una desaceleracion) se restaura a la original
					if (newDistance < distancia){
						newDistance = distancia;
					}
					
					distanciaX = (float)(Math.cos(angulo) * newDistance);
					distanciaY = (float)(Math.sin(angulo) * newDistance);
					//Actualizamos la posicion del dedo y el tiempo
					mDedo1.setXY(x, y);
					mLastTime = event.getEventTime();
					//Enviamos la notificacion
					mListener.onMouseMove(distanciaX, distanciaY);
				}
			}
			else// Dos dedos moviendose, usar el scroll en base al segundo dedo
			if (event.getPointerCount() == 2){
				int indexDedo2 = event.findPointerIndex(mDedo2.getID());
				//Obtenemos las coordenadas del dedo2
				x = event.getX(indexDedo2);
				y = event.getY(indexDedo2);
				float difY =  y - mDedo2.getY();
				if (Math.abs(difY) >= mScrollDistance){
					mDedo2.setY(y);
					int movs = (int)Math.abs(difY / mScrollDistance);
					for (int i = 0; i < movs; i++){
						mListener.onScroll(difY < 0);
					}
				}
				mDedo2.setX(x);
			}
	        break;
	    
	    //Algun dedo se ha levantado, mas no los dos.
		case MotionEvent.ACTION_POINTER_UP:
			//Se levanto el primer dedo			
			if (event.getPointerId(pointerIndex) == mDedo1.getID()){
				mDedo1.setID(mDedo2.getID());
				mDedo1.setXY(mDedo2.getX(), mDedo2.getY());
				mDedo1.setID(mDedo2.getID());
				
			}else{
				mDedo2.setID(-1);
			}
			break;
		//El ultimo dedo se ha levantado
		case MotionEvent.ACTION_UP:
			mDedo1.setID(-1);
			//El boton derecho estaba presionado
			if (mIsDownRight){
				if (mRightClick.getRect().contains((int)x, (int)y)){
					mListener.onClick(3);
				}
			}else if(!mIsDownScrollBottom && !mIsDownScrollTop){
				long tiempo = event.getEventTime() - mLastTime;
				float distanciaX = x - mX1;
				float distanciaY = y - mY1;
				double distancia = Math.hypot(distanciaX, distanciaY);
				if (distancia < 10.0 && tiempo <= mClickTime){
					mListener.onClick(1);
				}										
			}
			mIsDownRight = false;
			mIsDownScrollBottom = false;
			mIsDownScrollTop = false;
			mIsDown = false;
			mScrollBottom.onRelease();
			mScrollTop.onRelease();
			mRightClick.onRelease();
			break;
		}
		refresh();
		return true;
	}
	private void refresh(){
		Canvas canvas = null;
		try{
			if (mHolder!=null){
				canvas = mHolder.lockCanvas();
				if (canvas!=null)
					this.onDraw(canvas);
			}
		}finally{
			if (canvas!= null && mHolder != null)
				mHolder.unlockCanvasAndPost(canvas);
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mWidth = width;
		mHeight = height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas();
		mWidth = canvas.getWidth();
		mHeight = canvas.getHeight();
		holder.unlockCanvasAndPost(canvas);
		mScrollBottom.setCenter(mWidth, mHeight);
		mScrollTop.setCenter(mWidth, 0);
		mRightClick.setCenter(mWidth, mHeight / 2);
		mHolder = holder;
		refresh();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
}
