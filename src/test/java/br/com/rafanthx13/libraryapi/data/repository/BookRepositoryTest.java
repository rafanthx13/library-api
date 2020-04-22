package br.com.rafanthx13.libraryapi.data.repository;
/*
Como vamos testar o JPA, esse será um TESTSE DE INTEGRAÇÃO
*/
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.rafanthx13.libraryapi.data.entity.Book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@ExtendWith(SpringExtension.class) // Necessário pois é String
@ActiveProfiles("test") // Necessário apra os Testse
@DataJpaTest // Indica que vou fazer testes com JPA. Vai executar um banco em memória para executar os tests
// Os testes serâo em H2: Um banco em memória que será usado agora
public class BookRepositoryTest {

    // Serve para configurar o cenário. EentityManger será como o estado do banco
	@Autowired 
    TestEntityManager entityManager;

    // repository é a forma de acessar o banco
    @Autowired // REpositório injetado para executar os tests
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenario: crio um livro e já insiro direto na minha base de dados
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book); // insere na minha base de dados fictícia
        //execucao
        boolean exists = repository.existsByIsbn(isbn);
        //verificacao
        assertThat(exists).isTrue();
    }

    // Mesmo caso que o anterior, mas sem ter inserido o livro na base dade dados
    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesntExist(){
        //cenario
        String isbn = "123";
           //execucao
        boolean exists = repository.existsByIsbn(isbn);

        //verificacao
        assertThat(exists).isFalse();
    }

    // test de getByID

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findByIdTest(){
        //cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        //execucao
        Optional<Book> foundBook = repository.findById(book.getId());

        //verificacoes
        assertThat(foundBook.isPresent()).isTrue();
    }

    // CREATE

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest(){
        // Cria um livro
        Book book = createNewBook("123");
        // Insere o livro e o retorno é o que foi inserido
        Book savedBook = repository.save(book); 
        // verifica se o que voltou é válido
        assertThat( savedBook.getId() ).isNotNull();

    }

    // DELETE

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        // pra deletar, eu tenho que ter o livro salvo
        Book book = createNewBook("123");
        entityManager.persist(book);
        // Depois eu vou buscar por esse livro, direto pelo entityManager
        Book foundBook = entityManager.find( Book.class, book.getId() );
        // deleto o que retornou
        repository.delete(foundBook);
        // busco denovo e verifico se relamente nao tem nada la
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();

    }

    public static Book createNewBook(String isbn) {
        return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
    }


}