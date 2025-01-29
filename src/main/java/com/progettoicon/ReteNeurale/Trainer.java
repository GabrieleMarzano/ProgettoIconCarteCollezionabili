package com.progettoicon.ReteNeurale;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import com.progettoicon.Controll;

public class Trainer {

    private MainNeuralNetwork network;

    public Trainer(MainNeuralNetwork network) {
        this.network = network;
    }

    public void train(List<double[]> inputs, List<Double> targets, int epochs) {
        if (inputs.isEmpty() || targets.isEmpty() || inputs.size() != targets.size()) {
            throw new IllegalArgumentException(
                    "Gli input e i target devono avere la stessa dimensione e non possono essere vuoti.");
        }

        // Numero di esempi e dimensione di ogni input
        int numExamples = inputs.size();
        int inputSize = inputs.get(0).length;

        // Crea un array bidimensionale per gli input
        INDArray inputArray = Nd4j.create(numExamples, inputSize);
        for (int i = 0; i < numExamples; i++) {
            inputArray.putRow(i, Nd4j.create(inputs.get(i))); // Inserisci ogni input come una riga
        }

        // Crea un array bidimensionale per i target
        INDArray targetArray = Nd4j.create(numExamples, 1);
        for (int i = 0; i < numExamples; i++) {
            targetArray.putScalar(i, targets.get(i)); // Inserisci ogni target
        }

        // Crea un DataSet
        DataSet dataSet = new DataSet(inputArray, targetArray);

        // Crea un iteratore per suddividere il dataset in batch
        DataSetIterator iterator = new ListDataSetIterator<>(dataSet.asList(), 32); // Batch size di 32

        // Addestramento
        for (int epoch = 0; epoch < epochs; epoch++) {
            network.getModel().fit(iterator);
            System.out.println("Epoch " + (epoch + 1) + " completato.");
        }
    }

    public void trainModel(MainNeuralNetwork network, Trainer trainer, int batchSize, int maxRecords,
            String tableName, Connection conn, String modelPath) throws IOException {

        int offset = 0; // Offset iniziale
        boolean hasMoreData;
        int totalProcessedRecords = 0; // Contatore per il totale dei record processati

        do {
            // Carica un batch di dati
            System.out.println("Caricamento batch con offset " + offset);
            List<VettoreCardCompatibility> listaCardCompatibility = Controll.creaListaValidationNeuralNetwork(
                    batchSize, offset, tableName, conn);
            hasMoreData = !listaCardCompatibility.isEmpty();

            if (hasMoreData) {
                List<double[]> inputs = new ArrayList<>();
                List<Double> target = new ArrayList<>();

                for (VettoreCardCompatibility vet : listaCardCompatibility) {
                    inputs.add(vet.getVet());
                    target.add(vet.getTarget());
                }

                // Addestra il modello
                System.out.println("Addestramento del modello in corso per il batch con offset " + offset);
                trainer.train(inputs, target, 10); // Addestramento su 8 epoche per batch

                // Incrementa l'offset per il prossimo batch
                offset += batchSize;

                // Incrementa il totale dei record processati
                totalProcessedRecords += listaCardCompatibility.size();

                // Controlla se è stato raggiunto il limite massimo
                if (totalProcessedRecords >= maxRecords) {
                    System.out.println("Limite massimo di record processati raggiunto: " + totalProcessedRecords);
                    break;
                }
            }

        } while (hasMoreData);

        System.out.println("Addestramento completato.");
    }

}