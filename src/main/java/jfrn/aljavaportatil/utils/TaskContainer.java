package jfrn.aljavaportatil.utils;

import java.io.BufferedReader;
import java.util.concurrent.Callable;

/**
 * TaskContainer é uma classe que implementa Callable para realizar uma ação. Seu objetivo
 * é realizar uma ação que prenderia uma thread, dentro de uma task a ser chamada com um timeout
 * desse modo, não há o risco da thread ficar congelada aguardando o readline de uma stream, por exemplo
 * @author marcel
 *
 */
public class TaskContainer implements Callable<String>{

	BufferedReader reader;
	
	public TaskContainer(BufferedReader receivedReader) {
		this.reader = receivedReader;
	}
	
	@Override
	public String call() throws Exception {
		return reader.readLine();
	}
	
}
