package br.com.rafanthx13.libraryapi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/*
Serve para mapear os parametro que vinehram por URL. POderia faze rde outr forma como a do GET FILTER BOOK
*/
public class LoanFilterDTO {

    private String isbn;
    private String customer;

}