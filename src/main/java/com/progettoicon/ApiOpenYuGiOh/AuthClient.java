package com.progettoicon.ApiOpenYuGiOh;

import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthClient {
    private static final String BASE_URL = "https://yugioh-open-api.vercel.app/v1/auth";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String bearerToken;
    private String apiKey;

    public void register(String username, String password) throws Exception {
        String url = BASE_URL + "/register";
        String json = objectMapper.writeValueAsString(Map.of("username", username, "password", password));

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Registrazione avvenuta con successo.");
            } else {
                throw new Exception("Errore nella registrazione: " + response.body().string());
            }
        }
    }

    public void login(String username, String password) throws Exception {
        String url = BASE_URL + "/login";
        String json = objectMapper.writeValueAsString(Map.of("username", username, "password", password));

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                Map<String, Object> responseData = objectMapper.readValue(responseBody, Map.class);
                this.bearerToken = (String) responseData.get("data"); // Estrai il token da "data"
                System.out.println("Login effettuato con successo.");
            } else {
                String errorBody = response.body().string();
                System.out.println("Errore nel login. Risposta server: " + errorBody);
                throw new Exception("Errore nel login: " + errorBody);
            }
        }
    }

    public void generateApiKey() throws Exception {
        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new Exception(
                    "Bearer token non valido. Assicurati di effettuare il login prima di generare l'API Key.");
        }

        String url = BASE_URL + "/generate-api-key";

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                Map<String, Object> responseData = objectMapper.readValue(responseBody, Map.class);
                Map<String, String> data = (Map<String, String>) responseData.get("data");

                String publicKey = data.get("public_key");
                String secretKey = data.get("secret_key");

                if (publicKey == null || secretKey == null) {
                    throw new Exception("Chiavi pubblica o segreta mancanti nella risposta.");
                }

                // Calcola la firma HMAC
                String signature = calculateHmacSignature(publicKey, secretKey);

                // Combina public_key e signature
                String pattern = publicKey + ":" + signature;

                // Codifica in Base64
                this.apiKey = Base64.getEncoder().encodeToString(pattern.getBytes());

                System.out.println("Chiave API generata con successo");
            } else {
                String errorBody = response.body().string();
                System.out.println("Errore nella generazione della chiave API. Risposta server: " + errorBody);
                throw new Exception("Errore nella generazione della chiave API: " + errorBody);
            }
        }
    }

    // Metodo per calcolare la firma HMAC
    private String calculateHmacSignature(String publicKey, String secretKey) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        hmac.init(secretKeySpec);

        byte[] hmacBytes = hmac.doFinal(publicKey.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}

// try {

// // AuthClient: Gestione autenticazione e API Key
// authClient = new AuthClient();
// // authClient.register("Ultralecce", "123456");
// authClient.login("Ultralecce", "123456");
// authClient.generateApiKey();

// // ApiClient: Recupero dati
// apiClient = new Client(authClient.getApiKey(), authClient.getBearerToken());

// } catch (Exception e) {

// System.err.println(e);
// }

// try {
// apiClient.getAllPublicDecks();
// } catch (Exception e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }