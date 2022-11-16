package br.com.zup.edu.nossalojavirtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NossaLojaVirtualApplication {

	public static void main(String[] args) {
		SpringApplication.run(NossaLojaVirtualApplication.class, args);
	}

}
