package com.xmsoft.xmsync;
public class ColaCircular{
	private int[]mElementos;
	private int head = -1;
	private int tail = -1;
	private int capacity;
	
	public ColaCircular(int capacity){
		this.capacity = capacity;
		mElementos = new int[capacity];
	}
	public boolean insertar(int dato) {
		
		int aux = nextPosition(head);
		if (aux != tail){
			head = aux;
			mElementos[head] = dato;
			if (tail == -1){
				tail = 0;
			}
			return true;
		}else{
			System.err.println("Overflow");
			return false;
		}
	}
	
	public boolean estaVacia(){
		return tail == -1;
	}
	
	public boolean estaLlena(){
		return tail == nextPosition(head);
	}
	
	public  int eliminar(){
		if (tail != -1){
			int aux = mElementos[tail];
			if (tail == head){
				tail = head = -1;
			}else{
				tail = nextPosition(tail);
			}
			return aux;
		}
		System.err.println("Underflow");
		return -1;
	}
	
	private int nextPosition(int index){
		return (index+1) % capacity;
	}
	
	public void imprimir(){
		int indice = tail-1;
		if (tail != -1){
			while(indice != head){
				indice = nextPosition(indice);
				System.out.print(mElementos[indice] + " "); 
			}
			System.out.println();
		}else{
			System.out.println("No hay elementos que mostrar");
		}
	}
}