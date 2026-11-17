import java.io.*;
import java.util.*;

public class TextAnalysisApp {
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);

        System.out.println("======================================================================");
        System.out.println("TEXT ANALYSIS & SIMILARITY ENGINE");
        System.out.println("======================================================================");

        while (true) {

            System.out.println("1. Execute Comparative File Analysis");
            System.out.println("2. Terminate Application Session");
            System.out.print("Select an option (1-2): ");

            String choice = input.nextLine();

            try {

                if (choice.equals("1")) {

                    System.out.print("Enter path for the first text file: ");

                    String filename1 = input.nextLine();

                    System.out.print("Enter path for the second text file: ");

                    String filename2 = input.nextLine();

                    //Read both files
                    String text1 = FileHandler.readFile(filename1);

                    String text2 = FileHandler.readFile(filename2);

                    LiteraryAnalysis analysis1 = new LiteraryAnalysis(text1);

                    LiteraryAnalysis analysis2 = new LiteraryAnalysis(text2);

                    //Load dictionaries
                    analysis1.loadStopWords("stopwords.txt");
                    analysis1.loadSentimentWords("positive.txt", true);
                    analysis1.loadSentimentWords("negative.txt", false);

                    analysis2.loadStopWords("stopwords.txt");
                    analysis2.loadSentimentWords("positive.txt", true);
                    analysis2.loadSentimentWords("negative.txt", false);


                    analysis1.analyze();
                    analysis2.analyze();

                    //Display and save reports
                    System.out.println("--- ANALYSIS GRID SUMMARY: FILE 1 ---");

                    analysis1.displayResults();

                    analysis1.generateReport( new File(filename1).getName().substring(0, (new File(filename1)).getName().lastIndexOf('.')) + " Report.txt");

                    System.out.println("--- ANALYSIS GRID SUMMARY: FILE 2 ---");

                    analysis2.displayResults();
                    analysis2.generateReport( new File(filename2).getName().substring(0, (new File(filename2)).getName().lastIndexOf('.')) + " Report.txt");

                    // Calculate similarity
                    double similarity = SimilarityCalculator.calculateSimilarity(text1, text2);

                    System.out.println("======================================================================");

                    System.out.println("Computed Cosine Similarity Score: " + similarity);

                    if (similarity >= 0.75) {
                        System.out.println("Structural Evaluation : The texts are SIMILAR.");
                    } else {
                        System.out.println("Structural Evaluation : The texts are DIFFERENT.");
                    }

                    System.out.println("File Status : Reports saved to disk.");

                    System.out.println("======================================================================");

                } else if (choice.equals("2")) {

                    System.out.println("Exiting");

                    break;

                } else {

                    System.out.println("Invalid option. Please enter 1 or 2.");
                }

            } catch (IOException e) {

                System.out.println("File Error: Unable to locate or read the specified file.");

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());
            }
        }

        input.close();
    }


}

interface Analysis{
    public void analyze();
    public void generateReport(String filename) throws IOException;
    public void displayResults();
}

class FileHandler{
    //Returns String representing entire contents of text file
    public static String readFile(String filename) throws IOException{
        File file = new File(filename);
        StringBuilder fileText = new StringBuilder();
        try(Scanner input = new Scanner(file)) {

            while (input.hasNextLine()) {
                fileText.append(input.nextLine()).append("\n");
            }

        }
        return fileText.toString();
    }

}

class TextProcessor{

    //Takes String of text and splits it into tokens along spaces.
    public static ArrayList<String> tokenize(String text){

        String[] unfilteredWords = text.split("\\W+");

        ArrayList<String> words = new ArrayList<>();

        for(String s: unfilteredWords) {
            if(s.isBlank() || s.equals(" ")) {
                continue;
            } else{
                words.add((s.toLowerCase()).replaceAll("\\p{Punct}",""));
            }
        }

        return words;
    }

