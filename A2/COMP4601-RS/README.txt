Jules Kuehn
100661464

Brian Ferch
100962115


COMP4601 Assignment 2
Winter 2019


General testing instructions:

Must have Python installed (tested with Python 3.x)
Required Python dependencies: pandas, pickle, numpy, flask


Server testing Instructions:

Run the server with `python server.py`
This starts a server listening on localhost:5000
The required endpoints can then be accessed at localhost:5000/rs/
All required data to serve these endpoints has been pre-computed and is available in the relevant .pkl and .txt files in the user_profiles and LDA directories


LDA topic generation testing instructions:

Navigate to the LDA directory
Add the "pages" folder from the assignment 2 archive
Run `python lda.py`
New movieTopicDict.pkl and topicWordsDict.pkl files will be generated
