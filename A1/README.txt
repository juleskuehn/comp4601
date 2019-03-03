Jules Kuehn
100661464

Brian Ferch
100962115



COMP4601 Assignment 1
Winter 2019



Usage: Run WAR file on Tomcat 9.0

 - To crawl and create a local mongoDB and Lucene index:
	http://localhost:8080/COMP4601-SDA/rest/sda/crawl/{maxPages}
	As packaged, crawls the following pages with 3 crawler threads:
				"http://lol.jules.lol/parsertest/",
	        	"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/resources/N-0.html",
	        	"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/handouts/"

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

