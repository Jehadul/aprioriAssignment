package apriori;

import java.io.*;
import java.util.*;

public class AprioriExample {

    private static final double MIN_SUPPORT = 0.3;     // 30%
    private static final double MIN_CONFIDENCE = 0.5;   // 50%

    public static void main(String[] args) {
        List<Set<String>> transactions = readTransactions("src/apriori/transactions.txt");
        List<List<Set<String>>> allFrequentItemsets = new ArrayList<>();

        // Generate frequent 1-itemsets
        List<Set<String>> frequentItemsets = findFrequentOneItemsets(transactions, MIN_SUPPORT);
        allFrequentItemsets.add(frequentItemsets);
        System.out.println("Frequent 1-itemsets: " + frequentItemsets);

        int k = 2;
        while (!frequentItemsets.isEmpty()) {
            List<Set<String>> candidates = generateCandidates(frequentItemsets, k);
            frequentItemsets = filterFrequentItemsets(candidates, transactions, MIN_SUPPORT);
            if (!frequentItemsets.isEmpty()) {
                System.out.println("Frequent " + k + "-itemsets: " + frequentItemsets);
                allFrequentItemsets.add(frequentItemsets);
            }
            k++;
        }

        generateAssociationRules(allFrequentItemsets, transactions, MIN_CONFIDENCE);
    }

    // ------------------------------------------------------
    // Step 1: Read transactions from file
    // ------------------------------------------------------
    private static List<Set<String>> readTransactions(String filename) {
        List<Set<String>> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] items = line.trim().split(",");
                    Set<String> transaction = new HashSet<>();
                    for (String item : items) {
                        transaction.add(item.trim().toLowerCase());
                    }
                    transactions.add(transaction);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // ------------------------------------------------------
    // Step 2: Find frequent 1-itemsets
    // ------------------------------------------------------
    private static List<Set<String>> findFrequentOneItemsets(List<Set<String>> transactions, double minSupport) {
        Map<String, Integer> itemCount = new HashMap<>();

        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
            }
        }

        List<Set<String>> frequentItemsets = new ArrayList<>();
        int totalTransactions = transactions.size();

        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            if (support >= minSupport) {
                Set<String> itemset = new HashSet<>();
                itemset.add(entry.getKey());
                frequentItemsets.add(itemset);
            }
        }

        return frequentItemsets;
    }

    // ------------------------------------------------------
    // Step 3: Generate candidate k-itemsets
    // ------------------------------------------------------
    private static List<Set<String>> generateCandidates(List<Set<String>> prevFrequentItemsets, int k) {
        List<Set<String>> candidates = new ArrayList<>();

        for (int i = 0; i < prevFrequentItemsets.size(); i++) {
            for (int j = i + 1; j < prevFrequentItemsets.size(); j++) {
                Set<String> candidate = new HashSet<>(prevFrequentItemsets.get(i));
                candidate.addAll(prevFrequentItemsets.get(j));

                if (candidate.size() == k && !candidates.contains(candidate)) {
                    candidates.add(candidate);
                }
            }
        }

        return candidates;
    }

    // ------------------------------------------------------
    // Step 4: Filter frequent itemsets by support
    // ------------------------------------------------------
    private static List<Set<String>> filterFrequentItemsets(List<Set<String>> candidates,
                                                           List<Set<String>> transactions,
                                                           double minSupport) {
        List<Set<String>> frequentItemsets = new ArrayList<>();
        int totalTransactions = transactions.size();

        for (Set<String> candidate : candidates) {
            int count = 0;
            for (Set<String> transaction : transactions) {
                if (transaction.containsAll(candidate)) {
                    count++;
                }
            }

            double support = (double) count / totalTransactions;
            if (support >= minSupport) {
                frequentItemsets.add(candidate);
            }
        }

        return frequentItemsets;
    }

    // ------------------------------------------------------
    // Step 5: Generate association rules
    // ------------------------------------------------------
    private static void generateAssociationRules(List<List<Set<String>>> allFrequentItemsets,
                                                 List<Set<String>> transactions,
                                                 double minConfidence) {
        System.out.println("\n--- Association Rules ---");

        for (int k = 1; k < allFrequentItemsets.size(); k++) {
            for (Set<String> itemset : allFrequentItemsets.get(k)) {
                if (itemset.size() < 2) continue;

                List<Set<String>> subsets = getSubsets(itemset);

                for (Set<String> antecedent : subsets) {
                    if (antecedent.isEmpty() || antecedent.size() == itemset.size()) continue;

                    Set<String> consequent = new HashSet<>(itemset);
                    consequent.removeAll(antecedent);

                    double supportItemset = getSupport(itemset, transactions);
                    double supportAntecedent = getSupport(antecedent, transactions);
                    double confidence = supportItemset / supportAntecedent;

                    if (confidence >= minConfidence) {
                        System.out.printf("%s => %s (support: %.2f, confidence: %.2f)%n",
                                antecedent, consequent, supportItemset, confidence);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------
    // Helper: Generate all non-empty subsets of a set
    // ------------------------------------------------------
    private static List<Set<String>> getSubsets(Set<String> set) {
        List<Set<String>> subsets = new ArrayList<>();
        List<String> list = new ArrayList<>(set);
        int n = list.size();

        for (int i = 1; i < (1 << n); i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) subset.add(list.get(j));
            }
            subsets.add(subset);
        }

        return subsets;
    }

    // ------------------------------------------------------
    // Helper: Get support of an itemset
    // ------------------------------------------------------
    private static double getSupport(Set<String> itemset, List<Set<String>> transactions) {
        int count = 0;
        for (Set<String> t : transactions) {
            if (t.containsAll(itemset)) count++;
        }
        return (double) count / transactions.size();
    }
}
