package jfrn.aljavaportatil.conversor;


public interface ProgressListener  {

	public void progress (int arquivoAtual, int porcentagem);
	
	public void progress (int porcentagem);
	
	public void message (String mensagem);
	
	public void complete(String pathToFile);
	
	public void error();

}
