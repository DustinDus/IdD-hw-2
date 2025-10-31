package lucene.programs;

import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneIndexSearcher
{
	
	/* Chiede in input all'utente una query con il seguente formato: <keyword>_r_<query>
	 * <keyword> indica l'index da interrrogare
	 * <query> è l'effettiva query usata */
    public static void main(String[] args) throws Exception
    {
        ///////////
    	/* Input */
    	///////////
        
        // Inserisci una query
        System.out.println("Please, input the search query with the following syntax:");
        System.out.println("<keyword>_r_<query>");
        
        // Input query inserita dall'utente
        Scanner scanner = new Scanner(System.in);
        String input_query = scanner.nextLine();
        scanner.close();
        
        // Il formato della query corretto?
        String[] queryParts = input_query.split("_r_");
        if (queryParts.length!=2)
        {
        	System.out.println("The syntax is incorrect...");
        	System.exit(0);
        }
        
        // Interpreto la query
        String keyword = queryParts[0];
        String query = queryParts[1];
        
        // Riconosco la keyword?
        if ( !keyword.equals("title") && !keyword.equals("content") )
        {
        	System.out.println("I don't recognize this keyword... Try 'title' o 'content'");
        	System.exit(0);
        }
        
        ////////////
    	/* Search */
    	////////////
        
        // Directory da cui leggere i file indicizzati
        final Directory targetIndex = FSDirectory.open( Paths.get("indexFolder/" + keyword + "Index" ) );
        
        // Setup IndexSearch
        IndexReader reader = DirectoryReader.open(targetIndex);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // Analyzer scelto
        Analyzer analyzer ;
        if (keyword.equals("title")) {analyzer = new SimpleAnalyzer();}
        else {analyzer = new StandardAnalyzer();}
        
        // Parse della query
        QueryParser parser = new QueryParser(keyword, analyzer);
        Query parsed_query = parser.parse(query);
        
        // Search (restituisco al massimo i 10 risultati migliori)
        TopDocs results = searcher.search(parsed_query, 10);
        
        ////////////
  	    /* Output */
  	    ////////////
        
        //Total found documents
        System.out.println("Results: " + results.totalHits);
         
        //Let's print out the path of files which have searched term
        for (ScoreDoc scoreDoc : results.scoreDocs)
        {
            Document doc = searcher.doc(scoreDoc.doc);
            String[] docPath = doc.get("path").split("\\\\");
            System.out.println("Doc: "+ docPath[docPath.length-1] + ", Score: " + scoreDoc.score);
        }
    }
    
}
