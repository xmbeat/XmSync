package com.xmsoft.xmsync;

import android.os.AsyncTask;

public abstract class TaskSafe<D,F,G> extends AsyncTask<D, F, G> {
	private TaskSafeListener mListener;
	private Object[] mLastMessage;
	private G mResult;
	private int mStatus;
	/**
	 * Constructor que especifica quien va a recibir las notificaciones
	 * @param listener
	 */
	public TaskSafe(TaskSafeListener listener){
		mListener=listener;
	}
	
	public final TaskSafeListener getTaskSafeListener(){
		return mListener;
	}
	
	public final void setTaskSafeListener(TaskSafeListener listener)
	{
		mListener = listener;
		//Para llamar al ultimo mensaje de progreso
		if (mListener!=null && mLastMessage != null){
			mListener.onTaskProgress(mLastMessage);
		}
		//Terminado con exito
		if (mStatus == 1){
			this.onPostExecute(mResult);
		}
		//Cancelado
		else if (mStatus == 2){
			this.onCancelled();
		}
	}
	
	@Override
	protected G doInBackground(D... params) {
		return null;
	}
	@Override
	protected void onPostExecute(G param){
		mResult = param;
		if (mListener != null){
			mListener.onTaskFinish(param);
			mStatus = 1;
		}else{
			mStatus = 0;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override 
	protected void onProgressUpdate(F... params){
		mLastMessage = params;
		if (mListener!=null){
			mListener.onTaskProgress(params);
		}	
	}
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute(){
		if (mListener!=null){
			mListener.onTaskStart();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override 
	public void onCancelled(){
		if (mListener!=null){
			mListener.onTaskCancel(mResult);
			mStatus = 0;
		}else{
			mStatus = 2;
		}
	}
	/*
	 * Interfaz que recibe los mensajes de esta tarea
	 */
	public static interface TaskSafeListener{
		public void onTaskStart();
		public void onTaskProgress(Object...params);
		public void onTaskFinish(Object result);
		public void onTaskCancel(Object result);
	}
}