    //Combines two token lists into a non-duplicated vocabulary list
    public static ArrayList<String> buildVocabulary(ArrayList<String> words1, ArrayList<String> words2) {

        ArrayList<String> vocabulary = new ArrayList<>();

        boolean found;

        for (String s : words1) {
            found = false;
            for (String s2 : vocabulary) {
                if (s.equals(s2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                vocabulary.add(s);
            }
        }

        for (String s : words2) {
            found = false;
            for (String s2 : vocabulary) {
                if (s.equals(s2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                vocabulary.add(s);
            }
        }

        return vocabulary;
    }

    //For one list of words, counts frequencies against vocabulary and fill parallel frequency vector
    public static void fillVector (ArrayList<String> words, ArrayList<String> vocabulary, int[] vector){

        for(String s: words){
            for(int i = 0; i<vocabulary.size();i++){
                if (s.equals(vocabulary.get(i))) {
                    vector[i]++;
                }
            }
        }

    }

    public static double cosineSimilarity(int[] vec1, int[] vec2){
        if(vec1.length == 0 || vec2.length == 0 ||
                vec1.length != vec2.length){
            return 0.0;
        }

        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        for(int i = 0; i < vec1.length; i++){

            dotProduct += vec1[i] * vec2[i];

            magnitude1 += vec1[i] * vec1[i];

            magnitude2 += vec2[i] * vec2[i];
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if(magnitude1 == 0 || magnitude2 == 0){
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }
}


class WordCount{
    String word;
    int freq;
    public WordCount(String word, int count){
        this.word = word;
        this.freq = count;
    }
}

class SimilarityCalculator{
    public static double calculateSimilarity(String text1, String text2){
        ArrayList<String> words1 = TextProcessor.tokenize(text1);

        ArrayList<String> words2 = TextProcessor.tokenize(text2);

        ArrayList<String> vocabulary = TextProcessor.buildVocabulary(words1, words2);

        int[] vector1 = new int[vocabulary.size()];

        int[] vector2 = new int[vocabulary.size()];

        TextProcessor.fillVector(words1, vocabulary, vector1);

        TextProcessor.fillVector(words2, vocabulary, vector2);

        return TextProcessor.cosineSimilarity(vector1, vector2);

    }
}

class LiteraryAnalysis implements Analysis{
    //Metrics
    String sentiment;
    ArrayList<WordCount> wordFrequencies;
    int totalCharacters;
    int totalWords;
    double vocabularyRichness;
    String longestWord;
    int vowels;
    int consonants;
    int uniqueWords;
    String report;



    //Text
    String text;
    ArrayList<String> processedText;
    ArrayList<String> stopWords;
    ArrayList<String> positiveWords;
    ArrayList<String> negativeWords;



    public LiteraryAnalysis(String text){
        this.processedText = TextProcessor.tokenize(text);
        this.text = text;
        this.wordFrequencies = new ArrayList<>();
    }

    //Get list of stop words and load it into class
    public void loadStopWords(String filename) throws IOException{
        File file = new File(filename);
        StringBuilder fileText = new StringBuilder();

        try(Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                fileText.append(input.nextLine()).append("\n");
            }
        }

        this.stopWords = new ArrayList<>(List.of((fileText.toString()).toLowerCase().split("\\s+")));
    }

    //Given file path, get list of sentiment words, positive or negative sentiment must be given
    public void loadSentimentWords(String filename, boolean isPositive) throws IOException{
        File file = new File(filename);

        StringBuilder fileText = new StringBuilder();
        try(Scanner input = new Scanner(file)) {
            while(input.hasNextLine()) {
                fileText.append(input.nextLine()).append("\n");
            }
        }

        //Choose whether to assign to positive or negative
        if(isPositive) {
            this.positiveWords = new ArrayList<>(List.of((fileText.toString().toLowerCase().split("\\s+"))));
        } else {
            this.negativeWords = new ArrayList<>(List.of((fileText.toString().toLowerCase().split("\\s+"))));
        }
    }

    private void performBasicMetrics(){


        //Generate Frequency list
        totalWords = 0;
        totalCharacters = 0;
        wordFrequencies = new ArrayList<>();
        for (String string : processedText) {
            boolean stopWord = false;

            //Check to make sure word isn't a stop Word
            for (String word : stopWords) {
                if (string.equals(word)) {
                    stopWord = true;
                    break;
                }
            }

            //If it's not a stop word, either add it or increase existing frequency
            if (!stopWord) {

                totalWords++;

                totalCharacters += string.length();

                int index = findWordIndex(string);
                if (index != -1) {
                    wordFrequencies.get(index).freq++;
                } else {
                    wordFrequencies.add(new WordCount(string, 1));
                }
            }
        }

        sortWordFrequencies();

        //Get the sentiment report
        int sentimentCount = 0;
        for(String s: processedText){
            for(String s1: positiveWords){
                if(s.equals(s1)) sentimentCount ++;
            }

            for(String s1: negativeWords){
                if(s.equals(s1)) sentimentCount --;
            }
        }

        if(sentimentCount > 0){
            sentiment = "Positive";
        } else if (sentimentCount == 0){
            sentiment = "Neutral";
        } else {
            sentiment = "Negative";
        }

        //Get total filtered Characters, as well as longest word
        this.longestWord = "";

        this.uniqueWords = wordFrequencies.size();

        if(totalWords > 0){
            this.vocabularyRichness =
                    (double) uniqueWords / totalWords;
        } else {
            this.vocabularyRichness = 0.0;
        }

        for(WordCount w : wordFrequencies){

            if(w.word.length() > longestWord.length()){
                longestWord = w.word;
            }
        }

        //Go through entire text, counting vowels and consonants
        this.vowels = 0;
        this.consonants = 0;

        for(int i = 0; i < text.length(); i++){

            char c = Character.toLowerCase(text.charAt(i));

            if(c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u'){

                vowels++;

            } else if(Character.isLetter(c)){

                consonants++;
            }
        }




    }

    //Extracts the amount of sentences in the file by splitting along sentence terminating punctuation.
    private int extractSentencesCount(){

        String[] unfilteredSentences = text.split("[.!?]+");
        int count = 0;

        for (String unfilteredSentence : unfilteredSentences) {
            if (!(unfilteredSentence.isBlank())) count++;
        }

        return count;
    }

    //Extracts the amount of paragraphs.
    private int extractParagraphsCount(){
        String[] unfilteredParagraphs = text.split("\\n\\s*\\n");
        int count = 0;

        for (String unfilteredParagraph : unfilteredParagraphs) {
            if (!(unfilteredParagraph.isBlank())) count++;
        }

        return count;
    }

    private int findWordIndex(String word){
        for(int i = 0; i<wordFrequencies.size(); i++){
            if(word.equals(wordFrequencies.get(i).word)) return i;
        }
        return -1;
    }

    private void sortWordFrequencies(){

        for(int i = 0; i<wordFrequencies.size();i++){
            WordCount max = wordFrequencies.get(i);
            int maxIndex = i;
            for(int j = i + 1;j<wordFrequencies.size(); j++){
                if(wordFrequencies.get(j).freq > max.freq){
                    max = wordFrequencies.get(j);
                    maxIndex = j;
                }
            }

            if(max != wordFrequencies.get(i)) {
                wordFrequencies.set(maxIndex, wordFrequencies.get(i));
                wordFrequencies.set(i, max);
            }
        }

    }

    private String formatReportString(){
        StringBuilder report = new StringBuilder();

        report.append("----------------------------------------------------------------------\n");

        report.append(String.format("| %-42s | %-21s |\n", "LITERARY ANALYSIS REPORT METRIC", "VALUE"));

        report.append("----------------------------------------------------------------------\n");

        report.append(String.format("| %-42s | %-21d |\n", "Total Words (Without stopwords): ", totalWords));

        report.append(String.format("| %-42s | %-21d |\n", "Total Sentences: ", this.extractSentencesCount()));

        report.append(String.format("| %-42s | %-21d |\n", "Total Paragraphs: ", this.extractParagraphsCount()));

        report.append(String.format("| %-42s | %-21d |\n", "Total Characters (Without stopwords): ", totalCharacters));

        report.append(String.format("| %-42s | %-21d |\n", "Unique Words: ", uniqueWords));

        report.append(String.format("| %-42s | %-21.3f |\n", "Vocabulary Richness: ", vocabularyRichness));

        String mostFrequent = "N/A";

        if (!wordFrequencies.isEmpty()) {

            mostFrequent = wordFrequencies.get(0).word + " (" + wordFrequencies.get(0).freq + ")";
        }

        report.append(String.format("| %-42s | %-21s |\n", "Most Frequent Word", mostFrequent));

        report.append(String.format("| %-42s | %-21s |\n", "Longest Word", longestWord));

        report.append(String.format("| %-42s | %-21d |\n", "Vowels", vowels));

        report.append(String.format("| %-42s | %-21d |\n", "Consonants", consonants));

        report.append(String.format("| %-42s | %-21s |\n", "Sentiment Classification", sentiment));

        report.append("----------------------------------------------------------------------\n");

        report.append("| TOP 20 FREQUENT WORDS DENSITY PROFILE                            |\n");

        report.append("----------------------------------------------------------------------\n");

        int limit = Math.min(20, wordFrequencies.size());

        for (int i = 0; i < limit; i++) {

            WordCount current = wordFrequencies.get(i);

            report.append(String.format("| %-42s | %-21d |\n", current.word, current.freq));
        }

        report.append("----------------------------------------------------------------------\n");

        return report.toString();
    }

    @Override
    public void analyze(){

        performBasicMetrics();

        report = formatReportString();


    }

    @Override
    public void generateReport(String filename) throws IOException{

    try(PrintWriter reportOut = new PrintWriter(filename)){
        reportOut.println(report);
    }


    }

    @Override
    public void displayResults(){
        System.out.println(report);
    }

}
