package com.progettoicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.progettoicon.APIYugiohPrices.ApiYugiohPrices;
import com.progettoicon.APIYugiohPrices.CardData;
import com.progettoicon.APIYugiohPrices.Prices;
import com.progettoicon.InfoCarte.ApiYGOPRODeck;
import com.progettoicon.InfoCarte.Banlist_info;
import com.progettoicon.InfoCarte.CardPrices;
import com.progettoicon.InfoCarte.CardSets;
import com.progettoicon.InfoCarte.Card_Set;
import com.progettoicon.ReteNeurale.CardTrainingRecord;
import com.progettoicon.ReteNeurale.VettoreCardCompatibility;

public class Database {

    private Connection conn = null;
    private String url;
    private String user;
    private String password;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Database(String url, String user, String password) throws Exception {
        this.url = url;
        this.user = user;
        this.password = password;

        try {
            // Connessione al database
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connessione al database avvenuta con successo!");

            // Controlla se la tabella è vuota e carica le carte
            if (isTableEmpty("SELECT COUNT(*) FROM Carte")) {
                System.out.println("Tabella Carte vuota, caricamento in corso...");
                caricaCarte(conn);
                System.out.println("Caricamento delle carte completato.");
            } else {
                System.out.println("Tabella Carte già popolata.");
            }

            if (isTableEmpty("SELECT COUNT(*) FROM card_sets")) {
                System.out.println("Tabella cardsets vuota, caricamento in corso...");
                caricaCardSet(conn);
                System.out.println("Caricamento del cardsets completato.");
            } else {
                System.out.println("Tabella Cardsets già popolata.");
            }

            if (isTableEmpty("SELECT COUNT(*) FROM card_cardsets")) {
                System.out.println("Tabella card_cardsets vuota, caricamento in corso...");
                caricaCard_cardsets(conn);
                System.out.println("Caricamento del card_cardsets completato.");
            } else {
                System.out.println("Tabella card_cardsets già popolata.");
            }

            // InserisciPriceForCard_Cardsets();
            // processDecksAndCards(conn);
            // importDecksFromFile("/Users/gabrielemarzano/Desktop/deckcopia.txt");
            // updateAllHAS_EFFECT(conn);

            // saveArchetypePairsToDb(conn,
            // "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/archetypeCompatibilyDaDeck.txt");

            // saveAllDeckRaceCompatibility(conn);
        } catch (SQLException e) {
            System.out.println("Errore durante la connessione al database: " + e.getMessage());
        }

    }

    // prende gli archetipi compatibili da un file e li inserisce nel db
    public static void saveArchetypePairsToDb(Connection conn, String filePath) {
        String insertSQL = """
                INSERT IGNORE INTO archetype_compatibili (archetype1, archetype2)
                VALUES (?, ?)
                """;
        Set<String> uniquePairs = new HashSet<>();

        // Legge il file e popola il Set con le coppie uniche
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                uniquePairs.add(line.trim()); // Elimina eventuali duplicati
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
            e.printStackTrace();
        }

        // Inserisce le coppie nel database
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (String pair : uniquePairs) {
                String[] parts = pair.split(" < ");
                if (parts.length == 2) {
                    String archetype1 = parts[0].trim();
                    String archetype2 = parts[1].trim();

                    pstmt.setString(1, archetype1);
                    pstmt.setString(2, archetype2);
                    pstmt.addBatch();
                }
            }

