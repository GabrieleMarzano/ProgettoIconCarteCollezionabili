package com.progettoicon.KnowledgeGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GraphVisualizerFX extends Application {

    private static Graph<Integer, DefaultWeightedEdge> staticGraph; // Variabile statica per il grafo
    private Graph<Integer, DefaultWeightedEdge> graph; // Istanza locale del grafo

    // Metodo statico per impostare il grafo
    public static void setGraph(Graph<Integer, DefaultWeightedEdge> graph) {
        staticGraph = graph;
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.graph = filterTriangles(staticGraph); // Filtra il grafo per visualizzare solo triangoli
    }

    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();
        Map<Integer, Circle> vertexMap = new HashMap<>();

        double width = 1200; // Larghezza della finestra
        double height = 800; // Altezza della finestra

        // Trova le componenti connesse
        List<Set<Integer>> components = new ConnectivityInspector<>(graph).connectedSets();
        double componentRadius = Math.min(width, height) * 0.9; // Aumenta il raggio del layout per ogni componente
        double centerX = width / 2;
        double centerY = height / 2;

        double angleStep = 2 * Math.PI / components.size();
        int componentIndex = 0;

        for (Set<Integer> component : components) {
            // Posizione centrale per questa componente
            double componentCenterX = centerX + componentRadius * Math.cos(angleStep * componentIndex);
            double componentCenterY = centerY + componentRadius * Math.sin(angleStep * componentIndex);

            // Posiziona i nodi della componente in un cerchio
            double nodeRadius = componentRadius; // Aumenta il raggio per distanziare i nodi
            int nodeIndex = 0;
            for (Integer vertex : component) {
                double angle = 2 * Math.PI * nodeIndex / component.size();
                double x = componentCenterX + nodeRadius * Math.cos(angle);
                double y = componentCenterY + nodeRadius * Math.sin(angle);

                // Creazione del nodo
                Circle circle = new Circle(x, y, 10, Color.LIGHTBLUE);
                Text text = new Text(x - 10, y - 15, String.valueOf(vertex)); // Aggiungi il testo con l'ID del nodo
                text.setFill(Color.BLACK); // Colore del testo
                text.setStyle("-fx-font-size: 10px;"); // Dimensione del font

                pane.getChildren().addAll(circle, text); // Aggiungi cerchio e testo al layout
                vertexMap.put(vertex, circle);
                nodeIndex++;
            }
            componentIndex++;
        }

        // Disegna gli archi
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Integer source = graph.getEdgeSource(edge);
            Integer target = graph.getEdgeTarget(edge);
            Circle sourceCircle = vertexMap.get(source);
            Circle targetCircle = vertexMap.get(target);

            if (sourceCircle != null && targetCircle != null) {
                Line line = new Line(
                        sourceCircle.getCenterX(), sourceCircle.getCenterY(),
                        targetCircle.getCenterX(), targetCircle.getCenterY());
                line.setStrokeWidth(1);
                line.setStroke(Color.GRAY);

                pane.getChildren().add(line); // Aggiungi la linea
            }
        }

        // Configura la scena con zoom
        Scene scene = new Scene(pane, width, height, Color.WHITE);
        pane.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.05;
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor;
            }
            pane.setScaleX(pane.getScaleX() * zoomFactor);
            pane.setScaleY(pane.getScaleY() * zoomFactor);
        });

        stage.setScene(scene);
        stage.setTitle("Card Graph Visualizer - Connected Components with IDs");
        stage.show();
    }

    /**
     * Filtra il grafo per mantenere solo i nodi e gli archi che formano triangoli.
     *
     * @param graph Il grafo originale.
     * @return Un nuovo grafo contenente solo i nodi e gli archi che formano
     *         triangoli.
     */
    public static Graph<Integer, DefaultWeightedEdge> filterTriangles(Graph<Integer, DefaultWeightedEdge> graph) {
        Graph<Integer, DefaultWeightedEdge> triangleGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Integer nodeA : graph.vertexSet()) {
            Set<Integer> neighborsA = getNeighbors(graph, nodeA);

            for (Integer nodeB : neighborsA) {
                if (nodeB.equals(nodeA))
                    continue;

                Set<Integer> neighborsB = getNeighbors(graph, nodeB);

                for (Integer nodeC : neighborsA) {
                    if (neighborsB.contains(nodeC) && !nodeC.equals(nodeA) && !nodeC.equals(nodeB)) {
                        triangleGraph.addVertex(nodeA);
                        triangleGraph.addVertex(nodeB);
                        triangleGraph.addVertex(nodeC);

                        addEdgeWithWeight(graph, triangleGraph, nodeA, nodeB);
                        addEdgeWithWeight(graph, triangleGraph, nodeB, nodeC);
                        addEdgeWithWeight(graph, triangleGraph, nodeA, nodeC);
                    }
                }
            }
        }

        return triangleGraph;
    }

    private static Set<Integer> getNeighbors(Graph<Integer, DefaultWeightedEdge> graph, Integer node) {
        Set<Integer> neighbors = new HashSet<>();
        for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
            neighbors.add(
                    graph.getEdgeSource(edge).equals(node) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge));
        }
        return neighbors;
    }

    private static void addEdgeWithWeight(Graph<Integer, DefaultWeightedEdge> originalGraph,
            Graph<Integer, DefaultWeightedEdge> targetGraph,
            Integer nodeA, Integer nodeB) {
        if (!targetGraph.containsEdge(nodeA, nodeB)) {
            DefaultWeightedEdge edge = targetGraph.addEdge(nodeA, nodeB);
            if (edge != null) {
                targetGraph.setEdgeWeight(edge, originalGraph.getEdgeWeight(originalGraph.getEdge(nodeA, nodeB)));
            }
        }
    }

    public static Graph<Integer, DefaultWeightedEdge> filterTrianglesAndSquares(
            Graph<Integer, DefaultWeightedEdge> graph) {
        Graph<Integer, DefaultWeightedEdge> filteredGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Integer nodeA : graph.vertexSet()) {
            Set<Integer> neighborsA = getNeighbors(graph, nodeA);

            for (Integer nodeB : neighborsA) {
                if (nodeB.equals(nodeA))
                    continue;

                Set<Integer> neighborsB = getNeighbors(graph, nodeB);

                // Cerca triangoli
                for (Integer nodeC : neighborsA) {
                    if (neighborsB.contains(nodeC) && !nodeC.equals(nodeA) && !nodeC.equals(nodeB)) {
                        filteredGraph.addVertex(nodeA);
                        filteredGraph.addVertex(nodeB);
                        filteredGraph.addVertex(nodeC);

                        addEdgeWithWeight(graph, filteredGraph, nodeA, nodeB);
                        addEdgeWithWeight(graph, filteredGraph, nodeB, nodeC);
                        addEdgeWithWeight(graph, filteredGraph, nodeA, nodeC);
                    }
                }

                // Cerca quadrati
                for (Integer nodeC : neighborsB) {
                    if (nodeC.equals(nodeA) || neighborsA.contains(nodeC))
                        continue;

                    Set<Integer> neighborsC = getNeighbors(graph, nodeC);
                    for (Integer nodeD : neighborsC) {
                        if (nodeD.equals(nodeA) || nodeD.equals(nodeB) || nodeD.equals(nodeC))
                            continue;

                        if (neighborsA.contains(nodeD)) {
                            filteredGraph.addVertex(nodeA);
                            filteredGraph.addVertex(nodeB);
                            filteredGraph.addVertex(nodeC);
                            filteredGraph.addVertex(nodeD);

                            addEdgeWithWeight(graph, filteredGraph, nodeA, nodeB);
                            addEdgeWithWeight(graph, filteredGraph, nodeB, nodeC);
                            addEdgeWithWeight(graph, filteredGraph, nodeC, nodeD);
                            addEdgeWithWeight(graph, filteredGraph, nodeD, nodeA);
                        }
                    }
                }
            }
        }

        return filteredGraph;
    }

}