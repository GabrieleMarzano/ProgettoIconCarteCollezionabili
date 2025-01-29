package com.progettoicon;

public class Utente {
    private String nome;
    private String cognome;
    private String password;
    private String email;
    private String nickname;
    private int id;
    private int id_collezione;

    // Costruttore vuoto
    public Utente() {

    }

    // Costruttore completo
    public Utente(String nome, String cognome, String password, String email, String nickname, int id,
            int id_collezione) {
        this.nome = nome;
        this.cognome = cognome;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.id = id;
        this.id_collezione = id_collezione;
    }

    // Getter e Setter
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_collezione() {
        return id_collezione;
    }

    public void setId_collezione(int id_collezione) {
        this.id_collezione = id_collezione;
    }

    // Metodo toString
    @Override
    public String toString() {
        return "Utente{" +
                "nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", id=" + id +
                ", idCollezione=" + id_collezione +
                '}';
    }
}
