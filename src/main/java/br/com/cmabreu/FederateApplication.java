package br.com.cmabreu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("br.com.cmabreu")
public class FederateApplication {

	// Eh aqui onde tudo comeca.
	// Inicio da aplicacao.
	// Isso irah carregar o FederateService, dando inicio ao processo de registro
	// e inicializacao deste Federado atraves do arquivo "StartupAppListener" no Package "config".
	// procure o metodo "federateService.startRti()" para comecar a acompanhar
	// o fluxo de inicializacao
	public static void main(String[] args) {
		SpringApplication.run( FederateApplication.class, args );
	}

}
