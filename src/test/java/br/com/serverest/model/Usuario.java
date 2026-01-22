package br.com.serverest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usuario {
    
    private String _id;
    private String nome;
    private String email;
    private String password;
    private String administrador;
    
    public Usuario() {}
    
    public Usuario(String _id, String nome, String email, String password, String administrador) {
        this._id = _id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.administrador = administrador;
    }
    
    public static UsuarioBuilder builder() {
        return new UsuarioBuilder();
    }
    
    // Getters and Setters
    public String get_id() {
        return _id;
    }
    
    public void set_id(String _id) {
        this._id = _id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAdministrador() {
        return administrador;
    }
    
    public void setAdministrador(String administrador) {
        this.administrador = administrador;
    }
    
    public static class UsuarioBuilder {
        private String _id;
        private String nome;
        private String email;
        private String password;
        private String administrador;
        
        public UsuarioBuilder _id(String _id) {
            this._id = _id;
            return this;
        }
        
        public UsuarioBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }
        
        public UsuarioBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UsuarioBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UsuarioBuilder administrador(String administrador) {
            this.administrador = administrador;
            return this;
        }
        
        public Usuario build() {
            return new Usuario(_id, nome, email, password, administrador);
        }
    }
}