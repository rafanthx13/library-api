package br.com.rafanthx13.libraryapi.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate; // Data

@Data // Gera Getters, Setter, HashCode e isEquals
@AllArgsConstructor // Ter o contrutor com todos os parametos
@NoArgsConstructor // ter constutor sem parametro nenhum (vai combinar com o @Builder)
@Builder // Lombok. Serve para gerar o builder, uma forma mais legível de gerar o objeto
@Entity // Entidade JPA
public class Loan {

    @Id // PK
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY) // GERADO AUTO pelo Banco
    private Long id;

    @Column(length = 100)
    private String customer;

    @Column(name = "customer_email") // "customer_email" será o nome desse dado na tabela
    private String customerEmail;

    @JoinColumn(name = "id_book")
    @ManyToOne // Esta classe tem 1 para muitos: Há muito emprestimos para um livro. Se chamara id_book
    private Book book;

    @Column
    private LocalDate loanDate;

    @Column
    private Boolean returned;
}

/* Loan é o empréstimo de um livro:
+ id
+ A pessoa que pediu o livro
+ O livro
+ A data que pegou
+ Se foi devolvido ou não

*/