package com.progettoicon.ReteNeurale;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MainNeuralNetwork {

    private MultiLayerNetwork model;

    // Costruttore per creare un nuovo modello
    public MainNeuralNetwork(int inputSize) {
        if (inputSize <= 0) {
            throw new IllegalArgumentException("Il numero di input (inputSize) deve essere maggiore di 0.");
        }

        // Configurazione della rete
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.001)) // Ottimizzatore Adam
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputSize)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder()
                        .nIn(32)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .build();

        this.model = new MultiLayerNetwork(config);
        this.model.init();
    }

    // Getter per il modello
    public MultiLayerNetwork getModel() {
        return model;
    }

    // Salvataggio del modello su file
    public void saveModel(String path) throws IOException {
        model.save(new File(path), true);
    }

    // Metodo statico per caricare un modello salvato
    public static MainNeuralNetwork loadModel(String path) throws IOException {
        MultiLayerNetwork loadedModel = MultiLayerNetwork.load(new File(path), true);
        MainNeuralNetwork mainNeuralNetwork = new MainNeuralNetwork(1); // Valore placeholder ignorato
        mainNeuralNetwork.model = loadedModel; // Assegna il modello caricato
        return mainNeuralNetwork; // Ritorna l'istanza con il modello caricato
    }

    // Metodo per effettuare previsioni con il modello
    public double predict(double[] input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("L'input non può essere nullo o vuoto.");
        }

        INDArray inputArray = Nd4j.create(new double[][] { input }); // Trasforma in un array 2D
        INDArray output = model.output(inputArray);
        return output.getDouble(0);
    }
}