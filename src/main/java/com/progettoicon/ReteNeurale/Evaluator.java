package com.progettoicon.ReteNeurale;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import com.progettoicon.Controll;

public class Evaluator {

    /**
     * Valuta il modello utilizzando i dati di validazione.
     *
     * @param network   Il modello di rete neurale addestrato.
     * @param batchSize La dimensione del batch per la valutazione.
     * @param conn      La connessione al database.
     */
    public void evaluate(MainNeuralNetwork network, int batchSize, Connection conn, int maxIterations,
            String nameTable) {

        boolean hasMoreData;
        double totalError = 0.0; // Errore totale
        int totalSamples = 0; // Numero totale di campioni
        int offset = 0;
        int iteration = 0; // Contatore delle iterazioni

        // Variabili per le categorie
        double lowError = 0.0;
        int lowSamples = 0;
        double mediumError = 0.0;
        int mediumSamples = 0;
        double highError = 0.0;
        int highSamples = 0;

        do {

            List<VettoreCardCompatibility> listaValidazione = Controll.creaListaValidationNeuralNetwork(batchSize,
                    offset, nameTable, conn);
            // Carica un batch di dati di validazione

            hasMoreData = !listaValidazione.isEmpty();

            if (hasMoreData) {
                for (VettoreCardCompatibility vet : listaValidazione) {

                    double[] input = vet.getVet();
                    double target = vet.getTarget();

                    // Predizione del modello
                    double prediction = network.predict(input);

                    // Calcola l'errore quadratico
                    totalError += Math.pow(prediction - target, 2);
                    totalSamples++;

                    // Classifica il target nelle categorie e aggiorna gli errori
                    if (target >= 0.0 && target <= 0.39) {
                        lowError += Math.pow(prediction - target, 2);
                        lowSamples++;
                    } else if (target >= 0.4 && target <= 0.69) {
                        mediumError += Math.pow(prediction - target, 2);
                        mediumSamples++;
                    } else if (target >= 0.7 && target <= 1.0) {
                        highError += Math.pow(prediction - target, 2);
                        highSamples++;
                    }

                    try (BufferedWriter writer = new BufferedWriter(
                            new FileWriter("/Users/gabrielemarzano/Desktop/ValutazioneModello2",
                                    true))) { // true per aggiungere senza sovrascrivere
                        writer.write("Compatibilita predetta " + prediction + "   target: " +
                                target);
                        writer.newLine();
                        writer.write("---------------------------------------");
                        writer.newLine();

                    } catch (IOException e) {
                        System.err.println("Errore durante la scrittura nel file: " +
                                e.getMessage());
                        e.printStackTrace();
                    }

                }

                offset += batchSize; // Incrementa l'offset
            }

            iteration++; // Incrementa il contatore delle iterazioni
            if (iteration >= maxIterations) {
                System.out.println("Numero massimo di iterazioni raggiunto: " + maxIterations);
                break;
            }

        } while (hasMoreData);

        // Calcola l'errore quadratico medio (MSE) complessivo
        double mse = totalError / totalSamples;

        // Calcola gli MSE per ogni categoria
        double lowMSE = lowSamples > 0 ? lowError / lowSamples : 0.0;
        double mediumMSE = mediumSamples > 0 ? mediumError / mediumSamples : 0.0;
        double highMSE = highSamples > 0 ? highError / highSamples : 0.0;

        System.out.println("Errore Quadratico Medio (MSE) complessivo: " + mse);
        System.out.println("Errore Quadratico Medio (MSE) categoria bassa: " + lowMSE);
        System.out.println("Errore Quadratico Medio (MSE) categoria media: " + mediumMSE);
        System.out.println("Errore Quadratico Medio (MSE) categoria alta: " + highMSE);
    }

}
