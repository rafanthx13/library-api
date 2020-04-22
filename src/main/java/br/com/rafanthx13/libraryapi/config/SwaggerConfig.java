package br.com.rafanthx13.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2 // Habilitar Swagger
@Configuration // Pra o springBoot reconhecer que essa é uma classe de configração
public class SwaggerConfig {

    // Docket é arquivo para configuraçâo do Swagger. 
    // Possui o necessário para subir o Swagger
    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2) // tipo
                    .select() // para poder continuar a configurar
                    // RequestHandlerSelectors é a classe que decide que APIs vão ser documentadas
                    // Aqui estamos especificando para buscar os crontroller que estiverem nesse pacote
                    .apis( RequestHandlerSelectors.basePackage("br.com.rafanthx13.libraryapi.controller") ) // o que vai ser documentado
                    .paths(PathSelectors.any())
                    .build()
                    .apiInfo(apiInfo()); // informações
    }

    // 'ApiInfo' Objeto que tem as informações básicas. Inserido no Docket
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                    .title("Library API")
                    .description("API do Projeto de controle de aluguel de livros")
                    .version("1.0")
                    .contact(contact())
                    .build();
    }

    // 'Contact' tem as informaçôes de contato. INserida em ApiInfo
    private Contact contact(){
        return new Contact("Rafael Morais de Assis",
                "http://github.com/rafanthx13",
                "rafaassis15@gmail.com");
    }
}