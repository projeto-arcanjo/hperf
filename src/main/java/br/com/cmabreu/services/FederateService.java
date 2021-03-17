package br.com.cmabreu.services;

import java.io.File;
import java.net.URL;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hperf.HPerfMain;

@Service
public class FederateService {
	private Logger logger = LoggerFactory.getLogger( FederateService.class );
	private URL[] modules;
	private String[] args;
	
    @Value("${federation.fomfolder}")
    String fomFolder;	

    @Value("${federation.name}")
    String federationName;	
    
    @PreDestroy
	public void onExit() {
		logger.info("Encerando Federado...");
		// 
	}
    
    
    
    // O ciclo de vida do Federado comaca aqui.
    public void startRti() throws Exception {
    	// Tudo relativo a inicializacao do Federado eh administrado exclusivamente
    	// pelo HPERF em HPerfMain.java
    	// Para efeito de praticidade, o codigo original sera mantido com poucas alteracoes
    	// ao inves de ser transcrito para o corpo deste Federado.
    	// eh muito mais facil e rapido iniciar o HPERF original pela sua classe Main
    	// do que tentar entender o codigo original e transcrever para ca.

    	
		this.modules = new URL[]{
			// The MIM file MUST be present	
			(new File( fomFolder + "HLAstandardMIM.xml")).toURI().toURL(),
			(new File( fomFolder + "testfom.fed")).toURI().toURL(),
		};
    	
    	// Resolvi colocar o inicio do Federado pelo REST
    	// para ele nao sair rodando ja de cara.
    	// O usuario precisarah chamar ele usando o endpoint "/start"
    	// veja no FederateController
    	
    }

    
    public void start( Integer interval, String federateName, String driver, String peers ) {
		// Driver pode ser { throughput-test, latency-test ou lifecycle-test }
		driver = "--" + driver;
		peers = "--peers " + peers;
		federateName = "--federate-name " + federateName;
		String feName = "--federation-name " + this.federationName;
		
		this.args = new String[]{driver, peers, federateName, feName};
		
		try {
			HPerfMain.init( this.modules, this.args);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

 	

	
	
}
