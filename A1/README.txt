Jules Kuehn
100661464

Brian Ferch
100962115


Usage:

Run "Controller" (as a Java application).
 - This will build a MongoDB collection for the crawled pages, and for the graph.
 - It will then compute PageRank and build a Lucene index.

Run "SearchableDocumentArchive" (on Tomcat).
 - To create a document:
	http://localhost:8080/COMP4601-SDA/HTML/create_document.html
 - Other endpoints:
	http://localhost:8080/COMP4601-SDA/rest/sda    (POST, GET)
	http://localhost:8080/COMP4601-SDA/rest/sda/list
	http://localhost:8080/COMP4601-SDA/rest/sda/query/{query}
	http://localhost:8080/COMP4601-SDA/rest/sda/search/{query}
	http://localhost:8080/COMP4601-SDA/rest/sda/{id}   (POST, GET, DELETE)
	http://localhost:8080/COMP4601-SDA/rest/sda/reset   (same as boost)
	http://localhost:8080/COMP4601-SDA/rest/sda/pagerank
	http://localhost:8080/COMP4601-SDA/rest/sda/boost
	http://localhost:8080/COMP4601-SDA/rest/sda/noboost
	http://localhost:8080/COMP4601-SDA/rest/sda/delete/{tags}

