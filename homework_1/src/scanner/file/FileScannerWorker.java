package scanner.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class FileScannerWorker implements Callable<Map<String, Integer>> {

    private final List<String> keywords;
    private final List<File> files;

    public FileScannerWorker(List<String> keywords, List<File> files) {
        this.keywords = keywords;
        this.files = files;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> results = new HashMap<>();

        //System.out.println("Svoj deo posla obavlja: " +Thread.currentThread().getName());

        for (String key : keywords) {
            //stavljamo inicijalnu vrednost 0 za svaku kljucnu rec, koja nam predstavlja kljuc
            results.put(key, 0);
        }

        for (File f : this.files){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath()));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] words = line.split("\\s+");// delimo u odnosu na razmake/tabove/nove redove

                    for (String word: words) {

                        if (word != null && keywords.contains(word)) {

                            //uzimamo vrednost za kljuc word ili 0 ako kljuc ne postoji u mapi
                            int count = results.getOrDefault(word, 0);
                            results.put(word, count + 1);
                        }
                    }
                }

                reader.close();

            }catch (Exception e) {
                System.out.println("File: [" + f.getPath() + "] cannot be read");
            }
        }
        return results;
    }
}
