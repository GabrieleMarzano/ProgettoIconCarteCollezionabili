package com.progettoicon.ApiOpenYuGiOh;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Client {
    private static final String BASE_URL = "https://yugioh-open-api.vercel.app/v1";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private String bearerToken;

    public Client(String apiKey, String bearerToken) {

        this.apiKey = apiKey;
        this.bearerToken = bearerToken;
    }

    public void getAllCards() throws Exception {
        // URL base per le carte
        String url = BASE_URL + "/card";

        // Debug: stampa l'URL, il Bearer Token e l'API Key
        System.out.println("Debug - URL: " + url);
        System.out.println("Debug - Bearer Token: " + bearerToken);
        System.out.println("Debug - API Key: " + apiKey);

        // Controllo se i valori chiave sono nulli
        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new Exception("Bearer Token non valido o nullo.");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("API Key non valida o nulla.");
        }

        // Costruisci la richiesta HTTP
        Request request = new Request.Builder()
                .url(url)
                // .addHeader("Authorization", "Bearer " + bearerToken)
                // .addHeader("X-Api-key", apiKey)
                .get()
                .build();

        // Esegui la richiesta
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Debug - HTTP Response Code: " + response.code());

            if (response.isSuccessful()) {
                // Leggi e stampa la risposta JSON
                String jsonResponse = response.body().string();
                System.out.println("Risposta JSON: ");
                System.out.println(jsonResponse);
            } else {
                String errorBody = response.body().string();
                System.out.println("Errore nella risposta dell'API: " + errorBody);
                throw new Exception("Errore nella risposta dell'API: " + errorBody);
            }
        }
    }

    public void getAllPublicDecks() throws Exception {
        // URL base per il recupero dei deck
        String baseUrl = BASE_URL + "/deck/public";
        int page = 1; // Inizia dalla pagina 1
        int limit = 100; // Numero massimo di risultati per pagina (può essere cambiato)
        boolean hasNext = true; // Flag per continuare la paginazione

        // Controlla se bearerToken e apiKey sono nulli
        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new Exception("Bearer Token non valido o nullo.");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("API Key non valida o nulla.");
        }

        // Inizia la paginazione
        while (hasNext) {
            // Costruisci l'URL con i parametri per la pagina corrente
            String url = baseUrl + "?page=" + page + "&limit=" + limit;
            System.out.println("Debug - URL richiesto: " + url);

            // Costruisci la richiesta
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-Api-key", apiKey)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .get()
                    .build();

            // Esegui la chiamata
            try (Response response = client.newCall(request).execute()) {
                // Stampa debug del codice di risposta HTTP
                System.out.println("Debug - HTTP Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    // Leggi la risposta JSON
                    String jsonResponse = response.body().string();
                    System.out.println("JSON Response (Pagina " + page + "): " + jsonResponse);

                    // Parsing della risposta per controllare il flag `next`
                    Map<String, Object> responseData = objectMapper.readValue(jsonResponse, Map.class);

                    // Controlla se ci sono altre pagine
                    hasNext = (boolean) responseData.getOrDefault("next", false);

                    // Incrementa la pagina per la prossima iterazione
                    page++;

                    // Log aggiuntivi per debug
                    System.out.println("Debug - hasNext: " + hasNext);
                    System.out.println("Debug - Pagina corrente: " + (page - 1));
                } else {
                    // Stampa del corpo della risposta in caso di errore
                    String errorBody = response.body() != null ? response.body().string() : "Nessun contenuto";
                    System.out.println("Errore nella risposta dell'API: " + errorBody);
                    throw new Exception("Errore nella risposta dell'API: " + errorBody);
                }
            } catch (IOException e) {
                throw new Exception("Errore durante l'esecuzione della richiesta: " + e.getMessage(), e);
            }
        }

        System.out.println("Recupero di tutti i deck completato.");
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}