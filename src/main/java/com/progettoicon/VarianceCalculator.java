package com.progettoicon;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class VarianceCalculator {

    public static void calculateVariance(String filePath) {
        try {
            // Carica il dataset ARFF
            DataSource source = new DataSource(filePath);
            Instances dataset = source.getDataSet();

            System.out.println("Varianza per ciascun attributo numerico:");

            // Itera sugli attributi del dataset
            for (int i = 0; i < dataset.numAttributes(); i++) {
                Attribute attr = dataset.attribute(i);

                // Calcola la varianza solo per attributi numerici
                if (attr.isNumeric()) {
                    double mean = dataset.meanOrMode(attr);
                    double variance = 0.0;

                    for (int j = 0; j < dataset.numInstances(); j++) {
                        double value = dataset.instance(j).value(attr);
                        variance += Math.pow(value - mean, 2);
                    }

                    variance /= dataset.numInstances();
                    System.out.println(attr.name() + ": " + variance);
                }
            }

        } catch (Exception e) {
            System.err.println("Errore nel calcolo della varianza: " + e.getMessage());
        }
    }

    public static void calculateTargetVariance(String filePath) {
        try {
            // Carica il dataset ARFF
            DataSource source = new DataSource(filePath);
            Instances dataset = source.getDataSet();

            // Imposta l'attributo target (classe) come l'ultimo attributo
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }

            Attribute targetAttr = dataset.classAttribute();

            // Controlla se il target è numerico
            if (!targetAttr.isNumeric()) {
                throw new IllegalArgumentException("L'attributo target non è numerico.");
            }

            // Calcola la varianza del target
            double mean = dataset.meanOrMode(targetAttr);
            double variance = 0.0;

            for (int i = 0; i < dataset.numInstances(); i++) {
                double value = dataset.instance(i).value(targetAttr);
                variance += Math.pow(value - mean, 2);
            }

            variance /= dataset.numInstances();
            System.out.println("Varianza del target (" + targetAttr.name() + "): " + variance);

        } catch (Exception e) {
            System.err.println("Errore nel calcolo della varianza del target: " + e.getMessage());
        }
    }
}