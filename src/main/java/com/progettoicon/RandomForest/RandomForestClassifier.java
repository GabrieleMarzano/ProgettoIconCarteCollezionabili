package com.progettoicon.RandomForest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class RandomForestClassifier {
    private static final String MODEL_FILE = "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/classifier_model_weka3.model";
    private RandomForest model;

    // Costruttore: Carica il modello se esiste
    public RandomForestClassifier() {
        this.model = loadModel();
    }

    // Metodo per verificare se il modello è disponibile
    public boolean isModelLoaded() {
        return model != null;
    }

    // Addestra un nuovo modello
    public void trainModel(Instances trainingData, int numTrees, int maxDepth) {
        if (trainingData == null || trainingData.numInstances() == 0) {
            throw new IllegalArgumentException("Dati di addestramento non validi.");
        }

        try {
            model = new RandomForest();
            model.setNumIterations(numTrees); // Imposta il numero di alberi
            model.setMaxDepth(maxDepth); // Imposta la profondità massima degli alberi
            model.buildClassifier(trainingData);
            saveModel(); // Salva il modello addestrato
            System.out.println("Modello Random Forest per classificazione addestrato con successo.");
        } catch (Exception e) {
            System.err.println("Errore durante l'addestramento del modello: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Predice la classe per un'istanza
    public String predict(Instance instance) {
        if (model == null) {
            throw new IllegalStateException("Il modello non è stato caricato o addestrato.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("L'istanza di input non può essere nulla.");
        }

        try {
            double classIndex = model.classifyInstance(instance);
            return instance.classAttribute().value((int) classIndex); // Restituisce la classe predetta come stringa
        } catch (Exception e) {
            System.err.println("Errore durante la previsione: " + e.getMessage());
            e.printStackTrace();
            return null; // Valore di fallback in caso di errore
        }
    }

    // Salva il modello su file
    private void saveModel() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MODEL_FILE))) {
            oos.writeObject(model);
            System.out.println("Modello salvato correttamente in: " + MODEL_FILE);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio del modello: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Carica il modello da file
    private RandomForest loadModel() {
        File modelFile = new File(MODEL_FILE);
        if (!modelFile.exists()) {
            System.out.println("File del modello non trovato. Verrà generato un nuovo modello.");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile))) {
            return (RandomForest) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore durante il caricamento del modello: " + e.getMessage());
            return null;
        }
    }

    public RandomForest getModel() {
        return model;
    }

    public void setModel(RandomForest model) {
        this.model = model;
    }

    // Metodo per salvare i dati in formato ARFF
    public void saveDataAsArff(Instances data, String filePath) {
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(filePath));
            saver.writeBatch();
            System.out.println("Dati salvati in formato ARFF in: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dei dati: " + e.getMessage());
        }
    }

    // Metodo per caricare i dati in formato ARFF
    public Instances loadDataFromArff(String filePath) {
        try {
            ArffLoader loader = new ArffLoader();
            loader.setFile(new File(filePath));
            return loader.getDataSet();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento dei dati: " + e.getMessage());
            return null;
        }
    }

    public static void createClassificationArffDataset(Connection conn, String outputFilePath) {
        String query = """
                               SELECT
                    ccs.average_price AS ccs_average_price,
                    ccs.shift,
                    ccs.shift_3,
                    ccs.shift_7,
                    ccs.shift_21,
                    ccs.shift_30,
                    ccs.shift_90,
                    ccs.shift_180,
                    ccs.shift_365,
                    ccsb.average_price AS ccsb_average_price
                FROM sys.card_cardsets AS ccs
                JOIN sys.card_cardsetsBackup AS ccsb
                ON ccs.id = ccsb.id
                WHERE ccs.updated_at != ccsb.updated_at;
                                """;

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            // Definizione delle feature
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("ccs_average_price"));
            attributes.add(new Attribute("shift"));
            attributes.add(new Attribute("shift_3"));
            attributes.add(new Attribute("shift_7"));
            attributes.add(new Attribute("shift_21"));
            attributes.add(new Attribute("shift_30"));
            attributes.add(new Attribute("shift_90"));
            attributes.add(new Attribute("shift_180"));
            attributes.add(new Attribute("shift_365"));

            // Definizione della classe (target) come attributo nominale
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("prezzo_diminuisce");
            classValues.add("prezzo_aumenta");
            attributes.add(new Attribute("class", classValues)); // Classe nominale

            // Creazione del dataset vuoto
            Instances dataset = new Instances("CardPriceClassification", attributes, 0);
            dataset.setClassIndex(attributes.size() - 1); // Ultimo attributo come target

            // Riempimento del dataset con i dati dal ResultSet
            while (rs.next()) {
                double[] instanceValues = new double[attributes.size()]; // Include lo spazio per la classe
                instanceValues[0] = rs.getDouble("ccs_average_price");
                instanceValues[1] = rs.getDouble("shift");
                instanceValues[2] = rs.getDouble("shift_3");
                instanceValues[3] = rs.getDouble("shift_7");
                instanceValues[4] = rs.getDouble("shift_21");
                instanceValues[5] = rs.getDouble("shift_30");
                instanceValues[6] = rs.getDouble("shift_90");
                instanceValues[7] = rs.getDouble("shift_180");
                instanceValues[8] = rs.getDouble("shift_365");

                // Determina il valore della classe
                String classValue = rs.getDouble("ccs_average_price") > rs.getDouble("ccsb_average_price")
                        ? "prezzo_diminuisce"
                        : "prezzo_aumenta";

                // Creazione di una nuova istanza
                DenseInstance instance = new DenseInstance(1.0, instanceValues);
                instance.setDataset(dataset); // Associa l'istanza al dataset
                instance.setClassValue(classValue); // Imposta il valore della classe

                // Aggiungi l'istanza al dataset
                dataset.add(instance);
            }

            // Salvataggio del dataset in formato ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataset);
            saver.setFile(new File(outputFilePath));
            saver.writeBatch();

            System.out.println("Dataset ARFF per la classificazione creato con successo in: " + outputFilePath);

        } catch (Exception e) {
            System.err.println("Errore durante la creazione del dataset ARFF per classificazione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String classifyCard(RandomForestClassifier model, Double[] cardAttributes) {
        if (model == null) {
            throw new IllegalArgumentException("Il modello fornito non può essere nullo.");
        }
        if (cardAttributes == null || cardAttributes.length != 9) { // Assumi 9 attributi esclusa la classe
            throw new IllegalArgumentException("Gli attributi della carta devono contenere esattamente 9 valori.");
        }

        try {
            // Converti Double[] in double[]
            double[] attributes = new double[cardAttributes.length];
            for (int i = 0; i < cardAttributes.length; i++) {
                attributes[i] = cardAttributes[i] != null ? cardAttributes[i] : Double.NaN;
            }

            // Creazione di un'istanza dalla carta fornita
            Instance instance = new DenseInstance(1.0, attributes);

            // Crea un dataset temporaneo per associare l'istanza
            ArrayList<Attribute> tempAttributes = new ArrayList<>();
            tempAttributes.add(new Attribute("ccs_average_price"));
            tempAttributes.add(new Attribute("shift"));
            tempAttributes.add(new Attribute("shift_3"));
            tempAttributes.add(new Attribute("shift_7"));
            tempAttributes.add(new Attribute("shift_21"));
            tempAttributes.add(new Attribute("shift_30"));
            tempAttributes.add(new Attribute("shift_90"));
            tempAttributes.add(new Attribute("shift_180"));
            tempAttributes.add(new Attribute("shift_365"));

            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("prezzo aumenta");
            classValues.add("prezzo diminuisce");
            tempAttributes.add(new Attribute("class", classValues)); // Classe nominale

            Instances tempDataset = new Instances("CardClassification", tempAttributes, 0);
            tempDataset.setClassIndex(tempDataset.numAttributes() - 1); // Imposta l'ultima colonna come target
            instance.setDataset(tempDataset); // Associa l'istanza al dataset

            // Predizione
            double classIndex = model.classifyInstance(instance);
            return instance.classAttribute().value((int) classIndex); // Restituisce la classe predetta come stringa
        } catch (Exception e) {
            System.err.println("Errore durante la classificazione della carta: " + e.getMessage());
            e.printStackTrace();
            return null; // Valore di fallback in caso di errore
        }
    }

    public double classifyInstance(Instance instance) throws Exception {
        if (model == null) {
            throw new IllegalStateException("Il modello non è stato caricato o addestrato.");
        }
        return model.classifyInstance(instance);
    }
}