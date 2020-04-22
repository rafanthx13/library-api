package br.com.rafanthx13.libraryapi.exception;

import br.com.rafanthx13.libraryapi.exception.BusinessException;

import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

  private List<String> errors;

  // Contrutor que colocar em 'this.erros' um array de erros que vinher de bidingResult
  // Captura erros de Validação
  public ApiErrors(BindingResult bindingResult) {
    this.errors = new ArrayList<>();
    bindingResult.getAllErrors().forEach( 
      error -> this.errors.add(error.getDefaultMessage())  
    );
  }

  public ApiErrors(BusinessException ex) {
    this.errors = Arrays.asList(ex.getMessage());
  }

 public ApiErrors(ResponseStatusException ex) {
      this.errors = Arrays.asList(ex.getReason());
  }


  public List<String> getErrors() {
    return errors;
  }

}