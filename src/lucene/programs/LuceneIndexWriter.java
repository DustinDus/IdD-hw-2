package lucene.programs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneIndexWriter
{
	
	/* Indicizza i file del folder di input
	 * nelle cartelle sotto  */
    public static void main(String[] args) throws IOException
    {   
        ///////////
        /* Paths */
        ///////////
        // Directory in cui si trovano i file da indicizzare
        final Path inputFolder = Paths.get("inputFolder");
        // Directories in cui scrivere i file indicizzati
        final Directory titleIndex = FSDirectory.open( Paths.get("indexFolder/titleIndex") );
        final Directory contentIndex = FSDirectory.open( Paths.get("indexFolder/contentIndex") );
        
        /////////////
        /* Writers */
        /////////////
        final IndexWriter titleIndexWriter = createIndexWriter( new SimpleAnalyzer(), titleIndex );
        final IndexWriter contentIndexWriter = createIndexWriter( new StandardAnalyzer(), contentIndex );
        
        //////////////
        /* Indexing */
        //////////////
        long startingTime;
        // title index
        startingTime = System.currentTimeMillis();
        employIndexWriter( inputFolder, titleIndexWriter, "title" );
        System.out.println("Time to index titles: " + (System.currentTimeMillis()-startingTime) + " ms" );
        // content index
        startingTime = System.currentTimeMillis();
        employIndexWriter( inputFolder, contentIndexWriter, "content" );
        System.out.println("Time to index content: " + (System.currentTimeMillis()-startingTime) + " ms" );

        /////////
        /* End */
        /////////
        titleIndexWriter.close();
        contentIndexWriter.close();
        System.out.println("We're done here!");
    }

    
    /* Crea e restituisce un IndexWriter */
    private static IndexWriter createIndexWriter(final Analyzer analyzer, final Directory index) throws IOException
    {
    	// IndexWriterConfigs
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE);
        
        // IndexWriters
        IndexWriter indexWriter = new IndexWriter(index, config);
        
        // Return
        return indexWriter;
    }

    
    /* Impiega l'IndexWriter
     * per indicizzare i file dal folder di input
     * secondo il campo specificato */
    private static void employIndexWriter(final Path inputFolder, final IndexWriter indexWriter, final String field)
    {
    	// Ricorro per ogni documento in input
        try {
            Files.walkFileTree(inputFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                   	try (InputStream stream = Files.newInputStream(file))
                    {
                        // Nuovo documento Lucene
                        Document doc = new Document();
                        
                        // Memorizzo il path
                        doc.add(new StringField("path", file.toString(), Field.Store.YES));
                        
                        // Analizzo il campo
                        //
                        // title: guardo il nome del file
                        if (field.equals("title")) {doc.add(new TextField("title", file.getFileName().toString(), Store.YES));}
                        // content: guardo il contenuto
                        else if (field.equals("content")) {doc.add(new TextField("content", new String(Files.readAllBytes(file)), Store.YES));}
                        // Altro? Non so che fare
                        else {throw new DatatypeConfigurationException();}
                        
                        // Scrivo il documento su index
                        indexWriter.updateDocument(new Term("path", file.toString()), doc);
                    }
                    catch (IOException e)
                    {
                    	System.err.println("Error visiting input file: " + file + "\n" + e.getMessage());
                    	System.exit(1);
                    }
                   	catch (DatatypeConfigurationException e)
                   	{
                   		System.err.println("I don't recognize this field: " + field + "\n" + e.getMessage());
                    	System.exit(1);
                   	}
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            System.err.println("Error traversing index directory\n" + e.getMessage());
            System.exit(1);
        }
    }

}