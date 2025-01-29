package com.progettoicon.KnowledgeGraph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class CardGraph {

    private Graph<Integer, DefaultWeightedEdge> graph;

    public CardGraph() {
        // Creazione del grafo con nodi unici (ID delle carte) e archi pesati
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    // Metodo per verificare se il grafo esiste o crearlo
    public void ensureGraphExists(Connection conn) {
        if (graph == null || graph.vertexSet().isEmpty()) {
            System.out.println("Grafo non trovato. Creazione in corso...");
            populateGraph(conn);
        } else {
            System.out.println("Grafo già esistente con " + graph.vertexSet().size() + " nodi e " +
                    graph.edgeSet().size() + " archi.");
        }
    }

    // Metodo per popolare il grafo
    public void populateGraph(Connection conn) {
        // String query = """
        // SELECT * FROM card_compatibility
        // WHERE compatibily > 0.7 limit 3000 offset 10000
        // """;

        String query = """
                SELECT id_card1, id_card2, compatibily FROM card_compatibility join sys.carte  on ( carte.id = card_compatibility.id_card1 or carte.id = card_compatibility.id_card2) where archetype = "blue-eyes"  and compatibily > 0.7 ;

                                  """;

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int card1Id = rs.getInt("id_card1");
                int card2Id = rs.getInt("id_card2");
                double compatibilityValue = rs.getDouble("compatibily");

                // Aggiungi i nodi al grafo
                graph.addVertex(card1Id);
                graph.addVertex(card2Id);

                // Aggiungi un arco con peso
                DefaultWeightedEdge edge = graph.addEdge(card1Id, card2Id);
                if (edge != null) {
                    graph.setEdgeWeight(edge, compatibilityValue);
                }
            }
            System.out.println("Grafo popolato con successo.");
        } catch (Exception e) {
            System.err.println("Errore durante la creazione del grafo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Graph<Integer, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    // Stampa il grafo
    public void printGraph() {
        System.out.println("=== Nodi del Grafo (ID delle carte) ===");
        graph.vertexSet().forEach(id -> System.out.println("ID Carta: " + id));

        System.out.println("=== Archi del Grafo ===");
        graph.edgeSet().forEach(edge -> {
            Integer source = graph.getEdgeSource(edge);
            Integer target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);
            System.out.printf("Arco: %d -> %d (Peso: %.2f)%n", source, target, weight);
        });
    }

    public void setGraph(Graph<Integer, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    // Filtra il grafo per triangoli
    public Graph<Integer, DefaultWeightedEdge> filterTriangles() {
        Graph<Integer, DefaultWeightedEdge> triangleGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Integer nodeA : graph.vertexSet()) {
            Set<Integer> neighborsA = getNeighbors(nodeA);

            for (Integer nodeB : neighborsA) {
                if (nodeB.equals(nodeA))
                    continue;

                Set<Integer> neighborsB = getNeighbors(nodeB);

                for (Integer nodeC : neighborsA) {
                    if (neighborsB.contains(nodeC) && !nodeC.equals(nodeA) && !nodeC.equals(nodeB)) {
                        triangleGraph.addVertex(nodeA);
                        triangleGraph.addVertex(nodeB);
                        triangleGraph.addVertex(nodeC);

                        addEdgeWithWeight(nodeA, nodeB, triangleGraph);
                        addEdgeWithWeight(nodeB, nodeC, triangleGraph);
                        addEdgeWithWeight(nodeA, nodeC, triangleGraph);
                    }
                }
            }
        }

        return triangleGraph;
    }

    // Filtra il grafo per triangoli e quadrati
    public Graph<Integer, DefaultWeightedEdge> filterTrianglesAndSquares() {
        Graph<Integer, DefaultWeightedEdge> filteredGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Integer nodeA : graph.vertexSet()) {
            Set<Integer> neighborsA = getNeighbors(nodeA);

            for (Integer nodeB : neighborsA) {
                if (nodeB.equals(nodeA))
                    continue;

                Set<Integer> neighborsB = getNeighbors(nodeB);

                // Cerca triangoli
                for (Integer nodeC : neighborsA) {
                    if (neighborsB.contains(nodeC) && !nodeC.equals(nodeA) && !nodeC.equals(nodeB)) {
                        filteredGraph.addVertex(nodeA);
                        filteredGraph.addVertex(nodeB);
                        filteredGraph.addVertex(nodeC);

                        addEdgeWithWeight(nodeA, nodeB, filteredGraph);
                        addEdgeWithWeight(nodeB, nodeC, filteredGraph);
                        addEdgeWithWeight(nodeA, nodeC, filteredGraph);
                    }
                }

                // Cerca quadrati
                for (Integer nodeC : neighborsB) {
                    if (nodeC.equals(nodeA) || neighborsA.contains(nodeC))
                        continue;

                    Set<Integer> neighborsC = getNeighbors(nodeC);
                    for (Integer nodeD : neighborsC) {
                        if (nodeD.equals(nodeA) || nodeD.equals(nodeB) || nodeD.equals(nodeC))
                            continue;

                        if (neighborsA.contains(nodeD)) {
                            filteredGraph.addVertex(nodeA);
                            filteredGraph.addVertex(nodeB);
                            filteredGraph.addVertex(nodeC);
                            filteredGraph.addVertex(nodeD);

                            addEdgeWithWeight(nodeA, nodeB, filteredGraph);
                            addEdgeWithWeight(nodeB, nodeC, filteredGraph);
                            addEdgeWithWeight(nodeC, nodeD, filteredGraph);
                            addEdgeWithWeight(nodeD, nodeA, filteredGraph);
                        }
                    }
                }
            }
        }

        return filteredGraph;
    }

    private Set<Integer> getNeighbors(Integer node) {
        Set<Integer> neighbors = new HashSet<>();
        for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
            neighbors.add(
                    graph.getEdgeSource(edge).equals(node) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge));
        }
        return neighbors;
    }

    private void addEdgeWithWeight(Integer nodeA, Integer nodeB, Graph<Integer, DefaultWeightedEdge> targetGraph) {
        if (!targetGraph.containsEdge(nodeA, nodeB)) {
            DefaultWeightedEdge edge = targetGraph.addEdge(nodeA, nodeB);
            if (edge != null) {
                targetGraph.setEdgeWeight(edge, graph.getEdgeWeight(graph.getEdge(nodeA, nodeB)));
            }
        }
    }

    // Trova le componenti connesse nel grafo
    public List<Set<Integer>> findConnectedComponents() {
        ConnectivityInspector<Integer, DefaultWeightedEdge> inspector = new ConnectivityInspector<>(graph);
        return inspector.connectedSets();
    }

    public Graph<Integer, DefaultWeightedEdge> createSubgraphFromComponent(Set<Integer> component) {
        Graph<Integer, DefaultWeightedEdge> subgraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Aggiungi tutti i nodi della componente al sottografo
        for (Integer node : component) {
            subgraph.addVertex(node);
        }

        // Aggiungi gli archi corrispondenti tra i nodi della componente
        for (Integer node : component) {
            for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
                Integer source = graph.getEdgeSource(edge);
                Integer target = graph.getEdgeTarget(edge);

                if (component.contains(source) && component.contains(target)) {
                    if (!subgraph.containsEdge(source, target)) {
                        DefaultWeightedEdge newEdge = subgraph.addEdge(source, target);
                        if (newEdge != null) {
                            subgraph.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));
                        }
                    }
                }
            }
        }
        return subgraph;
    }

    // Trova i cliques nel grafo
    // public Set<Set<Integer>> findCliques() {
    // BronKerboschCliqueFinder<Integer, DefaultWeightedEdge> cliqueFinder = new
    // BronKerboschCliqueFinder<>(graph);
    // return cliqueFinder.getAllMaximalCliques();
    // }
}
