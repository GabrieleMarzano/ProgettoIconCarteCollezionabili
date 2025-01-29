package com.progettoicon.APIYugiohPrices;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiYugiohPrices {
    private static final String BASE_URL = "https://yugiohprices.com/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ApiYugiohPrices() {
        // Costruttore privato per evitare istanziazione
    }

    public static List<CardData> getCardPrice(String cardName) throws Exception {
        // Rimuovi caratteri speciali dal nome per migliorare la compatibilità con l'API
        String sanitizedCardName = cardName.replace("#", "").trim();

        // Costruzione dell'URL
        String url = BASE_URL + "/get_card_prices/" + URLEncoder.encode(sanitizedCardName, StandardCharsets.UTF_8);

        System.out.println("Nome della carta: " + cardName);
        // Creazione della richiesta HTTP
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();

                // Mappa la risposta JSON
                Map<String, Object> root = objectMapper.readValue(jsonResponse, Map.class);

                if (!"success".equals(root.get("status"))) {
                    System.err.println("Errore: Carta \"" + cardName + "\" non trovata nell'API.");
                    return Collections.emptyList(); // Ritorna una lista vuota
                }

                return objectMapper.convertValue(
                        root.get("data"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CardData.class));
            } else {
                throw new Exception("Errore nella risposta dell'API: " + response.body().string());
            }
        } catch (IOException e) {
            throw new Exception("Errore durante l'esecuzione della richiesta: " + e.getMessage(), e);
        }
    }

    // Recupera i dati di una carta
    public static Map<String, Object> getCardData(String cardName) throws Exception {
        String url = BASE_URL + "/card_data/" + cardName.replace(" ", "%20");
        return executeRequestAsMap(url);
    }

    // Recupera il prezzo di un booster pack
    public static List<Map<String, Object>> getBoosterPackPrice(String boosterPackName) throws Exception {
        String url = BASE_URL + "/boosterpack_name/" + boosterPackName.replace(" ", "%20");
        return executeRequest(url);
    }

    // Recupera tutti i set disponibili
    public static List<Map<String, Object>> getAllSets() throws Exception {
        String url = BASE_URL + "/card_sets";
        return executeRequest(url);
    }

    // Utility: esegue la richiesta e restituisce i dati come List<Map>
    private static List<Map<String, Object>> executeRequest(String url) throws Exception {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, List.class);
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Nessun contenuto";
                throw new Exception("Errore nella risposta dell'API: " + errorBody);
            }
        } catch (IOException e) {
            throw new Exception("Errore durante l'esecuzione della richiesta: " + e.getMessage(), e);
        }
    }

    // Utility: esegue la richiesta e restituisce i dati come Map
    private static Map<String, Object> executeRequestAsMap(String url) throws Exception {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                return objectMapper.readValue(jsonResponse, Map.class);
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Nessun contenuto";
                throw new Exception("Errore nella risposta dell'API: " + errorBody);
            }
        } catch (IOException e) {
            throw new Exception("Errore durante l'esecuzione della richiesta: " + e.getMessage(), e);
        }
    }
}