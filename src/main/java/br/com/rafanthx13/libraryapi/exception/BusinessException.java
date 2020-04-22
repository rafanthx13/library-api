package br.com.rafanthx13.libraryapi.exception;

// Exceptiond a regra de negócio: ao cadastrar um livro com ISBN dupluicado;
public class BusinessException extends RuntimeException {
    /**
     *
     */
    // private static final long serialVersionUID = 1L;

    public BusinessException(String s) {
        super(s);
    }
}