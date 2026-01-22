package br.com.serverest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Login {
    
    private String email;
    private String password;
    
    public Login() {}
    
    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public static LoginBuilder builder() {
        return new LoginBuilder();
    }
    
    // Getters and Setters
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
    
    public static class LoginBuilder {
        private String email;
        private String password;
        
        public LoginBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public LoginBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public Login build() {
            return new Login(email, password);
        }
    }
}