            pstmt.executeBatch(); // Esegui l'inserimento in batch
            // System.out.println("Coppie di archetipi salvate nel database.");
        } catch (SQLException e) {
            System.err.println("Errore durante il salvataggio nel database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // rimuove righe duplicate da file
    public static void removeDuplicateLines(String filePath) {
        // Usa un Set per mantenere solo righe uniche
        Set<String> uniqueLines = new LinkedHashSet<>();

        // Legge il file e popola il Set
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                uniqueLines.add(line.trim()); // Aggiungi la riga eliminando spazi inutili
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
            e.printStackTrace();
        }

        // Riscrive il file senza duplicati
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String uniqueLine : uniqueLines) {
                writer.write(uniqueLine);
                writer.newLine();
            }
            System.out.println("File aggiornato senza duplicati: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // prende gli archetipi presenti in ogni deck da db e scrive in un file che quei
    // archetipi sono compatibili
    public static void writeArchetypePairsToFile(Connection conn, String filePath) {
        String query = """
                    SELECT DISTINCT archetype
                    FROM carte
                    JOIN deck_carte ON carte.id = deck_carte.card_id
                    WHERE deck_carte.deck_id = ?
                """;

        String deckQuery = "SELECT DISTINCT deck_id FROM deck_carte";

        try (PreparedStatement deckStmt = conn.prepareStatement(deckQuery);
                PreparedStatement archetypeStmt = conn.prepareStatement(query);
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // Recupera tutti i deck_id
            try (ResultSet deckRs = deckStmt.executeQuery()) {
                while (deckRs.next()) {
                    int deckId = deckRs.getInt("deck_id");

                    // Recupera tutti gli archetipi per il deck corrente
                    List<String> archetypes = new ArrayList<>();
                    archetypeStmt.setInt(1, deckId);
                    try (ResultSet archetypeRs = archetypeStmt.executeQuery()) {
                        while (archetypeRs.next()) {
                            String archetype = archetypeRs.getString("archetype");
                            if (archetype != null && !archetype.isBlank()) {
                                archetypes.add(archetype);
                            }
                        }
                    }

                    // Ordina gli archetipi alfabeticamente
                    Collections.sort(archetypes);

                    // Scrivi tutte le coppie di archetipi nel file
                    for (int i = 0; i < archetypes.size(); i++) {
                        for (int j = i + 1; j < archetypes.size(); j++) {
                            writer.write(archetypes.get(i) + " < " + archetypes.get(j));
                            writer.newLine();
                        }
                    }
                }
            }

            System.out.println("File scritto con successo: " + filePath);

        } catch (SQLException e) {
            System.err.println("Errore durante l'interazione con il database: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // verifdica in quanti deck sono presenti le due carte contemporaneamente

    public static int getCommonDeckCount(Connection conn, int card1Id, int card2Id) {
        int commonDecks = 0;

        String query = """
                SELECT COUNT(DISTINCT d1.deck_id) AS common_decks
                FROM deck_carte AS d1
                JOIN deck_carte AS d2
                  ON d1.deck_id = d2.deck_id
                WHERE d1.card_id = ? AND d2.card_id = ?;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta i parametri
            pstmt.setInt(1, card1Id);
            pstmt.setInt(2, card2Id);

            // Esegue la query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    commonDecks = rs.getInt("common_decks");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il calcolo dei deck comuni: " + e.getMessage());
        }

        return commonDecks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public static boolean readEmail(Connection conn, String email) {

        boolean b = false;

        String query = "SELECT EMAIL FROM Utente where email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Imposta il parametro nella query
            pstmt.setString(1, email);

            // Esegue la query
            ResultSet rs = pstmt.executeQuery();

            // Controlla se ci sono risultati
            if (rs.next()) {

                b = true;
            } else {

            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }
        return b;
    }

    public static String readPassword(Connection conn, String email) {

        String query = "SELECT password FROM utente WHERE email = ?";
        String password = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta il parametro nella query
            pstmt.setString(1, email);

            // Esegue la query
            ResultSet rs = pstmt.executeQuery();

            // Controlla se ci sono risultati
            if (rs.next()) {
                password = rs.getString("password");
            } else {
                System.out.println("Utente non trovato per l'email fornita.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }
        return password; // Restituisce la password
    }

    public static int getIdByEmail(Connection conn, String email) {
        String query = "SELECT id FROM Utente WHERE email = ?";
        Integer id = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta l'email come parametro della query
            pstmt.setString(1, email);

            // Esegue la query
            ResultSet rs = pstmt.executeQuery();

            // Se trova un risultato, legge l'ID
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return id; // Restituisce l'ID (null se non trovato)
    }

    public static String getNomeByEmail(Connection conn, String email) {
        String query = "SELECT nome FROM Utente WHERE email = ?";
        String nome = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nome = rs.getString("nome");
            } else {
                System.out.println("Nome non trovato per l'email fornita.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return nome;
    }

    public static String getCognomeByEmail(Connection conn, String email) {
        String query = "SELECT cognome FROM Utente WHERE email = ?";
        String cognome = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                cognome = rs.getString("cognome");
            } else {
                System.out.println("Cognome non trovato per l'email fornita.");
            }
        } catch (SQLException e) {
            System.out.println("cazzo di merda");
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return cognome;
    }

    public static String getNicknameByEmail(Connection conn, String email) {
        String query = "SELECT nickname FROM Utente WHERE email = ?";
        String nickname = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nickname = rs.getString("nickname");
            } else {
                System.out.println("Nickname non trovato per l'email fornita.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return nickname;
    }

    public static int getCollezioneIdByUtenteId(Connection conn, int utenteId) {

        String query = "SELECT id_collezione FROM Collezione WHERE id_utente = ?";
        int collezioneId = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta il parametro nella query
            pstmt.setInt(1, utenteId);

            // Esegue la query
            ResultSet rs = pstmt.executeQuery();

            // Controlla se ci sono risultati
            if (rs.next()) {
                collezioneId = rs.getInt("id_collezione");
            } else {
                System.out.println("Nessuna collezione trovata per l'utente con ID: " + utenteId);
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return collezioneId;
    }

    /**
     * @param conn
     */
    public void caricaCarte(Connection conn) {

        List<Carta> listaCarte;

        listaCarte = ApiYGOPRODeck.getCardInfo();

        String insertSQL = """
                    INSERT INTO carte (id, desk, name, type, race, atk, def, level, archetype,
                                       humanReadableCardType, frameType, ygoprodeck_url, attribute,
                                       beta_name, views, viewsweek, upvotes, downvotes, beta_id,
                                       tcg_date, ocg_date, konami_id, has_effect, md_rarity,
                                       cardmarket_price, ebay_price, amazon_price, coolstuffinc_price,
                                       tcgplayer_price, image_url, image_url_small, image_url_cropped,
                                       ban_tcg, ban_ocg, ban_goat)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            int count = 0;

            for (Carta carta : listaCarte) {
                try {
                    preparedStatement.setInt(1, carta.getId());
                    preparedStatement.setString(2, carta.getDesc());
                    preparedStatement.setString(3, carta.getName());
                    preparedStatement.setString(4, carta.getType());
                    preparedStatement.setString(5, carta.getRace());
                    preparedStatement.setString(6, carta.getAtk());
                    preparedStatement.setString(7, carta.getDef());
                    preparedStatement.setString(8, carta.getLevel());
                    preparedStatement.setString(9, carta.getArchetype());
                    preparedStatement.setString(10, carta.getHumanReadableCardType());
                    preparedStatement.setString(11, carta.getFrameType());
                    preparedStatement.setString(12, carta.getYgoprodeck_url());
                    preparedStatement.setString(13, carta.getAttribute());
                    preparedStatement.setString(14, carta.getMisc_info().get(0).getBeta_name());
                    preparedStatement.setInt(15, Integer.parseInt(carta.getMisc_info().get(0).getViews()));
                    preparedStatement.setInt(16, Integer.parseInt(carta.getMisc_info().get(0).getViewsweek()));
                    preparedStatement.setInt(17, Integer.parseInt(carta.getMisc_info().get(0).getUpvotes()));
                    preparedStatement.setInt(18, Integer.parseInt(carta.getMisc_info().get(0).getDownvotes()));
                    preparedStatement.setString(19, carta.getMisc_info().get(0).getBeta_id());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate;
                    if (carta.getMisc_info().get(0).getTcg_date() != null) {
                        localDate = LocalDate.parse(carta.getMisc_info().get(0).getTcg_date(), formatter);

                        // Conversione in java.sql.Date
                        Date sqlDateTcg = Date.valueOf(localDate);
                        preparedStatement.setDate(20, sqlDateTcg);

                    } else {
                        preparedStatement.setDate(20, null);

                    }

                    if (carta.getMisc_info().get(0).getOcg_date() != null) {

                        localDate = LocalDate.parse(carta.getMisc_info().get(0).getOcg_date(), formatter);

                        // Conversione in java.sql.Date
                        Date sqlDateOcg = Date.valueOf(localDate);

                        preparedStatement.setDate(21, sqlDateOcg);
                    } else {
                        preparedStatement.setDate(21, null);

                    }

                    preparedStatement.setString(22, carta.getMisc_info().get(0).getKonami_id());
                    preparedStatement.setBoolean(23, Boolean.parseBoolean(carta.getMisc_info().get(0).getHas_effect()));
                    preparedStatement.setString(24, carta.getMisc_info().get(0).getMd_rarity());

                    BigDecimal cardMArkePrices = new BigDecimal(
                            Double.parseDouble(carta.getCard_prices().get(0).getCardmarket_price()));

                    preparedStatement.setBigDecimal(25, cardMArkePrices);

                    BigDecimal ebayPrices = new BigDecimal(
                            Double.parseDouble(carta.getCard_prices().get(0).getEbay_price()));

                    preparedStatement.setBigDecimal(26, ebayPrices);

                    BigDecimal amazonPrices = new BigDecimal(
                            Double.parseDouble(carta.getCard_prices().get(0).getAmazon_price()));

                    preparedStatement.setBigDecimal(27, amazonPrices);

                    BigDecimal coolstuffincPrice = new BigDecimal(
                            Double.parseDouble(carta.getCard_prices().get(0).getAmazon_price()));

                    preparedStatement.setBigDecimal(28, coolstuffincPrice);

                    BigDecimal tcgplayerPrice = new BigDecimal(
                            Double.parseDouble(carta.getCard_prices().get(0).getTcgplayer_price()));

                    preparedStatement.setBigDecimal(29, tcgplayerPrice);

                    preparedStatement.setString(30, carta.getCard_images().get(0).getImage_url());
                    preparedStatement.setString(31, carta.getCard_images().get(0).getImage_url_small());
                    preparedStatement.setString(32, carta.getCard_images().get(0).getImage_url_cropped());

                    if (carta.getBanlist_info() != null) {

                        preparedStatement.setString(33, carta.getBanlist_info().getBan_tcg());
                        preparedStatement.setString(34, carta.getBanlist_info().getBan_ocg());
                        preparedStatement.setString(35, carta.getBanlist_info().getBan_goat());

                    } else {

                        preparedStatement.setString(33, null);
                        preparedStatement.setString(34, null);
                        preparedStatement.setString(35, null);
                    }

                    preparedStatement.addBatch();
                    count++;
                } catch (SQLException e) {
                    System.err.println("Errore durante la preparazione dei dati per la carta: " + carta.getName());
                    e.printStackTrace();
                }
            }

            int[] results = preparedStatement.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0)
                    successCount++;
            }

            System.out
                    .println("Inserimento completato: " + successCount + " carte aggiunte su " + count + " richieste.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento delle carte nel database.");
            e.printStackTrace();
        }
    }

    public static void insertSingleCard(Connection conn, Carta carta) {
        String insertSQL = """
                    INSERT ignore INTO carte (id, desk, name, type, race, atk, def, level, archetype,
                                       humanReadableCardType, frameType, ygoprodeck_url, attribute,
                                       beta_name, views, viewsweek, upvotes, downvotes, beta_id,
                                       tcg_date, ocg_date, konami_id, has_effect, md_rarity,
                                       cardmarket_price, ebay_price, amazon_price, coolstuffinc_price,
                                       tcgplayer_price, image_url, image_url_small, image_url_cropped,
                                       ban_tcg, ban_ocg, ban_goat)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            preparedStatement.setInt(1, carta.getId());
            preparedStatement.setString(2, carta.getDesc());
            preparedStatement.setString(3, carta.getName());
            preparedStatement.setString(4, carta.getType());
            preparedStatement.setString(5, carta.getRace());
            preparedStatement.setString(6, carta.getAtk());
            preparedStatement.setString(7, carta.getDef());
            preparedStatement.setString(8, carta.getLevel());
            preparedStatement.setString(9, carta.getArchetype());
            preparedStatement.setString(10, carta.getHumanReadableCardType());
            preparedStatement.setString(11, carta.getFrameType());
            preparedStatement.setString(12, carta.getYgoprodeck_url());
            preparedStatement.setString(13, carta.getAttribute());

            if (carta.getMisc_info() != null && !carta.getMisc_info().isEmpty()) {
                preparedStatement.setString(14, carta.getMisc_info().get(0).getBeta_name());
                preparedStatement.setInt(15, Integer.parseInt(carta.getMisc_info().get(0).getViews()));
                preparedStatement.setInt(16, Integer.parseInt(carta.getMisc_info().get(0).getViewsweek()));
                preparedStatement.setInt(17, Integer.parseInt(carta.getMisc_info().get(0).getUpvotes()));
                preparedStatement.setInt(18, Integer.parseInt(carta.getMisc_info().get(0).getDownvotes()));
                preparedStatement.setString(19, carta.getMisc_info().get(0).getBeta_id());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (carta.getMisc_info().get(0).getTcg_date() != null) {
                    preparedStatement.setDate(20,
                            Date.valueOf(LocalDate.parse(carta.getMisc_info().get(0).getTcg_date(), formatter)));
                } else {
                    preparedStatement.setDate(20, null);
                }

                if (carta.getMisc_info().get(0).getOcg_date() != null) {
                    preparedStatement.setDate(21,
                            Date.valueOf(LocalDate.parse(carta.getMisc_info().get(0).getOcg_date(), formatter)));
                } else {
                    preparedStatement.setDate(21, null);
                }

                preparedStatement.setString(22, carta.getMisc_info().get(0).getKonami_id());
                preparedStatement.setBoolean(23, Boolean.parseBoolean(carta.getMisc_info().get(0).getHas_effect()));
                preparedStatement.setString(24, carta.getMisc_info().get(0).getMd_rarity());
            } else {
                preparedStatement.setString(14, null);
                preparedStatement.setInt(15, 0);
                preparedStatement.setInt(16, 0);
                preparedStatement.setInt(17, 0);
                preparedStatement.setInt(18, 0);
                preparedStatement.setString(19, null);
                preparedStatement.setDate(20, null);
                preparedStatement.setDate(21, null);
                preparedStatement.setString(22, null);
                preparedStatement.setBoolean(23, false);
                preparedStatement.setString(24, null);
            }

            if (carta.getCard_prices() != null && !carta.getCard_prices().isEmpty()) {
                preparedStatement.setBigDecimal(25,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getCardmarket_price())));
                preparedStatement.setBigDecimal(26,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getEbay_price())));
                preparedStatement.setBigDecimal(27,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getAmazon_price())));
                preparedStatement.setBigDecimal(28,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getCoolstuffinc_price())));
                preparedStatement.setBigDecimal(29,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getTcgplayer_price())));
            } else {
                preparedStatement.setBigDecimal(25, null);
                preparedStatement.setBigDecimal(26, null);
                preparedStatement.setBigDecimal(27, null);
                preparedStatement.setBigDecimal(28, null);
                preparedStatement.setBigDecimal(29, null);
            }

            if (carta.getCard_images() != null && !carta.getCard_images().isEmpty()) {
                preparedStatement.setString(30, carta.getCard_images().get(0).getImage_url());
                preparedStatement.setString(31, carta.getCard_images().get(0).getImage_url_small());
                preparedStatement.setString(32, carta.getCard_images().get(0).getImage_url_cropped());
            } else {
                preparedStatement.setString(30, null);
                preparedStatement.setString(31, null);
                preparedStatement.setString(32, null);
            }

            if (carta.getBanlist_info() != null) {
                preparedStatement.setString(33, carta.getBanlist_info().getBan_tcg());
                preparedStatement.setString(34, carta.getBanlist_info().getBan_ocg());
                preparedStatement.setString(35, carta.getBanlist_info().getBan_goat());
            } else {
                preparedStatement.setString(33, null);
                preparedStatement.setString(34, null);
                preparedStatement.setString(35, null);
            }

            preparedStatement.executeUpdate();
            System.out.println("Carta inserita con successo: " + carta.getName());
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento della carta: " + carta.getName());
            e.printStackTrace();
        }
    }

    public void insertCardCompatibily(Connection conn, int id1, int id2, double compatibily) {
        String sql = "INSERT INTO card_compatibily (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Assegna i valori ai placeholder (?)
            pstmt.setInt(1, id1); // id della prima carta
            pstmt.setInt(2, id2); // id della seconda carta
            pstmt.setDouble(3, compatibily); // valore di compatibilità

            // Esegui l'insert
            pstmt.executeUpdate();
            // System.out.println("Compatibilità inserita con successo per le carte " + id1
            // + " e " + id2);
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento della compatibilità: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // verifica se due carte sono gia conforntatr altrimenti inserisce il record
    // nela tab con compatibili 0.0
    public void ensureCardCompatibilyExists(Connection conn, int id1, int id2) throws SQLException {
        // Normalizzare l'ordine degli ID per garantire unicità (id1 < id2)
        int minId = Math.min(id1, id2);
        int maxId = Math.max(id1, id2);

        // Query per verificare se la coppia esiste
        String checkQuery = """
                    SELECT 1
                    FROM card_compatibily
                    WHERE (id_card1 = ? AND id_card2 = ?) OR (id_card1 = ? AND id_card2 = ?)
                """;

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, minId);
            checkStmt.setInt(2, maxId);
            checkStmt.setInt(3, maxId);
            checkStmt.setInt(4, minId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    // Se la coppia non esiste, inserire un nuovo record
                    String insertQuery = """
                                INSERT INTO card_compatibily (id_card1, id_card2, compatibily)
                                VALUES (?, ?, ?)
                            """;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, minId);
                        insertStmt.setInt(2, maxId);
                        insertStmt.setDouble(3, 0.0); // Compatibily iniziale
                        insertStmt.executeUpdate();
                        System.out.println("Nuovo record inserito: (" + minId + ", " + maxId + ")");
                    }
                }
            }
        }
    }

    public static void updateHAS_EFFECT(Connection conn, List<Carta> listaCarte) {
        // Query per aggiornare solo il campo has_effect
        String updateSQL = "UPDATE carte SET has_effect = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(updateSQL)) {
            int count = 0;
            int successCount = 0;

            for (Carta carta : listaCarte) {
                try {
                    // Verifica se la carta ha effetto
                    boolean hasEffect = carta.getMisc_info() != null
                            && carta.getMisc_info().size() > 0
                            && ("true".equalsIgnoreCase(carta.getMisc_info().get(0).getHas_effect())
                                    || "1".equals(carta.getMisc_info().get(0).getHas_effect()));

                    // Imposta i parametri della query
                    preparedStatement.setBoolean(1, hasEffect);
                    preparedStatement.setInt(2, carta.getId());

                    // Aggiunge la query al batch
                    preparedStatement.addBatch();
                    count++;
                } catch (SQLException e) {
                    System.err.println("Errore durante la preparazione dei dati per la carta con ID: " + carta.getId());
                    e.printStackTrace();
                }
            }

            // Esegue il batch
            int[] results = preparedStatement.executeBatch();

            // Conta gli aggiornamenti effettuati con successo
            for (int result : results) {
                if (result >= 0) {
                    successCount++;
                }
            }

            System.out.println(
                    "Aggiornamento completato: " + successCount + " carte aggiornate su " + count + " richieste.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento delle carte nel database.");
            e.printStackTrace();
        }
    }

    public void updateAllHAS_EFFECT(Connection conn) {
        // Recupera la lista delle carte dall'API
        List<Carta> listaCarte = ApiYGOPRODeck.getCardInfo();

        // Query per aggiornare solo il campo has_effect
        String updateSQL = "UPDATE carte SET has_effect = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(updateSQL)) {
            int count = 0;
            int successCount = 0;

            for (Carta carta : listaCarte) {
                try {
                    // Verifica se la carta ha effetto
                    boolean hasEffect = carta.getMisc_info() != null
                            && carta.getMisc_info().size() > 0
                            && ("true".equalsIgnoreCase(carta.getMisc_info().get(0).getHas_effect())
                                    || "1".equals(carta.getMisc_info().get(0).getHas_effect()));

                    if (hasEffect) {
                        // Imposta i parametri della query
                        preparedStatement.setBoolean(1, true);
                        preparedStatement.setInt(2, carta.getId());

                        // Aggiunge la query al batch
                        preparedStatement.addBatch();
                        count++;
                    }
                } catch (SQLException e) {
                    System.err.println("Errore durante la preparazione dei dati per la carta con ID: " + carta.getId());
                    e.printStackTrace();
                }
            }

            // Esegue il batch
            int[] results = preparedStatement.executeBatch();

            // Conta gli aggiornamenti effettuati con successo
            for (int result : results) {
                if (result >= 0) {
                    successCount++;
                }
            }

            System.out.println(
                    "Aggiornamento completato: " + successCount + " carte aggiornate su " + count + " richieste.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento delle carte nel database.");
            e.printStackTrace();
        }
    }

    public void inserisciCartaSingola(Connection conn, Carta carta) {
        String insertSQL = """
                    INSERT INTO carte (id, desk, name, type, race, atk, def, level, archetype,
                                       humanReadableCardType, frameType, ygoprodeck_url, attribute,
                                       beta_name, views, viewsweek, upvotes, downvotes, beta_id,
                                       tcg_date, ocg_date, konami_id, has_effect, md_rarity,
                                       cardmarket_price, ebay_price, amazon_price, coolstuffinc_price,
                                       tcgplayer_price, image_url, image_url_small, image_url_cropped,
                                       ban_tcg, ban_ocg, ban_goat)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            try {
                preparedStatement.setInt(1, carta.getId());
                preparedStatement.setString(2, carta.getDesc());
                preparedStatement.setString(3, carta.getName());
                preparedStatement.setString(4, carta.getType());
                preparedStatement.setString(5, carta.getRace());
                preparedStatement.setString(6, carta.getAtk());
                preparedStatement.setString(7, carta.getDef());
                preparedStatement.setString(8, carta.getLevel());
                preparedStatement.setString(9, carta.getArchetype());
                preparedStatement.setString(10, carta.getHumanReadableCardType());
                preparedStatement.setString(11, carta.getFrameType());
                preparedStatement.setString(12, carta.getYgoprodeck_url());
                preparedStatement.setString(13, carta.getAttribute());
                preparedStatement.setString(14, carta.getMisc_info().get(0).getBeta_name());
                preparedStatement.setInt(15, Integer.parseInt(carta.getMisc_info().get(0).getViews()));
                preparedStatement.setInt(16, Integer.parseInt(carta.getMisc_info().get(0).getViewsweek()));
                preparedStatement.setInt(17, Integer.parseInt(carta.getMisc_info().get(0).getUpvotes()));
                preparedStatement.setInt(18, Integer.parseInt(carta.getMisc_info().get(0).getDownvotes()));
                preparedStatement.setString(19, carta.getMisc_info().get(0).getBeta_id());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate;
                if (carta.getMisc_info().get(0).getTcg_date() != null) {
                    localDate = LocalDate.parse(carta.getMisc_info().get(0).getTcg_date(), formatter);
                    preparedStatement.setDate(20, Date.valueOf(localDate));
                } else {
                    preparedStatement.setDate(20, null);
                }

                if (carta.getMisc_info().get(0).getOcg_date() != null) {
                    localDate = LocalDate.parse(carta.getMisc_info().get(0).getOcg_date(), formatter);
                    preparedStatement.setDate(21, Date.valueOf(localDate));
                } else {
                    preparedStatement.setDate(21, null);
                }

                preparedStatement.setString(22, carta.getMisc_info().get(0).getKonami_id());
                preparedStatement.setBoolean(23, Boolean.parseBoolean(carta.getMisc_info().get(0).getHas_effect()));
                preparedStatement.setString(24, carta.getMisc_info().get(0).getMd_rarity());
                preparedStatement.setBigDecimal(25,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getCardmarket_price())));
                preparedStatement.setBigDecimal(26,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getEbay_price())));
                preparedStatement.setBigDecimal(27,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getAmazon_price())));
                preparedStatement.setBigDecimal(28,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getCoolstuffinc_price())));
                preparedStatement.setBigDecimal(29,
                        new BigDecimal(Double.parseDouble(carta.getCard_prices().get(0).getTcgplayer_price())));
                preparedStatement.setString(30, carta.getCard_images().get(0).getImage_url());
                preparedStatement.setString(31, carta.getCard_images().get(0).getImage_url_small());
                preparedStatement.setString(32, carta.getCard_images().get(0).getImage_url_cropped());

                if (carta.getBanlist_info() != null) {
                    preparedStatement.setString(33, carta.getBanlist_info().getBan_tcg());
                    preparedStatement.setString(34, carta.getBanlist_info().getBan_ocg());
                    preparedStatement.setString(35, carta.getBanlist_info().getBan_goat());
                } else {
                    preparedStatement.setString(33, null);
                    preparedStatement.setString(34, null);
                    preparedStatement.setString(35, null);
                }

                // Esegui l'inserimento
                preparedStatement.executeUpdate();
                System.out.println("Carta inserita con successo: " + carta.getName());
            } catch (SQLException e) {
                System.err.println("Errore durante la preparazione dei dati per la carta: " + carta.getName());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento della carta nel database.");
            e.printStackTrace();
        }
    }

    /**
     * @param conn
     */
    public void caricaCardSet(Connection conn) {

        List<Card_Set> listaCarte;

        listaCarte = ApiYGOPRODeck.getCardSet();

        String insertSQL = """
                    INSERT INTO card_sets (set_name, set_code, num_of_cards, tcg_date, set_image)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            int count = 0;

            for (Card_Set cardSet : listaCarte) {
                try {
                    preparedStatement.setString(1, cardSet.getSet_name());
                    preparedStatement.setString(2, cardSet.getSet_code());
                    preparedStatement.setInt(3, Integer.parseInt(cardSet.getNum_of_cards()));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate;
                    if (cardSet.getDateTgc() != null) {
                        localDate = LocalDate.parse(cardSet.getDateTgc(), formatter);

                        // Conversione in java.sql.Date
                        Date sqlDateTcg = Date.valueOf(localDate);
                        preparedStatement.setDate(4, sqlDateTcg);

                    } else {
                        preparedStatement.setDate(4, null);

                    }

                    preparedStatement.setString(5, cardSet.getSet_image());

                    preparedStatement.addBatch();
                    count++;
                } catch (SQLException e) {
                    System.err
                            .println("Errore durante la preparazione dei dati per la carta: " + cardSet.getSet_name());
                    e.printStackTrace();
                }
            }

            int[] results = preparedStatement.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0)
                    successCount++;
            }

            System.out
                    .println("Inserimento completato: " + successCount + " carte aggiunte su " + count + " richieste.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento delle carte nel database.");
            e.printStackTrace();
        }
    }

    public static void inserisciCardSetsPerCarte(Connection conn, List<Carta> carte) {

        // SQL per ottenere card_id dalla tabella cards
        String selectCardIdSQL = "SELECT id FROM Carte WHERE name = ?";

        // SQL per ottenere card_set_id dalla tabella card_sets
        String selectCardSetIdSQL = "SELECT id FROM card_sets1 WHERE set_code = ? AND set_name = ?";

        // SQL per inserire i dati nella tabella card_cardsets
        String insertSQL = """
                    INSERT INTO card_cardsets1 (card_id, card_set_id, set_name, set_code, set_rarity, set_rarity_code, set_price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement selectCardIdStmt = conn.prepareStatement(selectCardIdSQL);
                PreparedStatement selectCardSetIdStmt = conn.prepareStatement(selectCardSetIdSQL);
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            int totalInserted = 0;

            // Itera su ogni carta nella lista
            for (Carta card : carte) {
                if (card.getCard_sets() == null || card.getCard_sets().isEmpty()) {
                    // Salta le carte senza set associati
                    System.out.println("Nessun card set associato per la carta: " + card.getName());
                    continue;
                }

                // Ottieni card_id dalla tabella cards
                int cardId;
                selectCardIdStmt.setString(1, card.getName());
                try (ResultSet cardIdRs = selectCardIdStmt.executeQuery()) {
                    if (cardIdRs.next()) {
                        cardId = cardIdRs.getInt("id");
                    } else {
                        // Salta se la carta non esiste nella tabella cards
                        System.err.println("Carta non trovata nel database: " + card.getName());
                        continue;
                    }
                }

                int count = 0;

                // Itera su ogni set associato alla carta
                for (CardSets cardSet : card.getCard_sets()) {
                    // Estrai solo la parte del codice del set prima del trattino
                    String normalizedSetCode = cardSet.getSet_code().split("-")[0];

                    // Ottieni card_set_id dalla tabella card_sets
                    int cardSetId;
                    selectCardSetIdStmt.setString(1, normalizedSetCode);
                    selectCardSetIdStmt.setString(2, cardSet.getSet_name());
                    try (ResultSet cardSetIdRs = selectCardSetIdStmt.executeQuery()) {
                        if (cardSetIdRs.next()) {
                            cardSetId = cardSetIdRs.getInt("id");
                        } else {
                            // Salta se il set non esiste nella tabella card_sets
                            System.err.println("Set non trovato nel database: " + cardSet.getSet_name() + " ("
                                    + cardSet.getSet_code() + ")");
                            continue;
                        }
                    }

                    try {
                        // Popola i parametri del PreparedStatement per l'inserimento
                        insertStmt.setInt(1, cardId); // ID della carta
                        insertStmt.setInt(2, cardSetId); // ID del set
                        insertStmt.setString(3, cardSet.getSet_name()); // Nome del set
                        insertStmt.setString(4, cardSet.getSet_code()); // Codice del set originale
                        insertStmt.setString(5, cardSet.getSet_rarity()); // Rarità del set
                        insertStmt.setString(6, cardSet.getSet_rarity_code()); // Codice rarità
                        insertStmt.setDouble(7, Double.parseDouble(cardSet.getSet_price())); // Prezzo del set

                        // Esegui l'inserimento
                        insertStmt.executeUpdate();
                        count++;
                    } catch (SQLException | NumberFormatException e) {
                        System.err.println(
                                "Errore durante la preparazione dei dati per il set: " + cardSet.getSet_name());
                        e.printStackTrace();
                    }
                }

                totalInserted += count;
                System.out.println("Inseriti " + count + " set per la carta: " + card.getName());
            }

            System.out.println("Inserimento completato: " + totalInserted + " set aggiunti in totale.");

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento dei card sets per le carte.");
            e.printStackTrace();
        }
    }

    public void caricaCard_cardsets(Connection conn) {

        List<Carta> listaCarte = ApiYGOPRODeck.getCardInfo(); // Ottieni la lista di carte tramite l'API

        // SQL per ottenere card_id dalla tabella cards
        String selectCardIdSQL = "SELECT id FROM Carte WHERE name = ?";

        // SQL per ottenere card_set_id dalla tabella card_sets
        String selectCardSetIdSQL = "SELECT id FROM card_sets WHERE set_code = ? AND set_name = ?";

        // SQL per inserire i dati nella tabella card_cardsets
        String insertSQL = """
                    INSERT INTO card_cardsets (card_id, card_set_id, set_name, set_code, set_rarity, set_rarity_code, set_price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement selectCardIdStmt = conn.prepareStatement(selectCardIdSQL);
                PreparedStatement selectCardSetIdStmt = conn.prepareStatement(selectCardSetIdSQL);
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            int count = 0;

            // Itera su ogni carta nella lista
            for (Carta card : listaCarte) {
                if (card.getCard_sets() == null || card.getCard_sets().isEmpty()) {
                    // Salta le carte senza card_sets associati
                    continue;
                }

                // Ottieni card_id dalla tabella cards
                int cardId;
                selectCardIdStmt.setString(1, card.getName());
                try (ResultSet cardIdRs = selectCardIdStmt.executeQuery()) {
                    if (cardIdRs.next()) {
                        cardId = cardIdRs.getInt("id");

                    } else {
                        // Salta se la carta non esiste nella tabella cards
                        System.err.println("Carta non trovata nel database: " + card.getName());
                        continue;
                    }
                }

                // Itera su ogni set associato alla carta
                for (CardSets cardSet : card.getCard_sets()) {
                    // Estrai solo la parte del codice del set prima del trattino
                    String normalizedSetCode = cardSet.getSet_code().split("-")[0];
                    System.out.println(normalizedSetCode);

                    // Ottieni card_set_id dalla tabella card_sets
                    int cardSetId;
                    selectCardSetIdStmt.setString(1, normalizedSetCode);
                    selectCardSetIdStmt.setString(2, cardSet.getSet_name());
                    try (ResultSet cardSetIdRs = selectCardSetIdStmt.executeQuery()) {
                        if (cardSetIdRs.next()) {
                            cardSetId = cardSetIdRs.getInt("id");
                        } else {
                            // Salta se il set non esiste nella tabella card_sets
                            System.err.println("Set non trovato nel database: " + cardSet.getSet_name() + " ("
                                    + cardSet.getSet_code() + ")");
                            continue;
                        }
                    }

                    try {
                        // Popola i parametri del PreparedStatement per l'inserimento
                        insertStmt.setInt(1, cardId); // ID della carta
                        insertStmt.setInt(2, cardSetId); // ID del set
                        insertStmt.setString(3, cardSet.getSet_name()); // Nome del set
                        insertStmt.setString(4, cardSet.getSet_code()); // Codice del set originale
                        insertStmt.setString(5, cardSet.getSet_rarity()); // Rarità del set
                        insertStmt.setString(6, cardSet.getSet_rarity_code()); // Codice rarità
                        insertStmt.setDouble(7, Double.parseDouble(cardSet.getSet_price())); // Prezzo del set

                        // Aggiungi il record al batch
                        insertStmt.addBatch();
                        count++;
                    } catch (SQLException | NumberFormatException e) {
                        System.err.println("Errore durante la preparazione dei dati per la carta: " + card.getName());
                        e.printStackTrace();
                    }
                }
            }

            // Esegui il batch di inserimenti
            int[] results = insertStmt.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0)
                    successCount++;
            }

            // Stampa il risultato dell'inserimento
            System.out.println("Inserimento completato: " + successCount + " associazioni aggiunte su " + count
                    + " richieste.");
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento delle associazioni nel database.");
            e.printStackTrace();
        }
    }

    public void caricaCardCardsetsSingolaCarta(Connection conn, Carta card) {
        String selectCardIdSQL = "SELECT id FROM Carte WHERE name = ?";
        String selectCardSetIdSQL = "SELECT id FROM card_sets WHERE set_code = ? AND set_name = ?";
        String insertSQL = """
                    INSERT INTO card_cardsets (card_id, card_set_id, set_name, set_code, set_rarity, set_rarity_code, set_price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement selectCardIdStmt = conn.prepareStatement(selectCardIdSQL);
                PreparedStatement selectCardSetIdStmt = conn.prepareStatement(selectCardSetIdSQL);
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            if (card.getCard_sets() == null || card.getCard_sets().isEmpty()) {
                System.err.println("La carta non ha set associati: " + card.getName());
                return;
            }

            int cardId;
            selectCardIdStmt.setString(1, card.getName());
            try (ResultSet cardIdRs = selectCardIdStmt.executeQuery()) {
                if (cardIdRs.next()) {
                    cardId = cardIdRs.getInt("id");
                    System.out.println("Card ID trovato: " + cardId);
                } else {
                    System.err.println("Carta non trovata nel database: " + card.getName());
                    return;
                }
            }

            int count = 0;
            for (CardSets cardSet : card.getCard_sets()) {
                String normalizedSetCode = cardSet.getSet_code().split("-")[0];
                System.out.println("Normalized Set Code: " + normalizedSetCode);

                int cardSetId;
                selectCardSetIdStmt.setString(1, normalizedSetCode);
                selectCardSetIdStmt.setString(2, cardSet.getSet_name());
                try (ResultSet cardSetIdRs = selectCardSetIdStmt.executeQuery()) {
                    if (cardSetIdRs.next()) {
                        cardSetId = cardSetIdRs.getInt("id");
                        System.out.println("Card Set ID trovato: " + cardSetId);
                    } else {
                        System.err.println("Set non trovato nel database: " + cardSet.getSet_name() + " ("
                                + normalizedSetCode + ")");
                        continue;
                    }
                }

                try {
                    System.out.println("Inserimento: card_id=" + cardId + ", card_set_id=" + cardSetId + ", set_name="
                            + cardSet.getSet_name());
                    insertStmt.setInt(1, cardId);
                    insertStmt.setInt(2, cardSetId);
                    insertStmt.setString(3, cardSet.getSet_name());
                    insertStmt.setString(4, cardSet.getSet_code());
                    insertStmt.setString(5, cardSet.getSet_rarity());
                    insertStmt.setString(6, cardSet.getSet_rarity_code());
                    insertStmt.setDouble(7, Double.parseDouble(cardSet.getSet_price()));

                    insertStmt.addBatch();
                    count++;
                } catch (SQLException | NumberFormatException e) {
                    System.err.println("Errore durante l'inserimento: " + e.getMessage());
                }
            }

            int[] results = insertStmt.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0)
                    successCount++;
            }

            System.out.println(
                    "Inserimento completato: " + successCount + " associazioni aggiunte su " + count + " richieste.");
            conn.commit(); // Esegui il commit, se necessario
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento: " + e.getMessage());
        }
    }

    public static void InserisciPriceForCard_Cardsets(Connection conn) throws Exception {

        // Query per ottenere carte uniche
        String selectDistinctCardsSQL = """
                    SELECT DISTINCT Carte.id, Carte.name
                    FROM Carte
                    JOIN card_cardsetsBackup ON Carte.id = card_cardsetsBackup.card_id
                """;

        // Lista per memorizzare le carte uniche
        List<CardData> nameeIdCard = new ArrayList<>();

        // Recupero delle carte uniche
        try (PreparedStatement stmt = conn.prepareStatement(selectDistinctCardsSQL);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CardData card = new CardData();
                card.setId(rs.getInt("id"));
                card.setName(rs.getString("name"));
                nameeIdCard.add(card);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle carte uniche: " + e.getMessage());
            throw e;
        }

        for (CardData c : nameeIdCard) {

            System.out.println(c.getName());
        }

        System.out.println("Trovate " + nameeIdCard.size() + " carte uniche.");

        // CODICE ORIGINALE SUCCESSIVO

        // Ottieni i dati dall'API

        for (int i = nameeIdCard.size() / 2; i < nameeIdCard.size(); i++) {
            CardData data = nameeIdCard.get(i);

            List<CardData> apiData = ApiYugiohPrices.getCardPrice(data.getName());

            if (apiData == null || apiData.isEmpty()) {
                System.out.println("Nessun dato trovato per la carta: " + data.getName());
                continue;
            }

            // Query per aggiornare i dati nella tabella `card_cardsets`
            String updateSQL = """
                        UPDATE card_cardsetsBackup
                        SET high_price = ?, low_price = ?, average_price = ?, shift = ?, shift_3 = ?,
                            shift_7 = ?, shift_21 = ?, shift_30 = ?, shift_90 = ?, shift_180 = ?,
                            shift_365 = ?, updated_at = ?
                        WHERE card_id = ? AND set_name = ? AND set_code = ?
                    """;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                for (CardData apiCard : apiData) {
                    // Verifica che i dati siano disponibili e corretti
                    if (apiCard.getPrice_data() == null || !"success".equals(apiCard.getPrice_data().getStatus())) {
                        System.out.println("Dati di prezzo non disponibili per il set: " + apiCard.getName());
                        continue;
                    }

                    // Prepara l'aggiornamento
                    updateStmt.setBigDecimal(1,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getHigh()));
                    updateStmt.setBigDecimal(2, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getLow()));
                    updateStmt.setBigDecimal(3,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getAverage()));
                    updateStmt.setBigDecimal(4,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift()));
                    updateStmt.setBigDecimal(5,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_3()));
                    updateStmt.setBigDecimal(6,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_7()));
                    updateStmt.setBigDecimal(7,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_21()));
                    updateStmt.setBigDecimal(8,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_30()));
                    updateStmt.setBigDecimal(9,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_90()));
                    updateStmt.setBigDecimal(10,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_180()));
                    updateStmt.setBigDecimal(11,
                            new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_365()));
                    updateStmt.setTimestamp(12,
                            java.sql.Timestamp.valueOf(apiCard.getPrice_data().getData().getPrices().getUpdated_at()));

                    // Imposta i parametri per la chiave primaria
                    updateStmt.setInt(13, data.getId()); // ID della carta
                    updateStmt.setString(14, apiCard.getName()); // Nome del set
                    updateStmt.setString(15, apiCard.getPrint_tag());

                    // Esegui l'aggiornamento
                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Aggiornati i dati per la carta: " + apiCard.getName() + " ("
                                + apiCard.getPrint_tag() + ")");
                    } else {
                        System.out.println("Nessun aggiornamento effettuato per la carta: " + apiCard.getName() + " ("
                                + apiCard.getPrint_tag() + ")");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della tabella card_cardsets: " + e.getMessage());
                throw e;
            }
        }

    }

    public static void InserisciPriceForCard_CardsetsSingola(Carta card, Connection conn) throws Exception {
        // Recupera i dati dall'API per la carta specifica
        List<CardData> apiData = ApiYugiohPrices.getCardPrice(card.getName());

        if (apiData == null || apiData.isEmpty()) {
            System.out.println("Nessun dato trovato per la carta: " + card.getName());
            return;
        } else {
            System.out.println("Dati trovati per la carta: " + card.getName());

        }

        // Query per aggiornare i dati nella tabella `card_cardsets`
        String updateSQL = """
                    UPDATE card_cardsets
                    SET high_price = ?, low_price = ?, average_price = ?, shift = ?, shift_3 = ?,
                        shift_7 = ?, shift_21 = ?, shift_30 = ?, shift_90 = ?, shift_180 = ?,
                        shift_365 = ?, updated_at = ?
                    WHERE card_id = ? AND set_name = ? AND set_code = ?
                """;

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            for (CardData apiCard : apiData) {
                // Verifica che i dati siano disponibili e corretti
                if (apiCard.getPrice_data() == null || !"success".equals(apiCard.getPrice_data().getStatus())) {
                    System.out.println("Dati di prezzo non disponibili per il set: " + apiCard.getName());
                    continue;
                }

                // Prepara l'aggiornamento
                updateStmt.setBigDecimal(1, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getHigh()));
                updateStmt.setBigDecimal(2, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getLow()));
                updateStmt.setBigDecimal(3, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getAverage()));
                updateStmt.setBigDecimal(4, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift()));
                updateStmt.setBigDecimal(5, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_3()));
                updateStmt.setBigDecimal(6, new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_7()));
                updateStmt.setBigDecimal(7,
                        new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_21()));
                updateStmt.setBigDecimal(8,
                        new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_30()));
                updateStmt.setBigDecimal(9,
                        new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_90()));
                updateStmt.setBigDecimal(10,
                        new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_180()));
                updateStmt.setBigDecimal(11,
                        new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_365()));
                updateStmt.setTimestamp(12,
                        java.sql.Timestamp.valueOf(apiCard.getPrice_data().getData().getPrices().getUpdated_at()));

                // Imposta i parametri per la chiave primaria
                updateStmt.setInt(13, card.getId()); // ID della carta
                updateStmt.setString(14, apiCard.getName()); // Nome del set
                updateStmt.setString(15, apiCard.getPrint_tag());

                // Esegui l'aggiornamento
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Aggiornati i dati per la carta: " + apiCard.getName() + " ("
                            + apiCard.getPrint_tag() + ")");
                } else {
                    System.out.println("Nessun aggiornamento effettuato per la carta: " + apiCard.getName() + " ("
                            + apiCard.getPrint_tag() + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della tabella card_cardsets: " + e.getMessage());
            throw e;
        }
    }

    public void importDecksFromFile(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    // Rimuovi caratteri speciali e dividi i dati
                    line = line.replace("[", "").replace("]", "").replace("\"", "");
                    String[] elements = line.split(",");

                    // Il primo elemento è il nome del deck (URL o titolo)
                    String deckName = extractDeckName(elements[0].trim());

                    // Analizza le carte e calcola la quantità per ciascuna
                    Map<Integer, Integer> cardQuantities = new HashMap<>();
                    for (int i = 1; i < elements.length; i++) {
                        int cardId = Integer.parseInt(elements[i].trim());
                        cardQuantities.put(cardId, cardQuantities.getOrDefault(cardId, 0) + 1);
                    }

                    // Inserisci il deck nella tabella `public_deck`
                    insertDeck(deckName);

                    // e ottieni l'ID
                    int deckId = getDeckIdByName(deckName);

                    // Inserisci le carte nella tabella `Deck_carte`
                    insertCardsIntoDeck(deckId, cardQuantities);

                    System.out.println("Deck inserito con successo: " + deckName);
                }
            }
            System.out.println("Importazione completata con successo.");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore durante l'interazione con il database: " + e.getMessage());
        }
    }

    // inserimento dei deck
    private void insertDeck(String deckName) throws SQLException {
        String insertDeckSQL = "INSERT INTO public_deck (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertDeckSQL)) {
            stmt.setString(1, deckName);
            int rowsAffected = stmt.executeUpdate(); // Esegui l'INSERT con executeUpdate()
            if (rowsAffected > 0) {
            } else {
                System.out.println("Nessun deck inserito per il nome: " + deckName);
            }
        }
    }

    // inserimento delle carte nel deck
    private void insertCardsIntoDeck(int deckId, Map<Integer, Integer> cardQuantities) throws Exception {
        String insertCardSQL = "INSERT INTO Deck_carte (deck_id, card_id, quantity) VALUES (?, ?, ?)";
        String checkCardExistenceSQL = "SELECT COUNT(*) FROM carte WHERE id = ?";

        // Disabilita l'autocommit
        conn.setAutoCommit(false);

        try (
                PreparedStatement stmt = conn.prepareStatement(insertCardSQL);
                PreparedStatement checkStmt = conn.prepareStatement(checkCardExistenceSQL)) {
            for (Map.Entry<Integer, Integer> entry : cardQuantities.entrySet()) {
                int cardId = entry.getKey();
                int quantity = entry.getValue();

                // Controlla se il card_id esiste nella tabella `carte`
                checkStmt.setInt(1, cardId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // La carta non esiste, tenta di recuperarla dall'API
                        System.err.println("ID carta non trovato nel database: " + cardId);

                        Carta carta = ApiYGOPRODeck.getCardInfoById(cardId);
                        if (carta != null) {
                            System.out.println("Carta trovata tramite API, inserimento in corso: " + carta.getName());

                            // Inserisci la carta nel database
                            inserisciCartaSingola(conn, carta);

                            // Carica i set associati alla carta
                            caricaCardCardsetsSingolaCarta(conn, carta);

                            InserisciPriceForCard_CardsetsSingola(carta, conn);
                        } else {
                            System.err.println("Impossibile trovare la carta nell'API con ID: " + cardId);
                            continue; // Salta questa carta se non viene trovata nemmeno nell'API
                        }
                    }
                }

                // Inserisci la carta nel deck
                stmt.setInt(1, deckId);
                stmt.setInt(2, cardId);
                stmt.setInt(3, quantity);

                stmt.addBatch();
            }

            // Esegui il batch
            stmt.executeBatch();

            // Effettua il commit manuale
            conn.commit();
            System.out.println("Commit completato con successo.");
        } catch (SQLException e) {
            // Rollback in caso di errore
            System.err.println("Errore durante l'inserimento, rollback in corso: " + e.getMessage());
            conn.rollback();
            throw e;
        } finally {
            // Ripristina l'autocommit
            conn.setAutoCommit(true);
        }
    }

    // per trovare i deck nei cardset
    public String extractDeckName(String input) {
        // Verifica se la stringa contiene "deck/"
        if (input.contains("deck/")) {
            // Trova la posizione iniziale di "deck/"
            int startIndex = input.indexOf("deck/") + "deck/".length();
            // Trova la posizione finale del nome del deck (se c'è un '#' lo escludiamo)
            int endIndex = input.indexOf('#', startIndex);
            if (endIndex == -1) {
                // Se non c'è il simbolo '#', usa la fine della stringa
                endIndex = input.length();
            }
            // Estrai la sottostringa tra startIndex ed endIndex
            return input.substring(startIndex, endIndex).trim();
        } else {
            throw new IllegalArgumentException("La stringa fornita non contiene un nome di deck valido.");
        }
    }

    public boolean isTableEmpty(String query) {
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0; // Ritorna true se la tabella è vuota
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il controllo della tabella: " + e.getMessage());
        }
        return true; // Assume vuota in caso di errore
    }

    private int getDeckIdByName(String deckName) throws SQLException {
        String selectDeckSQL = "SELECT id FROM public_deck WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectDeckSQL)) {
            stmt.setString(1, deckName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); // Restituisce l'ID del deck
                } else {
                    throw new SQLException("Deck non trovato con il nome: " + deckName);
                }
            }
        }
    }

    public static int getCommonCardSets(Connection conn, int cardId1, int cardId2) {
        String query = """
                SELECT COUNT(DISTINCT c1.card_set_id) AS common_cardsets
                FROM card_cardsets c1
                JOIN card_cardsets c2
                  ON c1.card_set_id = c2.card_set_id
                WHERE c1.card_id = ? AND c2.card_id = ? AND c1.set_name NOT LIKE '%deck%'
                """;

        int commonCardSets = 0;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, cardId1);
            stmt.setInt(2, cardId2);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    commonCardSets = rs.getInt("common_cardsets");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'esecuzione della query: " + e.getMessage());
        }

        return commonCardSets;
    }

    // inserisce nuovi deck reperendoli dai dati che dispondo cioè dai cardsets
    // starter/structure deck
    public void processDecksAndCards(Connection conn) {
        String findCardSetIdsSQL = "SELECT id, set_name FROM card_sets WHERE set_name LIKE ?";
        String findCardIdsSQL = "SELECT card_id FROM card_cardsets WHERE card_set_id = ?";
        String insertDeckSQL = "INSERT INTO public_deck (name) VALUES (?)";
        String getDeckIdSQL = "SELECT id FROM public_deck WHERE name = ?";
        String insertDeckCarteSQL = "INSERT INTO deck_carte (deck_id, card_id, quantity) VALUES (?, ?, ?)";

        try (
                PreparedStatement findCardSetIdsStmt = conn.prepareStatement(findCardSetIdsSQL);
                PreparedStatement findCardIdsStmt = conn.prepareStatement(findCardIdsSQL);
                PreparedStatement insertDeckStmt = conn.prepareStatement(insertDeckSQL);
                PreparedStatement getDeckIdStmt = conn.prepareStatement(getDeckIdSQL);
                PreparedStatement insertDeckCarteStmt = conn.prepareStatement(insertDeckCarteSQL)) {

            // Cerca gli ID dei `card_sets` con "deck" nel set_name
            findCardSetIdsStmt.setString(1, "%deck%");
            try (ResultSet cardSetIdsRs = findCardSetIdsStmt.executeQuery()) {
                while (cardSetIdsRs.next()) {
                    int cardSetId = cardSetIdsRs.getInt("id");
                    String deckName = cardSetIdsRs.getString("set_name");

                    // Inserisci il deck nella tabella `deck`
                    insertDeckStmt.setString(1, deckName);
                    insertDeckStmt.executeUpdate();
                    System.out.println("Deck inserito con successo: " + deckName);

                    // Recupera l'ID del deck appena inserito
                    int deckId;
                    getDeckIdStmt.setString(1, deckName);
                    try (ResultSet deckIdRs = getDeckIdStmt.executeQuery()) {
                        if (deckIdRs.next()) {
                            deckId = deckIdRs.getInt("id");
                        } else {
                            System.err.println("Impossibile recuperare l'ID del deck: " + deckName);
                            continue;
                        }
                    }

                    // Mappa per tracciare la quantità di ogni card_id
                    Map<Integer, Integer> cardQuantities = new HashMap<>();

                    // Trova i `card_id` associati al `card_set_id`
                    findCardIdsStmt.setInt(1, cardSetId);
                    try (ResultSet cardIdsRs = findCardIdsStmt.executeQuery()) {
                        while (cardIdsRs.next()) {
                            int cardId = cardIdsRs.getInt("card_id");
                            // Incrementa la quantità per il card_id
                            cardQuantities.put(cardId, cardQuantities.getOrDefault(cardId, 0) + 1);
                        }
                    }

                    // Inserisci nella tabella `deck_carte` i risultati aggregati
                    for (Map.Entry<Integer, Integer> entry : cardQuantities.entrySet()) {
                        int cardId = entry.getKey();
                        int quantity = entry.getValue();

                        // Verifica se il record esiste già nella tabella `deck_carte_copy`
                        if (isRecordPresent(conn, deckId, cardId)) {
                            System.out.println("Record già presente per deck_id=" + deckId + ", card_id=" + cardId);
                            continue;
                        }

                        insertDeckCarteStmt.setInt(1, deckId);
                        insertDeckCarteStmt.setInt(2, cardId);
                        insertDeckCarteStmt.setInt(3, quantity);

                        insertDeckCarteStmt.addBatch();
                    }

                    // Esegui il batch di inserimenti per la tabella `deck_carte`
                    int[] batchResults = insertDeckCarteStmt.executeBatch();
                    int successCount = 0;
                    for (int result : batchResults) {
                        if (result > 0)
                            successCount++;
                    }
                    System.out
                            .println("Carte inserite per il deck: " + deckName + ". Record inseriti: " + successCount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'elaborazione dei deck e delle carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Metodo per verificare se un record è già presente nella tabella
     * `deck_carte_copy`.
     */
    private boolean isRecordPresent(Connection conn, int deckId, int cardId) throws SQLException {
        String checkRecordSQL = "SELECT COUNT(*) FROM deck_carte_copy WHERE deck_id = ? AND card_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkRecordSQL)) {
            checkStmt.setInt(1, deckId);
            checkStmt.setInt(2, cardId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connessione al database chiusa.");
            } catch (SQLException e) {
                System.out.println("Errore durante la chiusura della connessione: " + e.getMessage());
            }
        }
    }

    // inserisce nel db nella cartella race_contabiliti i race che si trovano in un
    // deck
    public static void saveAllDeckRaceCompatibility(Connection conn) {
        String fetchDeckIdsQuery = "SELECT DISTINCT deck_id FROM deck_carte";
        String fetchRacesQuery = """
                    SELECT DISTINCT race
                    FROM carte
                    JOIN deck_carte ON carte.id = deck_carte.card_id
                    WHERE deck_carte.deck_id = ?;
                """;
        String insertQuery = """
                    INSERT INTO race_compatibili (race1, race2)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE race1 = VALUES(race1), race2 = VALUES(race2);
                """;

        try (PreparedStatement fetchDeckIdsStmt = conn.prepareStatement(fetchDeckIdsQuery);
                PreparedStatement fetchRacesStmt = conn.prepareStatement(fetchRacesQuery);
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Recupera tutti gli ID dei deck
            List<Integer> deckIds = new ArrayList<>();
            try (ResultSet rs = fetchDeckIdsStmt.executeQuery()) {
                while (rs.next()) {
                    deckIds.add(rs.getInt("deck_id"));
                }
            }

            System.out.println("Trovati " + deckIds.size() + " deck.");

            // Per ogni deck, calcola le compatibilità delle razze
            for (int deckId : deckIds) {
                fetchRacesStmt.setInt(1, deckId);

                List<String> races = new ArrayList<>();
                try (ResultSet rs = fetchRacesStmt.executeQuery()) {
                    while (rs.next()) {
                        String race = rs.getString("race");
                        if (race != null && !race.isBlank()) {
                            races.add(race.toLowerCase()); // Converti a lowercase per uniformità
                        }
                    }
                }

                // Inserisci tutte le combinazioni uniche di razze
                for (int i = 0; i < races.size(); i++) {
                    for (int j = i + 1; j < races.size(); j++) {
                        String race1 = races.get(i);
                        String race2 = races.get(j);

                        // Inserisci sempre con ordine alfabetico per evitare duplicati
                        if (race1.compareTo(race2) > 0) {
                            String temp = race1;
                            race1 = race2;
                            race2 = temp;
                        }

                        insertStmt.setString(1, race1);
                        insertStmt.setString(2, race2);
                        insertStmt.addBatch();
                    }
                }

                // Esegui il batch di inserimenti per il deck corrente
                int[] results = insertStmt.executeBatch();
                System.out.println("Inserite " + results.length + " combinazioni di razze per il deck " + deckId);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il salvataggio delle compatibilità delle razze: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Carta> getAllCarts(Connection conn) {
        // String query = "SELECT * FROM carte LIMIT 500;";
        String query = "SELECT * FROM carte;";
        List<Carta> cards = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Costruisci un oggetto Carta dai dati del ResultSet
                Carta carta = new Carta(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("race"),
                        rs.getString("atk"),
                        rs.getString("def"),
                        rs.getString("level"),
                        rs.getString("archetype"),
                        rs.getString("humanReadableCardType"),
                        rs.getString("frameType"),
                        rs.getString("desk"),
                        rs.getString("ygoprodeck_url"),
                        null, // card_prices: da gestire separatamente
                        null, // card_images: da gestire separatamente
                        null, // card_sets: da gestire separatamente
                        null, // banlist_info: da gestire separatamente
                        null, // misc_info: da gestire separatamente
                        null, // typeline: da gestire separatamente
                        rs.getString("attribute"),
                        rs.getBoolean("has_effect"));

                cards.add(carta);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle carte: " + e.getMessage());
            e.printStackTrace();
        }

        return cards;
    }

    public static Set<Integer> getAllCardIds(Connection conn) {
        String query = "SELECT id FROM carte;";
        Set<Integer> cardIds = new HashSet<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cardIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero degli ID delle carte: " + e.getMessage());
            e.printStackTrace();
        }

        return cardIds;
    }

    public static Set<Map.Entry<String, String>> getAllCardSetIds(Connection conn) {
        String query = "SELECT set_code, set_name FROM card_sets;";
        Set<Map.Entry<String, String>> cardSetIds = new HashSet<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String setCode = rs.getString("set_code");
                String setName = rs.getString("set_name");
                cardSetIds.add(Map.entry(setCode, setName));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero degli ID dei card set: " + e.getMessage());
            e.printStackTrace();
        }

        return cardSetIds;
    }

    // dividere i dati tra le compatibilità tra le carte per test, validation, e
    // addestramento
    public void splitCardCompatibilityDataBalanced(Connection conn) {
        try {
            // Intervalli di compatibilità
            double[][] intervals = {
                    { 0.0, 0.29 },
                    { 0.3, 0.69 },
                    { 0.7, 1.0 }
            };

            // Proporzioni per ogni tabella
            double trainingRatio = 0.7;
            double validationRatio = 0.2;
            double testRatio = 0.1;

            // Prepara query di inserimento
            String insertTraining = "INSERT INTO card_training (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";
            String insertValidation = "INSERT INTO card_validation (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";
            String insertTest = "INSERT INTO card_test (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";

            // Itera sugli intervalli
            for (double[] interval : intervals) {
                // Query per selezionare i dati nell'intervallo specifico con LIMIT per il batch
                String selectQuery = "SELECT id_card1, id_card2, compatibily FROM card_compatibily " +
                        "WHERE compatibily BETWEEN ? AND ? LIMIT ? OFFSET ?";

                int batchSize = 1_000_000; // Processa un milione di righe per volta
                int offset = 0;
                boolean hasMoreData;

                do {

                    hasMoreData = false;

                    try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                        selectStmt.setDouble(1, interval[0]);
                        selectStmt.setDouble(2, interval[1]);
                        selectStmt.setInt(3, batchSize);
                        selectStmt.setInt(4, offset);

                        ResultSet rs = selectStmt.executeQuery();

                        List<double[]> records = new ArrayList<>();
                        while (rs.next()) {
                            hasMoreData = true; // C'è ancora più dati da elaborare
                            records.add(new double[] {
                                    rs.getInt("id_card1"),
                                    rs.getInt("id_card2"),
                                    rs.getDouble("compatibily") // Mantiene il valore continuo
                            });
                        }

                        if (!records.isEmpty()) {
                            // Shuffle dei record per randomizzazione
                            Collections.shuffle(records);

                            // Calcola le divisioni
                            int totalRecords = records.size();
                            int trainingCount = (int) (totalRecords * trainingRatio);
                            int validationCount = (int) (totalRecords * validationRatio);

                            // Sublista per ogni tabella
                            List<double[]> trainingData = records.subList(0, trainingCount);
                            List<double[]> validationData = records.subList(trainingCount,
                                    trainingCount + validationCount);
                            List<double[]> testData = records.subList(trainingCount + validationCount, totalRecords);

                            // Inserisci nei rispettivi dataset
                            System.out.println("training");
                            insertBatch(conn, insertTraining, trainingData);
                            System.out.println("validation");
                            insertBatch(conn, insertValidation, validationData);
                            System.out.println("test");
                            insertBatch(conn, insertTest, testData);

                            System.out.println("Batch di dati processato con offset " + offset + " per intervallo: [" +
                                    interval[0] + " - " + interval[1] + "]");
                        }

                        offset += batchSize; // Incrementa l'offset per il prossimo batch
                    }
                } while (hasMoreData);
            }

            System.out.println("Divisione completata in card_training, card_validation e card_test.");
        } catch (SQLException e) {
            System.err.println("Errore durante la divisione dei dati: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo per l'inserimento batch
    private void insertBatch(Connection conn, String query, List<double[]> data) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (double[] record : data) {
                pstmt.setDouble(1, record[0]);
                pstmt.setDouble(2, record[1]);
                pstmt.setDouble(3, record[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch(); // Esegue il batch
        }
    }

    public static List<CardTrainingRecord> getAllCardTrainingRecords(Connection conn) {
        // Query per selezionare i record dalla tabella `card_training` con supporto
        // batch
        String query = "SELECT id_card1, id_card2, compatibily FROM card_training LIMIT ? OFFSET ?";
        List<CardTrainingRecord> records = new ArrayList<>();

        int batchSize = 6000000; // Dimensione del batch
        int offset = 0; // Offset iniziale
        boolean hasMoreData;

        try {
            do {
                hasMoreData = false;

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    // Imposta i parametri per il batch
                    pstmt.setInt(1, batchSize);
                    pstmt.setInt(2, offset);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        int recordCount = 0;

                        // Itera sui risultati del batch
                        while (rs.next()) {
                            hasMoreData = true;
                            recordCount++;

                            // Costruisci un oggetto CardTrainingRecord
                            CardTrainingRecord record = new CardTrainingRecord(
                                    rs.getInt("id_card1"),
                                    rs.getInt("id_card2"),
                                    rs.getDouble("compatibily"));
                            records.add(record); // Aggiungi il record alla lista
                        }

                        System.out.println(
                                "Batch processato: " + recordCount + " record recuperati con offset " + offset);
                    }
                }

                offset += batchSize; // Incrementa l'offset per il prossimo batch
            } while (hasMoreData);

        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dei record da card_training: " + e.getMessage());
            e.printStackTrace();
        }

        return records; // Restituisce la lista dei record
    }

    public static List<CardTrainingRecord> getAllCardValidationRecords(int batchSize, int offset, String tableName,
            Connection conn) {
        // Query per selezionare i record dalla tabella passata
        String query = "SELECT * FROM " + tableName + "  LIMIT ? OFFSET ?";
        List<CardTrainingRecord> records = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta i parametri per il batch
            pstmt.setInt(1, batchSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Itera sui risultati e costruisci la lista di record
                while (rs.next()) {
                    CardTrainingRecord record = new CardTrainingRecord(
                            rs.getInt("id_card1"),
                            rs.getInt("id_card2"),
                            rs.getDouble("compatibily"));
                    records.add(record);
                }

                System.out.println("Batch processato: " + records.size() + " record recuperati con offset " + offset);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Errore durante il recupero dei record dalla tabella " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }

        return records; // Restituisce la lista dei record
    }

    public static Carta getCartaById(int cardId, Connection conn) {
        String query = "SELECT * FROM carte WHERE id = ?";
        Carta carta = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    carta = new Carta();
                    carta.setId(rs.getInt("id"));
                    carta.setDesc(rs.getString("desk"));
                    carta.setName(rs.getString("name"));
                    carta.setType(rs.getString("type"));
                    carta.setRace(rs.getString("race"));
                    carta.setAtk(rs.getString("atk"));
                    carta.setDef(rs.getString("def"));
                    carta.setLevel(rs.getString("level"));
                    carta.setArchetype(rs.getString("archetype"));
                    carta.setHumanReadableCardType(rs.getString("humanReadableCardType"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero della carta con ID " + cardId + ": " + e.getMessage());
        }

        return carta;
    }

    // recupero le carte in forma vettoriale con la loro compatibily utilizzzando
    // batch visto la gran quantita da database
    public List<VettoreCardCompatibility> getVettoreCardCompatibilityBatch(int batchSize, int offset,
            Connection conn) {
        String query = "SELECT vettore , target FROM vettoreCardCompatibily LIMIT ? OFFSET ?";
        List<VettoreCardCompatibility> records = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, batchSize); // Imposta il limite (numero di righe)
            pstmt.setInt(2, offset); // Imposta l'offset (da dove iniziare)

            try (ResultSet rs = pstmt.executeQuery()) {
                int rowCount = 0;

                while (rs.next()) {
                    String vettore = rs.getString("vettore");
                    Double target = rs.getDouble("target");

                    // Aggiungi il record alla lista
                    records.add(new VettoreCardCompatibility(vettore, target));
                    rowCount++;
                }

                if (rowCount == 0) {
                    System.out.println("Nessun dato trovato per batchSize=" + batchSize + " e offset=" + offset);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                    "Errore durante il recupero dei dati dalla tabella vettoreCardCompatibily: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    // inserisci dati nella tabella vettore_cardCOmpatibily
    public static void inserisciVettoreCardCompatibily(Connection conn, double[] vettore, Double target) {
        // Query di inserimento nella tabella
        String query = """
                INSERT INTO vettoreCardCompatibily (vettore, target)
                VALUES (?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Converte il vettore in una stringa delimitata da virgole
            String vettoreStringa = Arrays.toString(vettore)
                    .replace("[", "") // Rimuove la parentesi iniziale
                    .replace("]", "") // Rimuove la parentesi finale
                    .trim();

            // Imposta i parametri nella query
            pstmt.setString(1, vettoreStringa); // Vettore come stringa
            if (target != null) {
                pstmt.setDouble(2, target); // Target (compatibilità effetto)
            } else {
                pstmt.setNull(2, java.sql.Types.DOUBLE);
            }

            // Esegue l'inserimento
            pstmt.executeUpdate();
            // System.out.println("Valori inseriti correttamente nella tabella
            // vettore_card_compatibily.");
        } catch (SQLException e) {
            System.err
                    .println("Errore durante l'inserimento nella tabella vettore_card_compatibily: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertCardsInRange(Connection conn, int maxRecords) {
        try {
            // Definisci i limiti dell'intervallo
            double minRange = 0.000;
            double maxRange = 0.000;

            // Query per selezionare i dati nell'intervallo specifico con LIMIT per il batch
            String selectQuery = "SELECT id_card1, id_card2, compatibily FROM card_training WHERE compatibily BETWEEN ? AND ? LIMIT ? OFFSET ?";

            // Query di inserimento per la tabella di output
            String insertQuery = "INSERT ignore INTO card_test_reduce (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";

            int totalInserted = 0; // Contatore per i record inseriti
            int batchSize = 500_000; // Dimensione del batch
            int offset = 0;
            boolean hasMoreData;

            do {
                hasMoreData = false;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setDouble(1, minRange);
                    selectStmt.setDouble(2, maxRange);
                    selectStmt.setInt(3, batchSize);
                    selectStmt.setInt(4, offset);

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        List<double[]> records = new ArrayList<>();
                        while (rs.next()) {
                            hasMoreData = true;
                            records.add(new double[] {
                                    rs.getInt("id_card1"),
                                    rs.getInt("id_card2"),
                                    rs.getDouble("compatibily")
                            });
                        }

                        // Shuffle dei record per randomizzazione
                        Collections.shuffle(records);

                        // Seleziona solo i record rimanenti fino al limite massimo
                        int remaining = maxRecords - totalInserted;
                        List<double[]> recordsToInsert = records.subList(0, Math.min(remaining, records.size()));

                        // Inserisci i record nella tabella di output
                        insertBatch(conn, insertQuery, recordsToInsert);

                        totalInserted += recordsToInsert.size();
                        System.out.println("Intervallo [" + minRange + " - " + maxRange + "]: Inseriti "
                                + recordsToInsert.size() + " record (Totale: " + totalInserted + ")");

                        // Controlla se il limite massimo è stato raggiunto
                        if (totalInserted >= maxRecords) {
                            System.out.println("Limite massimo raggiunto: " + totalInserted);
                            break;
                        }
                    }

                    offset += batchSize; // Incrementa l'offset
                }
            } while (hasMoreData);

            System.out.println("Completato l'inserimento di 500.000 record nell'intervallo [" + minRange + " - "
                    + maxRange + "] nella tabella: card_training_reduce");
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void splitCardCompatibilityDataBalancedSingleTable(Connection conn, int maxRecordsPerCategory,
            String tableIn, String tableOut) {
        try {
            // Intervalli di compatibilità
            double[][] intervals = {
                    { 0.0, 0.35 },
                    { 0.36, 0.69 },
                    { 0.7, 1 }
            };

            // Query per selezionare i dati nell'intervallo specifico con LIMIT per il batch
            String selectQuery = "SELECT id_card1, id_card2, compatibily " +
                    "FROM " + tableIn +
                    " WHERE compatibily BETWEEN ? AND ? " +
                    "ORDER BY RAND() " +
                    "LIMIT ? OFFSET ?";

            // Query di inserimento per la tabella di output
            String insertQuery = "INSERT ignore INTO " + tableOut
                    + " (id_card1, id_card2, compatibily) VALUES (?, ?, ?)";

            for (double[] interval : intervals) {
                int totalInserted = 0; // Contatore per i record inseriti
                int batchSize = 1_000_000; // Dimensione del batch
                int offset = 0;
                boolean hasMoreData;

                do {
                    hasMoreData = false;

                    try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                        selectStmt.setDouble(1, interval[0]);
                        selectStmt.setDouble(2, interval[1]);
                        selectStmt.setInt(3, batchSize);
                        selectStmt.setInt(4, offset);

                        try (ResultSet rs = selectStmt.executeQuery()) {
                            List<double[]> records = new ArrayList<>();
                            while (rs.next()) {
                                hasMoreData = true;
                                records.add(new double[] {
                                        rs.getInt("id_card1"),
                                        rs.getInt("id_card2"),
                                        rs.getDouble("compatibily")
                                });
                            }

                            // Shuffle dei record per randomizzazione
                            Collections.shuffle(records);

                            // Seleziona solo i record rimanenti fino al limite massimo
                            int remaining = maxRecordsPerCategory - totalInserted;
                            List<double[]> recordsToInsert = records.subList(0, Math.min(remaining, records.size()));

                            // Inserisci i record nella tabella di output
                            insertBatch(conn, insertQuery, recordsToInsert);

                            totalInserted += recordsToInsert.size();
                            System.out.println("Intervallo " + Arrays.toString(interval) + ": Inseriti "
                                    + recordsToInsert.size() + " record (Totale: " + totalInserted + ")");

                            // Controlla se il limite massimo è stato raggiunto
                            if (totalInserted >= maxRecordsPerCategory) {
                                System.out.println(
                                        "Limite massimo raggiunto per l'intervallo: " + Arrays.toString(interval));
                                break;
                            }
                        }

                        offset += batchSize; // Incrementa l'offset
                    }
                } while (hasMoreData);

                System.out.println("Completato l'inserimento per l'intervallo: " + Arrays.toString(interval));
            }

            System.out.println("Divisione completata nella tabella: card_traing_reduce");
        } catch (SQLException e) {
            System.err.println("Errore durante la divisione bilanciata dei dati: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertNewCardList(Connection conn, List<Carta> newCarte) {

        if (!newCarte.isEmpty()) {

            for (Carta card : newCarte) {

                insertSingleCard(conn, card);

            }

        }

    }

    public static void insertNewCardSetDb(Connection conn, List<Card_Set> newCardSet) {

        if (!newCardSet.isEmpty()) {

            for (Card_Set cardSet : newCardSet) {

                inserisciCardSet(conn, cardSet);

            }

        }

    }

    public static void inserisciCardSet(Connection conn, Card_Set cardSet) {
        String insertSQL = """
                    INSERT ignore INTO card_sets (set_name, set_code, num_of_cards, tcg_date, set_image)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            // Imposta i parametri per il PreparedStatement
            preparedStatement.setString(1, cardSet.getSet_name());
            preparedStatement.setString(2, cardSet.getSet_code());
            preparedStatement.setInt(3, Integer.parseInt(cardSet.getNum_of_cards()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate;

            if (cardSet.getDateTgc() != null) {
                localDate = LocalDate.parse(cardSet.getDateTgc(), formatter);

                // Conversione in java.sql.Date
                Date sqlDateTcg = Date.valueOf(localDate);
                preparedStatement.setDate(4, sqlDateTcg);
            } else {
                preparedStatement.setDate(4, null);
            }

            preparedStatement.setString(5, cardSet.getSet_image());

            // Esegue l'inserimento
            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Card set inserito correttamente: " + cardSet.getSet_name());
            } else {
                System.out.println("Nessun card set è stato inserito.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento del card set: " + cardSet.getSet_name());
            e.printStackTrace();
        }
    }

    public static void inserisciInNewCard(Connection conn, int cardId) {
        // SQL per inserire l'ID carta e la data corrente nella tabella newCard
        String insertSQL = "INSERT INTO newCard (id, insert_date) VALUES (?, CURDATE())";

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            // Imposta i parametri della query
            preparedStatement.setInt(1, cardId);

            // Esegui l'inserimento
            int rowsInserted = preparedStatement.executeUpdate();

            // Stampa il risultato
            if (rowsInserted > 0) {
                System.out.println("Carta con ID " + cardId + " inserita nella tabella newCard.");
            } else {
                System.out.println("Nessuna carta inserita.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento nella tabella newCard per la carta con ID: " + cardId);
            e.printStackTrace();
        }
    }

    public static void inserisciInNewCard_sets(Connection conn, int card_setsId) {
        // SQL per inserire l'ID carta e la data corrente nella tabella newCard
        String insertSQL = "INSERT INTO newCard_sets (id, insert_date) VALUES (?, CURDATE())";

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL)) {
            // Imposta i parametri della query
            preparedStatement.setInt(1, card_setsId);

            // Esegui l'inserimento
            int rowsInserted = preparedStatement.executeUpdate();

            // Stampa il risultato
            if (rowsInserted > 0) {
                System.out.println("Card_set con ID " + card_setsId + " inserita nella tabella newCard.");
            } else {
                System.out.println("Nessuna carta inserita.");
            }
        } catch (SQLException e) {
            System.err
                    .println("Errore durante l'inserimento nella tabella newCard per la carta con ID: " + card_setsId);
            e.printStackTrace();
        }
    }

    public static int getCardSetIdByNameAndCode(Connection conn, String setName, String setCode) {
        String query = "SELECT id FROM card_sets WHERE set_name = ? AND set_code = ?";
        int cardSetId = -1; // Valore di default se non trovato

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Imposta i parametri nella query
            pstmt.setString(1, setName);
            pstmt.setString(2, setCode);

            // Esegui la query e ottieni il risultato
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    cardSetId = rs.getInt("id"); // Estrae l'id
                }
            }
        } catch (SQLException e) {
            // Gestione degli errori
            System.err.println("Errore durante la selezione dell'id dal DB.");
            e.printStackTrace();
        }

        return cardSetId;
    }

    public static void eliminaCarteVecchieDaNewCard(Connection conn) {
        // Query per eliminare le carte inserite ieri
        String deleteSQL = "DELETE FROM newCard WHERE insert_date <= DATE(NOW() - INTERVAL 150 DAY)";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            // Esegui la query
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Carte eliminate da newCard " + rowsDeleted);
        } catch (SQLException e) {
            // Gestione degli errori
            System.err.println("Errore durante l'eliminazione delle carte inserite ieri da newCard.");
            e.printStackTrace();
        }
    }

    public static void eliminaCard_setsVecchiDaNewCard(Connection conn) {
        // Query per eliminare le carte inserite ieri
        String deleteSQL = "DELETE FROM newCard_sets WHERE insert_date <= DATE(NOW() - INTERVAL 150 DAY)";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            // Esegui la query
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Carte eliminate da newCard " + rowsDeleted);
        } catch (SQLException e) {
            // Gestione degli errori
            System.err.println("Errore durante l'eliminazione delle carte inserite ieri da newCard.");
            e.printStackTrace();
        }
    }

    public static List<Carta> getCardsByCardId(Connection conn, int cardId) {
        String query = """
                SELECT *
                FROM sys.card_cardsets
                JOIN carte
                ON card_cardsets.card_id = carte.id
                WHERE card_cardsets.card_id = ?;
                """;

        List<Carta> cards = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, cardId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Carta card = new Carta();

                    // Setta i campi principali della carta
                    card.setId(rs.getInt("id"));
                    card.setName(rs.getString("name"));
                    card.setType(rs.getString("type"));
                    card.setRace(rs.getString("race"));
                    card.setAtk(rs.getString("atk"));
                    card.setDef(rs.getString("def"));
                    card.setLevel(rs.getString("level"));
                    card.setArchetype(rs.getString("archetype"));
                    card.setHumanReadableCardType(rs.getString("humanReadableCardType"));
                    card.setFrameType(rs.getString("frameType"));
                    card.setDesc(rs.getString("desk"));
                    card.setYgoprodeck_url(rs.getString("ygoprodeck_url"));
                    card.setAttribute(rs.getString("attribute"));
                    card.setHas_effect(rs.getBoolean("has_effect"));

                    // Setta i prezzi
                    CardPrices prices = new CardPrices(
                            rs.getString("cardmarket_price"),
                            rs.getString("ebay_price"),
                            rs.getString("amazon_price"),
                            rs.getString("coolstuffinc_price"),
                            rs.getString("tcgplayer_price"));
                    List<CardPrices> priceList = new ArrayList<>();
                    priceList.add(prices);
                    card.setCard_prices(priceList);

                    // Setta i set
                    CardSets cardSet = new CardSets(
                            rs.getString("set_name"),
                            rs.getString("set_code"),
                            rs.getString("set_rarity"),
                            rs.getString("set_rarity_code"),
                            rs.getString("set_price"),
                            new Prices(
                                    rs.getFloat("high_price"),
                                    rs.getFloat("low_price"),
                                    rs.getFloat("average_price"),
                                    rs.getFloat("shift"),
                                    rs.getFloat("shift_3"),
                                    rs.getFloat("shift_7"),
                                    rs.getFloat("shift_21"),
                                    rs.getFloat("shift_30"),
                                    rs.getFloat("shift_90"),
                                    rs.getFloat("shift_180"),
                                    rs.getFloat("shift_365"),
                                    rs.getString("updated_at")));
                    List<CardSets> cardSetList = new ArrayList<>();
                    cardSetList.add(cardSet);
                    card.setCard_sets(cardSetList);

                    // Setta la banlist
                    Banlist_info banlist = new Banlist_info(
                            rs.getString("ban_tcg"),
                            rs.getString("ban_ocg"),
                            rs.getString("ban_goat"));
                    card.setBanlist_info(banlist);

                    // Aggiungi la carta alla lista
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle carte: " + e.getMessage());
            e.printStackTrace();
        }

        return cards;
    }

    public static void insertCardCompatibility(Connection conn, int id_card1, int id_card2, double compatibility) {
        if (conn == null) {
            throw new IllegalArgumentException("La connessione al database non può essere nulla.");
        }

        String insertQuery = """
                    INSERT ignore INTO card_compatibility (id_card1, id_card2, compatibily)
                    VALUES (?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, id_card1);
            pstmt.setInt(2, id_card2);
            pstmt.setDouble(3, compatibility);

            int rowsAffected = pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento della compatibilità: " + e.getMessage());
            e.printStackTrace();

        }
    }

}

// for (CardData data : nameeIdCard) {

// List<CardData> apiData = ApiYugiohPrices.getCardPrice(data.getName());

// if (apiData == null || apiData.isEmpty()) {
// System.out.println("Nessun dato trovato per la carta: " + data.getName());
// continue;
// }

// // Query per aggiornare i dati nella tabella `card_cardsets`
// String updateSQL = """
// UPDATE card_cardsetsBackup
// SET high_price = ?, low_price = ?, average_price = ?, shift = ?, shift_3 = ?,
// shift_7 = ?, shift_21 = ?, shift_30 = ?, shift_90 = ?, shift_180 = ?,
// shift_365 = ?, updated_at = ?
// WHERE card_id = ? AND set_name = ? AND set_code = ?
// """;

// try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
// for (CardData apiCard : apiData) {

// // Verifica che i dati siano disponibili e corretti
// if (apiCard.getPrice_data() == null ||
// !"success".equals(apiCard.getPrice_data().getStatus())) {
// System.out.println("Dati di prezzo non disponibili per il set: " +
// apiCard.getName());
// continue;
// }

// // Prepara l'aggiornamento
// updateStmt.setBigDecimal(1,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getHigh()));
// updateStmt.setBigDecimal(2, new
// BigDecimal(apiCard.getPrice_data().getData().getPrices().getLow()));
// updateStmt.setBigDecimal(3,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getAverage()));
// updateStmt.setBigDecimal(4,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift()));
// updateStmt.setBigDecimal(5,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_3()));
// updateStmt.setBigDecimal(6,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_7()));
// updateStmt.setBigDecimal(7,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_21()));
// updateStmt.setBigDecimal(8,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_30()));
// updateStmt.setBigDecimal(9,
// new BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_90()));
// updateStmt.setBigDecimal(10,
// new
// BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_180()));
// updateStmt.setBigDecimal(11,
// new
// BigDecimal(apiCard.getPrice_data().getData().getPrices().getShift_365()));
// updateStmt.setTimestamp(12,
// java.sql.Timestamp.valueOf(apiCard.getPrice_data().getData().getPrices().getUpdated_at()));

// // Imposta i parametri per la chiave primaria
// updateStmt.setInt(13, data.getId()); // ID della carta
// updateStmt.setString(14, apiCard.getName()); // Nome del set
// updateStmt.setString(15, apiCard.getPrint_tag());

// // Esegui l'aggiornamento
// int rowsUpdated = updateStmt.executeUpdate();
// if (rowsUpdated > 0) {
// System.out.println("Aggiornati i dati per la carta: " + apiCard.getName() + "
// ("
// + apiCard.getPrint_tag() + ")");
// } else {
// System.out.println("Nessun aggiornamento effettuato per la carta: " +
// apiCard.getName() + " ("
// + apiCard.getPrint_tag() + ")");
// }
// }
// } catch (SQLException e) {
// System.err.println("Errore durante l'aggiornamento della tabella
// card_cardsets: " + e.getMessage());
// throw e;
// }
// }