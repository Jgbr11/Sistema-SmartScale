package br.com.milscale.milscale.adapters.web;

/** Corpo padrao de toda resposta de erro da API - o front le a chave "erro". */
public record ErroResposta(String erro) {}
