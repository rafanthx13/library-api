package br.com.rafanthx13.libraryapi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
/*
Caracteriza JSON do método PATH, para mudar somente um úncio atributo 
: returned, ou seja, que o lviro foi devolvido
*/
public class ReturnedLoanDTO {
    private Boolean returned;
}