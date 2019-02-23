package edu.carleton.comp4601.resources;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bson.Document;

public class MongoStore {

	public static String COMP_4601_BASE = System.getProperty("user.home") + File.separator;
	
	static MongoStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> docColl;

	public MongoStore() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("sda");
		docColl = db.getCollection("pages");
	}
	
	public void reindex() {
		try {
			index(true, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(Document doc) {
		docColl.replaceOne(Filters.eq("_id", doc.getInteger("_id")), doc, new UpdateOptions().upsert(true));
		reindex();
	}
	
	public void add(int thisDocId, String name, String url, String content,
			ArrayList<String> tags, ArrayList<String> links, String type, Double score) {
		Document doc = new Document("_id", thisDocId)
							.append("name", name)
							.append("url", url)
							.append("content", content)
							.append("tags", tags)
							.append("links", links)
							.append("crawltime", new Date())
							.append("type", type)
							.append("score", score);
		add(doc);
	}
	
	// Convenience functions for interaction with front-end
	// TODO ensure return types match with assignment requirements
	public void add(edu.carleton.comp4601.dao.Document sdaDocument) {
		add(toMongoDocument(sdaDocument));
	}
	
	public Document find(int id) {
		Document document = docColl.find(Filters.eq("_id", id)).first();
		return document;
	}
	
	public edu.carleton.comp4601.dao.Document getDocument(int id) {
		Document mongoDocument = find(id);
		return toSdaDocument(mongoDocument);
	}
	
	public void update(edu.carleton.comp4601.dao.Document sdaDocument) {
		Document update = toMongoDocument(sdaDocument);
		docColl.updateOne(Filters.eq("_id", sdaDocument.getId().intValue()), new Document("$set", update));
		reindex();
	}
	
	// This nastiness is because the SDA Document class takes a Float (not a Double) for score
	// Otherwise, we could use the Document(mongoDocument) map constructor
	public edu.carleton.comp4601.dao.Document toSdaDocument(Document mongoDocument) {
		edu.carleton.comp4601.dao.Document sdaDocument = new edu.carleton.comp4601.dao.Document();
		sdaDocument.setId(mongoDocument.getInteger("_id"));
		sdaDocument.setName(mongoDocument.getString("name"));
		sdaDocument.setUrl(mongoDocument.getString("url"));
		sdaDocument.setContent(mongoDocument.getString("content"));
		sdaDocument.setTags((ArrayList<String>) mongoDocument.get("tags"));
		sdaDocument.setLinks((ArrayList<String>) mongoDocument.get("links"));
		sdaDocument.setScore(((Double) mongoDocument.get("score")).floatValue());
		return sdaDocument;
	}
	
	public Document toMongoDocument(edu.carleton.comp4601.dao.Document sdaDocument) {
		return new Document("_id", (int) sdaDocument.getId())
				.append("name", sdaDocument.getName())
				.append("url", sdaDocument.getUrl())
				.append("content", sdaDocument.getContent())
				.append("tags", sdaDocument.getTags())
				.append("links", sdaDocument.getLinks())
				.append("crawltime", new Date())
				.append("type", "MIME-type is undefined in sdaDocument")
				.append("score", (double) sdaDocument.getScore());
	}
	
	public int getIdByURL(String url) {
		Document document = docColl.find(Filters.eq("url", url)).first();
		return Integer.parseInt(document.get("_id").toString());
	}
	
	public void setScore(int id, double newScore) {
		Document doc = find(id);
		doc.put("score", newScore);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}
	
	public boolean deleteOne(int id) {
		return docColl.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
	}
	
	public boolean deleteAllWithTag(String tag) {
		return docColl.deleteMany(Filters.eq("tags", tag)).getDeletedCount() > 0;
	}
	
	public edu.carleton.comp4601.dao.DocumentCollection getByTag(String tag) {
		edu.carleton.comp4601.dao.DocumentCollection coll = new edu.carleton.comp4601.dao.DocumentCollection();
		ArrayList<edu.carleton.comp4601.dao.Document> tmpList = new ArrayList<edu.carleton.comp4601.dao.Document>();
		FindIterable<Document> docs = tag == null ? docColl.find() : docColl.find(Filters.eq("tags", tag));
		MongoCursor<Document> cursor = docs.iterator();
        try {
            while(cursor.hasNext()) {               
                tmpList.add(toSdaDocument(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        coll.setDocuments(tmpList);
		return coll;
	}
	
	public edu.carleton.comp4601.dao.DocumentCollection getAll() {
		return getByTag(null);
	}
	
	public void addLink(int id, String linkUrl) {
		Document doc = find(id);
		ArrayList<String> links = (ArrayList<String>) doc.get("links");
		links.add(linkUrl);
		doc.put("links", links);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}
	
	public void addTag(int id, String tag) {
		Document doc = find(id);
		ArrayList<String> tags = (ArrayList<String>) doc.get("tags");
		tags.add(tag);
		doc.put("tags", tags);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}
	
    @SuppressWarnings("unused")
	public void index(boolean create, boolean boost) throws IOException {
    	String indexPath = COMP_4601_BASE + "lucene_temp/";
        Date start = new Date();
        
        System.out.println("Indexing to directory '" + indexPath + "'...");

        Directory dir = FSDirectory.open(Paths.get(indexPath));
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
        indexDocs(writer, docColl, boost);
        writer.close();

        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }
    

    /**
     * Indexes all the documents in a MongoCollection into Lucene
     * @param writer Writer to the index where the given file/dir info will be
     *               stored
     * @param docColl MongoCollection to index
     * @throws IOException If there is a low-level I/O error
     */
    static void indexDocs(final IndexWriter writer, MongoCollection<Document> docColl, boolean boost) throws IOException {
    	FindIterable<Document> docs = docColl.find();
		MongoCursor<Document> cursor = docs.iterator();
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
	static void indexDoc(IndexWriter writer, Document mongoDoc, boolean boost) throws IOException {
    	org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
        
        luceneDoc.add(new StringField("url", mongoDoc.getString("url"), Field.Store.YES));
        luceneDoc.add(new IntPoint("docId", mongoDoc.getInteger("_id")));
        luceneDoc.add(new StringField("i", "Jules Kuehn and Brian Ferch",  Field.Store.YES));
        luceneDoc.add(new LongPoint("date", ((Date) mongoDoc.get("crawltime")).getTime()));
        luceneDoc.add(new StringField("type", mongoDoc.getString("type"), Field.Store.YES));
        
        // Add searchable content (body text, title, URL, MIME-type)
        String content = mongoDoc.getString("content") + " ";
        content += mongoDoc.getString("name") + " ";
        content += mongoDoc.getString("url") + " ";
        content += mongoDoc.getString("type") + " ";
        TextField tfContent = new TextField("content", new StringReader(content));
        // Apply PageRank score as boost on the content field only
        if (boost)
        	tfContent.setBoost(mongoDoc.getDouble("score").floatValue());
        luceneDoc.add(tfContent);
        
        // Add searchable tags
        String tags = "";
        ArrayList<String> tagsList = (ArrayList<String>) mongoDoc.get("tags");
        for (String tag : tagsList) {
        	tags += tag + " ";
        }
        TextField tfTags = new TextField("tags", new StringReader(tags));
        // Default
        tfContent.setBoost(2);
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
	
	public static MongoStore getInstance() {
		if (instance == null)
			instance = new MongoStore();
		return instance;
	}
}