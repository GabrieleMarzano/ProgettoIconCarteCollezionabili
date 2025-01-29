package com.progettoicon;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.progettoicon.KnowledgeGraph.CardGraph;
import com.progettoicon.KnowledgeGraph.GraphVisualizerFX;
import com.progettoicon.RandomForest.RandomForestClassifier;
import com.progettoicon.ReteNeurale.MainNeuralNetwork;

public class Main {

    private static Database db_yu_gi_oh;
    private static Utente utente;
    private static final int dimensione_vettore_input_network = 1121;
    private static final String modelPath = "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/file/network_model2.nn";

    // static Client apiClient;
    // static AuthClient authClient;

    public static void main(String[] args) throws Exception {

        db_yu_gi_oh = new Database("jdbc:mysql://localhost:3306/sys", "root", "12345678");
        Login login = new Login(db_yu_gi_oh);
        boolean credenziali_corrette;

        while (db_yu_gi_oh.getConn() == null) {

            System.out.println("Database non connesso");
            Thread.sleep(6000);
            db_yu_gi_oh = new Database("jdbc:mysql://localhost:3306/sys", "root", "12345678");
            login = new Login(db_yu_gi_oh);
        }

        MainNeuralNetwork network;
        File modelFile = new File(modelPath);

        if (modelFile.exists()) {
            // Carica il modello esistente
            System.out.println("Caricamento del modello esistente...");
            network = MainNeuralNetwork.loadModel(modelPath);

        } else {
            // Crea un nuovo modello
            System.out.println("Creazione di un nuovo modello...");
            network = new MainNeuralNetwork(dimensione_vettore_input_network);

        }

        Controll.AggiornaConoscenza(db_yu_gi_oh.getConn(), network);
        // db_yu_gi_oh.splitCardCompatibilityDataBalancedSingleTable(db_yu_gi_oh.getConn(),
        // 2, "card_training",
        // "card_traing_reduce");

        // db_yu_gi_oh.insertCardsInRange(db_yu_gi_oh.getConn(), 2_000);

        // List<Carta> carte = db_yu_gi_oh.getAllCarts(db_yu_gi_oh.getConn());
        // double compatibliy;

        // // crea compatibily tra carte per adestramento

        // for (int i = 0; i < carte.size(); i++) {

        // System.out.println("carte mancanti: " + (carte.size() - i));

        // for (int j = 0; j < carte.size(); j++) {

        // if (i < j) {

        // compatibliy = Controll.cardCompatibily(db_yu_gi_oh.getConn(), carte.get(i),
        // carte.get(j));

        // // se la compatibily è diversa da 0 la salvo nel db
        // if (compatibliy != 0.0) {

        // db_yu_gi_oh.insertCardCompatibily(db_yu_gi_oh.getConn(),
        // carte.get(i).getId(),
        // carte.get(j).getId(),
        // compatibliy);

        // }

        // }

        // }

        // }

        // for (int i = 0; i < carte.size(); i++) {

        // System.out.println("carte mancanti: " + (carte.size() - i));

        // for (int j = 0; j < carte.size(); j++) {

        // if (i < j) {

        // db_yu_gi_oh.ensureCardCompatibilyExists(db_yu_gi_oh.getConn(),
        // carte.get(i).getId(),
        // carte.get(j).getId());
        // }

        // }

        // }

        // dividi i dati in addestramento test e validazione
        // db_yu_gi_oh.splitCardCompatibilityDataBalanced(db_yu_gi_oh.getConn());

        // Trainer trainer = new Trainer(network);

        // Controlla se il modello esiste

        // Addestramento del modello
        // Trainer trainer = new Trainer(network);

        // int batchSize = 300_000; // Numero di record per batch

        // int maxRecords = 4_000_000; // Numero massimo di record da processare

        // trainer.trainModel(network, trainer, batchSize, maxRecords,
        // "card_traing_reduce", db_yu_gi_oh.getConn(), modelPath);

        // System.out.println("Valutazione del modello...");
        // Evaluator evaluator = new Evaluator();
        // evaluator.evaluate(network, 100_000, db_yu_gi_oh.getConn(), 5,
        // "card_validation1");

        // Predizione
        Controll.CompatibilyNeuralNetworkPipeline(db_yu_gi_oh.getConn(), network);

        // // Percorso del file ARFF contenente i dati di addestramento
        // String trainingDataPath =
        // "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/training_data.arff";
        // String testDataPath =
        // "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/training_data.arff";

        // Percorso del file ARFF contenente i dati di addestramento
        String trainingDataPathClassification = "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/classification_data.arff";
        String testDataPathClassification = "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/classification_data_test.arff";

        RandomForestClassifier.createClassificationArffDataset(db_yu_gi_oh.getConn(),
                trainingDataPathClassification);

        // Configurazione del modello
        int numTrees = 100; // Numero di alberi nella foresta
        int maxDepth = 30; // Profondità massima degli alberi

        // Controll.runRandomForestPipeline(db_yu_gi_oh, trainingDataPath, testDataPath,
        // numTrees, maxDepth) ;

        // Esecuzione della pipeline
        Controll.runRandomForestClassifierPipeline(db_yu_gi_oh,
                trainingDataPathClassification,
                testDataPathClassification, numTrees, maxDepth);

        // //Creazione dell'oggetto RandomForestClassifier
        // RandomForestClassifier randomForestClassifier = new RandomForestClassifier();

        // Controll.classificationPipeline(db_yu_gi_oh.getConn(),
        // randomForestClassifier);

        // String filePathcardset =
        // "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/classification_data.arff";
        // VarianceCalculator.calculateVariance(filePathcardset);
        // VarianceCalculator.calculateTargetVariance(filePathcardset);
        CardGraph grafo = new CardGraph();
        grafo.ensureGraphExists(db_yu_gi_oh.getConn());

        // Trova le componenti connesse
        List<Set<Integer>> connectedComponents = grafo.findConnectedComponents();
        // Itera su ciascun nodo della componente
        // Itera su ciascuna componente connessa
        for (Set<Integer> component : connectedComponents) {
            System.out.println("Inserisc id card:  89631139");

            System.out.println("=== Component Info ===");

            // Itera su ciascun nodo della componente
            for (Integer nodeId : component) {
                // Recupera le informazioni sulla carta dal database
                Carta card = db_yu_gi_oh.getCartaById(nodeId, db_yu_gi_oh.getConn());

                // Prepara valori sicuri per la stampa
                String level = (card.getLevel() != null) ? String.valueOf(card.getLevel()) : "N/A";
                String atk = (card.getAtk() != null) ? String.valueOf(card.getAtk()) : "N/A";
                String def = (card.getDef() != null) ? String.valueOf(card.getDef()) : "N/A";
                String attribute = (card.getAttribute() != null) ? card.getAttribute() : "N/A";
                String archetype = (card.getArchetype() != null) ? card.getArchetype() : "N/A";

                // Stampa le informazioni essenziali in modo formattato
                System.out.printf("""
                        ------------------------------
                        Card ID: %d
                        Name: %s
                        Type: %s
                        Archetype: %s
                        Attribute: %s
                        Level: %s
                        Attack: %s
                        Defense: %s
                        ------------------------------
                        """,
                        card.getId(),
                        card.getName(),
                        card.getType(),
                        archetype,
                        attribute,
                        level,
                        atk,
                        def);
            }
        }

        // Visualizza ciascuna componente connessa come un sottografo
        for (Set<Integer> component : connectedComponents) {
            CardGraph componentGraph = new CardGraph();
            componentGraph.setGraph(grafo.createSubgraphFromComponent(component));

            // Visualizza il sottografo della componente connessa
            GraphVisualizerFX.setGraph(componentGraph.getGraph());
            GraphVisualizerFX.launch(GraphVisualizerFX.class, args);
        }

        credenziali_corrette = login.verificaCredenziali();

        while (credenziali_corrette == false) {

            System.out.println("Credenziali errate");
            credenziali_corrette = login.verificaCredenziali();

        }

        login.chiudiScanner();

        System.out.println("Accesso Effettuato");

        String email = login.getEmail();
        String password = login.getPassword();
        int id = Database.getIdByEmail(db_yu_gi_oh.getConn(), email);
        String nome = Database.getNomeByEmail(db_yu_gi_oh.getConn(), email);
        String cognome = Database.getCognomeByEmail(db_yu_gi_oh.getConn(), email);
        String nickname = Database.getNicknameByEmail(db_yu_gi_oh.getConn(), email);
        int id_collezione = Database.getCollezioneIdByUtenteId(db_yu_gi_oh.getConn(), id);

        utente = new Utente(nome, cognome, password, email, nickname, id, id_collezione);

        // System.out.println(utente.toString());

    }
}

// // Salva il modello dopo ogni batch
// System.out.println("Salvataggio del modello addestrato...");
// network.saveModel(modelPath);