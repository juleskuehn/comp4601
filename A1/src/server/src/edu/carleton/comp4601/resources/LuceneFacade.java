package edu.carleton.comp4601.resources;
/*
 * Based on code from http://lucene.apache.org/core/7_7_0/demo/index.html
 * With the following license:
 * 
Copyright 2019 Jules Kuehn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import Jama.Matrix;

// See usage in main() below
public class LuceneFacade {
	
	public static String COMP_4601_BASE = System.getProperty("user.home") + File.separator;
	public static String INDEX_PATH = COMP_4601_BASE + "lucene_temp/";
	private MongoStore mongoStore;

    public LuceneFacade() {
    	mongoStore = MongoStore.getInstance();
    }
    
    @SuppressWarnings("unused")
	public void index(boolean create, boolean boost) {
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + INDEX_PATH + "'...");

            Directory dir = FSDirectory.open(Paths.get(INDEX_PATH));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }
            
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, mongoStore.getDocColl(), boost);
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }
    

    /**
     * Indexes all the documents in a MongoCollection into Lucene
     * @param writer Writer to the index where the given file/dir info will be
     *               stored
     * @param docColl MongoCollection to index
     * @throws IOException If there is a low-level I/O error
     */
    static void indexDocs(final IndexWriter writer, MongoCollection<org.bson.Document> docColl, boolean boost) throws IOException {
    	FindIterable<org.bson.Document> docs = docColl.find();
		MongoCursor<org.bson.Document> cursor = docs.iterator();
        try {
            while(cursor.hasNext()) {               
            	indexDoc(writer, cursor.next(), boost);
            }
        } finally {
            cursor.close();
        }
    }

    /** Indexes a single document */
    @SuppressWarnings("deprecation")
	static void indexDoc(IndexWriter writer, org.bson.Document mongoDoc, boolean boost) throws IOException {
        Document luceneDoc = new Document();
        
        luceneDoc.add(new StoredField("url", mongoDoc.getString("url")));
        luceneDoc.add(new StoredField("docId", mongoDoc.getInteger("_id")));
        luceneDoc.add(new TextField("i", new StringReader("Jules Kuehn and Brian Ferch")));
        luceneDoc.add(new StoredField("i", "Jules Kuehn and Brian Ferch"));
        luceneDoc.add(new StoredField("date", ((Date) mongoDoc.get("crawltime")).getTime()));
        luceneDoc.add(new StoredField("type", mongoDoc.getString("type")));
        luceneDoc.add(new TextField("type", new StringReader(mongoDoc.getString("type"))));
        
        // Add searchable content (body text, title, URL, MIME-type)
        String content = mongoDoc.getString("content") + " ";
        content += mongoDoc.getString("name") + " ";
        TextField tfContent = new TextField("content", new StringReader(content));
        // Apply PageRank score as boost on each field
        if (boost) {
        	tfContent.setBoost(mongoDoc.getDouble("score").floatValue());
        } else {
        	tfContent.setBoost(1);
        }
        luceneDoc.add(tfContent);
        
        // Add searchable tags
        String tags = "";
        ArrayList<String> tagsList = (ArrayList<String>) mongoDoc.get("tags");
        for (String tag : tagsList) {
        	tags += tag + " ";
        }
        TextField tfTags = new TextField("tags", new StringReader(tags));
        // Default
        luceneDoc.add(tfTags);


        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            System.out.println("adding " + mongoDoc.getString("name"));
            writer.addDocument(luceneDoc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so
            // we use updateDocument instead to replace the old one matching docId
            System.out.println("updating " + mongoDoc.getString("name"));
            writer.updateDocument(new Term("docId"), luceneDoc);
        }
    }
    
    public ArrayList<edu.carleton.comp4601.dao.Document> query(String searchString) {
    	try {
	    	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH).toPath()));
	    	IndexSearcher searcher = new IndexSearcher(reader);
	    	Analyzer analyzer = new StandardAnalyzer();
	    	QueryParser parser = new QueryParser("content", analyzer);
	    	Query q = parser.parse(searchString);
	    	TopDocs results = searcher.search(q, 100); // 100 documents!
	    	ScoreDoc[] hits = results.scoreDocs;
	    	ArrayList<edu.carleton.comp4601.dao.Document> resultDocs = getDocs(hits, searcher);
	    	reader.close();
	    	return resultDocs;
    	} catch (IOException | ParseException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public ArrayList<edu.carleton.comp4601.dao.Document> getDocs(ScoreDoc[] hits, IndexSearcher searcher) throws IOException {
    	ArrayList<edu.carleton.comp4601.dao.Document> docs = new ArrayList<edu.carleton.comp4601.dao.Document>();
    	for (ScoreDoc hit : hits) {
    		Document indexDoc = searcher.doc(hit.doc);
    		String id = indexDoc.get("docId");
    		if (id != null) {
    			edu.carleton.comp4601.dao.Document d = mongoStore.getDocument((Integer.valueOf(id)));
    			if (d != null) {
		    		d.setScore(hit.score);
		    		docs.add(d);
    			}
    		}
    	}
    	return docs;
    }    

    public static void main(String[] args) {
        LuceneFacade myLuceneFacade = new LuceneFacade();
        // Create new index with boost. Arguments are (forceCreateIndex, applyBoost)
        myLuceneFacade.index(true, true);
    }
   
}