package br.com.rafanthx13.libraryapi.data.dto;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lombok é uma lib JAVA que gera getter e settere em tempo de compilação
@Getter
@Setter
@Builder // gera um builder que facilita criar esse objeto
// O '@Builder' permite fazer:  Book savedBook = Book.builder().id(10l).author("Artur").title("As aventuras").isbn("001").build();
@NoArgsConstructor // Gera um construtor sem argumento
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @NotEmpty
    private String isbn;
  
}