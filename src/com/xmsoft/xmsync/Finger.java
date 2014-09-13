package com.xmsoft.xmsync;

public class Finger {
	private float mX, mY;
	private int mId;
	public float getDistance(Finger anotherFinger){
		float x = mX - anotherFinger.getX();
		float y = mY - anotherFinger.getY();
		return (float) Math.hypot(x, y);
	}
	public String toString(){
		return "ID:" + mId + "\tX:"+mX + "\tY" + mY;
	}
	public Finger(){
		
	}
	public Finger(int x, int y){
		mX = x; mY = y;
	}
	public Finger(int x, int y, int id){
		this(x,y);
		mId = id;
	}
	public void setID(int value){
		mId = value;
	}
	public int getID(){
		return mId;
	}
	public void setX(float x){
		mX = x;
	}
	public float getX(){
		return mX;
	}
	public void setY(float y){
		mY = y;
	}
	public float getY(){
		return mY;
	} 
	public void setXY(float x, float y){
		mX = x;mY=y;
	}
	
}
