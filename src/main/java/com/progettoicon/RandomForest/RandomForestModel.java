package com.progettoicon.RandomForest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class RandomForestModel {
    private static final String MODEL_FILE = "/Users/gabrielemarzano/Documents/Programmi/yugioh/src/main/java/com/progettoicon/File/random_forest_model_weka.model";
    private RandomForest model;

    // Costruttore: Carica il modello se esiste
    public RandomForestModel() {
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
            System.out.println("Modello Random Forest addestrato con successo.");
        } catch (Exception e) {
            System.err.println("Errore durante l'addestramento del modello: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Predice il valore target per un'istanza
    // Modifica del metodo predict per accettare una singola istanza
    public double predict(Instance instance) {
        if (model == null) {
            throw new IllegalStateException("Il modello non è stato caricato o addestrato.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("L'istanza di input non può essere nulla.");
        }

        try {
            return model.classifyInstance(instance);
        } catch (Exception e) {
            System.err.println("Errore durante la previsione: " + e.getMessage());
            e.printStackTrace();
            return -1; // Valore di fallback in caso di errore
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
}