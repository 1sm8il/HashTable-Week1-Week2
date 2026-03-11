import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class Document {
    String id;
    String content;

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }
}

public class PlagiarismDetector {
    private HashMap<String, Set<String>> nGramIndex;
    private HashMap<String, Document> documents;
    private int nGramSize;

    public PlagiarismDetector(int nGramSize) {
        this.nGramIndex = new HashMap<>();
        this.documents = new HashMap<>();
        this.nGramSize = nGramSize;
    }

    public void addDocument(String docId, String content) {
        documents.put(docId, new Document(docId, content));
        Set<String> nGrams = extractNGrams(content);

        for (String nGram : nGrams) {
            nGramIndex.computeIfAbsent(nGram, k -> new HashSet<>()).add(docId);
        }
    }

    private Set<String> extractNGrams(String text) {
        Set<String> nGrams = new HashSet<>();
        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - nGramSize; i++) {
            StringBuilder nGram = new StringBuilder();
            for (int j = 0; j < nGramSize; j++) {
                nGram.append(words[i + j]).append(" ");
            }
            nGrams.add(nGram.toString().trim());
        }

        return nGrams;
    }

    public void analyzeDocument(String docId) {
        Document targetDoc = documents.get(docId);
        if (targetDoc == null) {
            System.out.println("Document not found: " + docId);
            return;
        }

        Set<String> targetNGrams = extractNGrams(targetDoc.content);
        List<DocumentSimilarity> similarities = new ArrayList<>();

        for (Document otherDoc : documents.values()) {
            if (!otherDoc.id.equals(docId)) {
                Set<String> otherNGrams = extractNGrams(otherDoc.content);
                int matchingNGrams = 0;

                for (String nGram : targetNGrams) {
                    if (otherNGrams.contains(nGram)) {
                        matchingNGrams++;
                    }
                }

                double similarity = (matchingNGrams * 100.0) / targetNGrams.size();
                similarities.add(new DocumentSimilarity(otherDoc.id, matchingNGrams, similarity));
            }
        }

        // Sort by similarity
        Collections.sort(similarities, (a, b) -> Double.compare(b.similarity, a.similarity));

        System.out.println("Analysis for Document: " + docId);
        System.out.println("Extracted " + targetNGrams.size() + " n-grams");
        System.out.println();

        for (DocumentSimilarity sim : similarities) {
            String status = sim.similarity > 50 ? "PLAGIARISM DETECTED" :
                    sim.similarity > 15 ? "suspicious" : "normal";
            System.out.println("Found " + sim.matchingNGrams + " matching n-grams with \"" + sim.docId + "\"");
            System.out.println("Similarity: " + String.format("%.1f", sim.similarity) + "% (" + status + ")");
            System.out.println();
        }
    }

    private class DocumentSimilarity {
        String docId;
        int matchingNGrams;
        double similarity;

        public DocumentSimilarity(String docId, int matchingNGrams, double similarity) {
            this.docId = docId;
            this.matchingNGrams = matchingNGrams;
            this.similarity = similarity;
        }
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        // Add sample documents
        detector.addDocument("essay_089.txt",
                "The quick brown fox jumps over the lazy dog. This is a sample essay about animals.");

        detector.addDocument("essay_092.txt",
                "The quick brown fox jumps over the lazy dog. This essay is very similar to another one.");

        detector.addDocument("essay_123.txt",
                "The quick brown fox jumps over the lazy dog. This is a completely different essay.");

        detector.addDocument("essay_456.txt",
                "A completely different topic about space exploration and the universe.");

        // Analyze document
        detector.analyzeDocument("essay_123.txt");
    }
}