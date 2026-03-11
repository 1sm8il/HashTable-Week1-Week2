import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

class Transaction {
    String id;
    double amount;
    String merchant;
    String accountId;
    long timestamp;

    public Transaction(String id, double amount, String merchant, String accountId, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.accountId = accountId;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("{id: %s, amount: %.2f, merchant: %s}", id, amount, merchant);
    }
}

public class TransactionAnalyzer {
    private List<Transaction> transactions;
    private HashMap<Double, List<Transaction>> amountIndex;

    public TransactionAnalyzer() {
        this.transactions = new ArrayList<>();
        this.amountIndex = new HashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        amountIndex.computeIfAbsent(transaction.amount, k -> new ArrayList<>()).add(transaction);
    }

    public List<List<Transaction>> findTwoSum(double target) {
        List<List<Transaction>> pairs = new ArrayList<>();
        HashMap<Double, Transaction> complementMap = new HashMap<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;

            if (complementMap.containsKey(complement)) {
                List<Transaction> pair = new ArrayList<>();
                pair.add(complementMap.get(complement));
                pair.add(t);
                pairs.add(pair);
            }

            complementMap.put(t.amount, t);
        }

        return pairs;
    }

    public List<List<Transaction>> findTwoSumWithTimeWindow(double target, long windowMs) {
        List<List<Transaction>> pairs = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t1 = transactions.get(i);
                Transaction t2 = transactions.get(j);

                long timeDiff = Math.abs(t2.timestamp - t1.timestamp);

                if (timeDiff <= windowMs && (t1.amount + t2.amount) == target) {
                    List<Transaction> pair = new ArrayList<>();
                    pair.add(t1);
                    pair.add(t2);
                    pairs.add(pair);
                }
            }
        }

        return pairs;
    }

    public List<Set<Transaction>> detectDuplicates() {
        List<Set<Transaction>> duplicates = new ArrayList<>();
        HashMap<String, List<Transaction>> merchantAmountMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            merchantAmountMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (List<Transaction> group : merchantAmountMap.values()) {
            if (group.size() > 1) {
                // Check if different accounts
                Set<String> accounts = new HashSet<>();
                for (Transaction t : group) {
                    accounts.add(t.accountId);
                }

                if (accounts.size() > 1) {
                    duplicates.add(new HashSet<>(group));
                }
            }
        }

        return duplicates;
    }

    public List<List<Transaction>> findKSum(int k, double target) {
        List<List<Transaction>> results = new ArrayList<>();
        findKSumHelper(k, target, 0, new ArrayList<>(), results);
        return results;
    }

    private void findKSumHelper(int k, double target, int start,
                                List<Transaction> current, List<List<Transaction>> results) {
        if (k == 2) {
            // Two sum
            HashMap<Double, Transaction> map = new HashMap<>();
            for (int i = start; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                double complement = target - t.amount;

                if (map.containsKey(complement)) {
                    List<Transaction> pair = new ArrayList<>(current);
                    pair.add(map.get(complement));
                    pair.add(t);
                    results.add(pair);
                }

                map.put(t.amount, t);
            }
        } else {
            for (int i = start; i < transactions.size(); i++) {
                current.add(transactions.get(i));
                findKSumHelper(k - 1, target - transactions.get(i).amount,
                        i + 1, current, results);
                current.remove(current.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        // Add sample transactions
        long baseTime = System.currentTimeMillis();
        analyzer.addTransaction(new Transaction("1", 500, "Store A", "acc1", baseTime));
        analyzer.addTransaction(new Transaction("2", 300, "Store B", "acc2", baseTime + 900000)); // 15 min later
        analyzer.addTransaction(new Transaction("3", 200, "Store C", "acc3", baseTime + 1800000)); // 30 min later
        analyzer.addTransaction(new Transaction("4", 500, "Store A", "acc4", baseTime + 3600000)); // 1 hour later
        analyzer.addTransaction(new Transaction("5", 700, "Store D", "acc5", baseTime + 7200000)); // 2 hours later
        analyzer.addTransaction(new Transaction("6", 300, "Store B", "acc6", baseTime + 7200000)); // 2 hours later

        System.out.println("=== Transaction Analysis ===\n");

        // Find Two Sum
        System.out.println("Find Two Sum (target = 500):");
        List<List<Transaction>> pairs = analyzer.findTwoSum(500);
        for (List<Transaction> pair : pairs) {
            System.out.println(pair.get(0) + " + " + pair.get(1) + " = 500");
        }

        // Find Two Sum with time window
        System.out.println("\nFind Two Sum with 1-hour window (target = 500):");
        pairs = analyzer.findTwoSumWithTimeWindow(500, 3600000); // 1 hour
        for (List<Transaction> pair : pairs) {
            System.out.println(pair.get(0) + " + " + pair.get(1) + " = 500");
        }

        // Detect duplicates
        System.out.println("\nDetect Duplicates (same amount, same merchant, different accounts):");
        List<Set<Transaction>> duplicates = analyzer.detectDuplicates();
        for (Set<Transaction> dup : duplicates) {
            System.out.println("Duplicate group: " + dup);
        }

        // Find K-Sum
        System.out.println("\nFind 3-Sum (target = 1000):");
        List<List<Transaction>> kSums = analyzer.findKSum(3, 1000);
        for (List<Transaction> group : kSums) {
            System.out.println(group);
        }
    }
}