package com.xmsoft.xmsync;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class InternWidget {
	private Rect mRect;
	private Bitmap mCurrentBitmap, mNormalBitmap, mDownBitmap, mScaledNormal, mScaledDown;
	private int mX, mY, mW, mH;
	public InternWidget(Bitmap normalBmp, Bitmap downBmp, int x, int y, int w, int h){
		mRect = new Rect();
		mDownBitmap = downBmp;
		mX = x;
		mY = y;
		mW = w;
		mH = h;
		mScaledNormal = ajustarImagen(normalBmp, w, h);
		if (downBmp != null){
			mScaledDown = ajustarImagen(downBmp,  w, h);
		}
		mCurrentBitmap = mScaledNormal;
	}
	private Bitmap ajustarImagen (Bitmap bmp, int width, int height) {
		int bW = bmp.getWidth();
		int bH = bmp.getHeight();
		if (bH > bW){
			bW = bW*height/bH;
			bH = height;
		}else{
			bH = bH*width/bW;
			bW = width;
		}
	    return Bitmap.createScaledBitmap(bmp, bW, bH, true);
	}
	protected void setCurrentBitmap(Bitmap bmp){
		mCurrentBitmap = bmp;
	}
	public void onPaint(Canvas canvas, Paint paint){
		int x = mX + (mW- mCurrentBitmap.getWidth())/2;
		int y = mY + (mH - mCurrentBitmap.getHeight())/2;
		canvas.drawBitmap(mCurrentBitmap,x, y, paint);
	}
	public Rect getRect(){
		mRect.left = mX;
		mRect.top = mY;
		mRect.right = mX + mW;
		mRect.bottom = mY + mH;
		return mRect;
	}
	public void onPressed(){
		if (mDownBitmap!=null)
			mCurrentBitmap = mScaledDown;
	}
	public void onRelease(){
		mCurrentBitmap=mScaledNormal;
	}
	public int getX(){
		return mX;
	}
	public int getY(){
		return mY;
	}
	
	public void setCenter(int x, int y){
		mX = x - mCurrentBitmap.getWidth() / 2;
		mY = y - mCurrentBitmap.getHeight() / 2;
	}
	public void setX(int x){
		mX = x;
	}
	public void setY(int y){
		mY  = y;
	}
	public int getWidth(){
		return mW;
	}
	public int getHeight(){
		return mH;
	}
}
