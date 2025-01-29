
package com.progettoicon.InfoCarte;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progettoicon.Carta;

public class ApiYGOPRODeck {

    private static final String API_URL = "https://db.ygoprodeck.com/api/v7/cardinfo.php?misc=yes";
    private static final String API_URL_CARDSET = "https://db.ygoprodeck.com/api/v7/cardsets.php";

    private static final String API_URL_singola = "https://db.ygoprodeck.com/api/v7/cardinfo.php";

    public static List<Carta> getCardInfo() {

        List<Carta> listaCarte;
        try {
            // Crea l'oggetto URL per l'API
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Controlla il codice di risposta HTTP
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();

                // Usa Jackson per deserializzare la risposta JSON
                ObjectMapper objectMapper = new ObjectMapper();

                // Deserializza il JSON nella classe wrapper CardResponse
                CardResponse cardResponse = objectMapper.readValue(content.toString(),
                        CardResponse.class);

                // Restituisci la lista di carte dalla proprietà 'data'

                listaCarte = cardResponse.getData();

                return listaCarte;
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Restituisci null o una lista vuota in caso di errore
        return null;
    }

    public static Carta getCardInfoById(int cardId) {
        try {
            // Costruzione dell'URL
            String urlString = API_URL_singola + "?id=" + URLEncoder.encode(String.valueOf(cardId), "UTF-8")
                    + "&misc=yes";
            URL url = new URL(urlString);

            // Debug
            System.out.println("Debug - URL: " + url);

            // Connessione HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Leggi la risposta
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Parsing della risposta JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    CardResponse cardResponse = objectMapper.readValue(response.toString(), CardResponse.class);

                    // Ritorna la carta (assumendo che i dati siano in una lista)
                    return cardResponse.getData().get(0);
                }
            } else {
                System.err.println("GET request fallita. Codice di risposta: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Card_Set> getCardSet() {
        List<Card_Set> listaCarsSet = new ArrayList<>();
        try {
            // Crea l'oggetto URL per l'API
            URL url = new URL(API_URL_CARDSET);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Controlla il codice di risposta HTTP
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();

                // Usa Jackson per deserializzare la risposta JSON in una lista di Card_Set
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    // Deserializza direttamente in una lista di Card_Set
                    listaCarsSet = objectMapper.readValue(content.toString(), new TypeReference<List<Card_Set>>() {
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Controlla se la lista è stata popolata correttamente
                if (listaCarsSet != null && !listaCarsSet.isEmpty()) {
                    return listaCarsSet;
                } else {
                    System.out.println("Nessuna carta trovata nella risposta.");
                }
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Restituisci una lista vuota in caso di errore
        return new ArrayList<>();
    }
}
