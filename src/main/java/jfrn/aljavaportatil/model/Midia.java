package jfrn.aljavaportatil.model;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Midia {
	private final StringProperty nome;
	private final StringProperty duracao;
	private final StringProperty caminho;
	private final StringProperty tamanho;
	private final StringProperty tipo;
	private final StringProperty resolucao;
	private final File arquivo;
	
	
	public Midia(String nome, String caminho, String tamanho, String duracao, String resolucao, String tipo, File arquivo) {
		this.nome = new SimpleStringProperty(nome);
		this.caminho = new SimpleStringProperty(caminho);
		this.tamanho = new SimpleStringProperty(tamanho);
		this.duracao = new SimpleStringProperty(duracao);
		this.resolucao = new SimpleStringProperty(resolucao);
		this.tipo = new SimpleStringProperty(tipo);
		this.arquivo = arquivo;
	}

	public StringProperty getNome() {
		return nome;
	}

	public StringProperty getDuracao() {
		return duracao;
	}

	public StringProperty getCaminho() {
		return caminho;
	}

	public StringProperty getTamanho() {
		return tamanho;
	}

	public StringProperty getResolucao() {
		return resolucao;
	}

	public StringProperty getTipo() {
		return tipo;
	}

	public File getArquivo() {
		return arquivo;
	}
	
	
	

}
