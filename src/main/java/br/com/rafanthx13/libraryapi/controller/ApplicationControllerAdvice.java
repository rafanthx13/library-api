package br.com.rafanthx13.libraryapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import br.com.rafanthx13.libraryapi.exception.ApiErrors;
import br.com.rafanthx13.libraryapi.exception.BusinessException;

/*
Adiciona outras configurações a todos os controllers
+ @ExceptionHandler
  - Serve para podemos cadastrar handler para lidr com certas exceções
*/
@RestControllerAdvice
public class ApplicationControllerAdvice {

    // Erros de Validação
    @ExceptionHandler(MethodArgumentNotValidException.class) // Quando alguem jogar essa Exception
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Retorna 4040 BAD_REQUEST
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult(); // Pega a lista de erros que tiver aqui dentro
        return new ApiErrors(bindingResult);
    }

    // Erros de Regra de Negócio
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(BusinessException ex){
        return new ApiErrors(ex);
    }

    // ResponseEntity: Objeto de resposta; 
    // Usado no LoanController::Post quando der erro ao buscar pelo ISBN
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException( ResponseStatusException ex ){
        return new ResponseEntity(new ApiErrors(ex), ex.getStatus());
    }
}