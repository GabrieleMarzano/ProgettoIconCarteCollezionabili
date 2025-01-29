package com.progettoicon.KnowledgeGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DeckSearch {

    // Rappresentazione del grafo come Map
    private Map<String, List<Edge>> graph = new HashMap<>();

    // Classe per rappresentare un arco con compatibilità
    public static class Edge {
        String targetNode;
        double compatibility;

        public Edge(String targetNode, double compatibility) {
            this.targetNode = targetNode;
            this.compatibility = compatibility;
        }
    }

    // Metodo per aggiungere un nodo e arco nel grafo
    public void addEdge(String source, String target, double compatibility) {
        graph.putIfAbsent(source, new ArrayList<>());
        graph.get(source).add(new Edge(target, compatibility));
    }

    // Metodo Breadth-First Search (BFS)
    public List<String> findDeckBFS(String startNode, double threshold, int maxCards) {
        List<String> deck = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty() && deck.size() < maxCards) {
            String current = queue.poll();
            deck.add(current);

            for (Edge edge : graph.getOrDefault(current, new ArrayList<>())) {
                if (edge.compatibility >= threshold && !visited.contains(edge.targetNode)) {
                    queue.add(edge.targetNode);
                    visited.add(edge.targetNode);
                }
            }
        }
        return deck;
    }

    // Metodo Depth-First Search (DFS) con approccio greedy
    public List<String> findDeckDFS(String startNode, double threshold, int maxCards) {
        List<String> deck = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfsHelper(startNode, threshold, maxCards, visited, deck);
        return deck;
    }

    private void dfsHelper(String currentNode, double threshold, int maxCards, Set<String> visited, List<String> deck) {
        if (deck.size() >= maxCards || visited.contains(currentNode)) {
            return;
        }

        visited.add(currentNode);
        deck.add(currentNode);

        // Ordina i vicini in ordine decrescente di compatibilità (greedy)
        List<Edge> neighbors = graph.getOrDefault(currentNode, new ArrayList<>());
        neighbors.sort((a, b) -> Double.compare(b.compatibility, a.compatibility));

        for (Edge edge : neighbors) {
            if (edge.compatibility >= threshold) {
                dfsHelper(edge.targetNode, threshold, maxCards, visited, deck);
            }
        }
    }

    // Esempio di utilizzo

}