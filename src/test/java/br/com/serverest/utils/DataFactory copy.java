package br.com.serverest.utils;

import br.com.serverest.model.Login;
import br.com.serverest.model.Usuario;
import com.github.javafaker.Faker;

import java.util.Locale;

public class DataFactory {
    
    private static final Faker faker = new Faker(new Locale("pt-BR"));
    
    public static Usuario criarUsuarioValido(boolean administrador) {
        return Usuario.builder()
                .nome(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password(8, 16))
                .administrador(administrador ? "true" : "false")
                .build();
    }
    
    public static Login criarLoginValido(Usuario usuario) {
        return Login.builder()
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .build();
    }
    
    public static String gerarEmailAleatorio() {
        return faker.internet().emailAddress();
    }
    
    public static String gerarNomeAleatorio() {
        return faker.name().fullName();
    }
}