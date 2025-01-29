package com.progettoicon;

import java.util.Scanner;

public class Login {

    private final Database yugioh_db;
    private Scanner scanner = new Scanner(System.in);
    private String email;
    private String password;

    public Login(Database yugioh_db) {

        this.yugioh_db = yugioh_db;

    }

    public boolean verificaCredenziali() {

        boolean b = false;
        boolean email_trovata;
        String email_tmp;
        String password_tmp;

        String password_db;

        // Creazione di uno Scanner per acquisire input da tastiera
        // Scanner scanner = new Scanner(System.in);

        try {
            // Lettura dell'email
            System.out.println("Inserisci Email: ");
            email_tmp = scanner.nextLine();

            // Lettura della password
            System.out.println("Inserisci Password: ");
            password_tmp = scanner.nextLine();

            // Verifica se l'email esiste nel database
            email_trovata = Database.readEmail(yugioh_db.getConn(), email_tmp);

            if (email_trovata) {

                // Lettura della password associata all'email dal database
                password_db = Database.readPassword(yugioh_db.getConn(), email_tmp);

                // Confronto della password inserita con quella memorizzata
                if (password_tmp.equals(password_db)) {

                    email = email_tmp;
                    password = password_tmp;
                    b = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la verifica delle credenziali: " + e.getMessage());
        } // finally {
          // // Chiusura dello Scanner
          // scanner.close();
          // }

        return b;
    }

    public void chiudiScanner() {

        scanner.close();
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

}
