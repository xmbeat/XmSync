package com.xmsoft.xmsync;

public class BicolaEntrada{
	private int [] mElementos;
	private int head = -1;
	private int tail = -1;
	public BicolaEntrada(int size){
		mElementos = new int[size];
	}
	public boolean insertar(int dato){
		if (head + 1 < mElementos.length){
			head++;
			if (head == 0){
				tail = 0;
			}
			mElementos[head] = dato;
			return true;
		}else{
			System.err.println("No se puede insertar por el frente");
			return false;
		}
	}
	
	public int eliminarInicio(){
		if (head != -1){
			int aux = mElementos[head];
			head--;
			if (head < tail){
				head = tail = -1;
			}
			return aux;
		}else{
			System.err.println("No hay elementos que eliminar");
			return -1;
		}
	}
	
	public int eliminarFin(){
		if (tail != -1){
			int aux = mElementos[tail];
			tail++;
			if (tail > head){
				head = tail = -1;
			}
			return aux;
		}else{
			System.err.println("No hay elementos que eliminar");
			return -1;
		}
	}
	
}