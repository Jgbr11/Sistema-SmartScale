package br.com.milscale.milscale.application;

public interface EnvioDeEmail {
    void enviar(String destinatario, String assunto, String corpo);
}
