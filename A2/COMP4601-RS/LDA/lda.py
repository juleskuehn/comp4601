from pyspark import SparkContext
from pyspark.sql import SparkSession, Row
from pyspark.ml.clustering import LDA
from corpus_parser import CorpusParser
from os import listdir
from os.path import isfile, join, splitext
from bs4 import BeautifulSoup
import pickle

numTopWords = 200
numTopTopicWords = 15
numTopics = 6
numIterations = 10
numPagesToProcess = 10

# pages directory expected to be in cwd
pagesPath = "./pages/"
pagesFileNames = [f for f in listdir(pagesPath) if isfile(join(pagesPath, f))]
pagesFileNames = pagesFileNames[:numPagesToProcess]
filePaths = [join(pagesPath, fileName) for fileName in pagesFileNames]

parser = CorpusParser(filePaths)
filename = "lda_dataset.txt"
parser.generateLDADataset(filename, numTopWords)

sc = SparkContext("local", "LDA")
spark = SparkSession.builder.appName("LDA").getOrCreate()

sc.addFile(filename)
dataset = spark.read.format("libsvm").load(filename)

# Trains a LDA model.
lda = LDA()
lda.setK(numTopics).setMaxIter(numIterations)
model = lda.fit(dataset)

ll = model.logLikelihood(dataset)
lp = model.logPerplexity(dataset)
print(f"The lower bound on the log likelihood of the entire corpus: {ll}")
print(f"The upper bound on perplexity: {lp}")

# Describe topics.
topics = model.describeTopics(numTopTopicWords)

topicWordsDictionary = {}

for i, topic in enumerate(topics.collect()):
  topicId = i
  words = []
  for wordIndex in topic.termIndices:
    word = parser.getWordFromIndex(wordIndex)
    words.append(word.value)
  topicWordsDictionary[topicId] = words


transformed = model.transform(dataset)

movieTopicDictionary = {}

for i, document in enumerate(transformed.collect()):
  movieId = splitext(pagesFileNames[i])[0]
  topicDistribution = []
  for j, topicSimilarity in enumerate(document.topicDistribution):
    topicDistribution.append((j, topicSimilarity))

  maxSimilarityTopicId = max(topicDistribution, key=lambda x: x[1])[0]
  movieTopicDictionary[movieId] = maxSimilarityTopicId

with open("topicWordsDict.pkl", "wb") as f:
  pickle.dump(topicWordsDictionary, f)

with open("movieTopicDict.pkl", "wb") as f:
  pickle.dump(movieTopicDictionary, f)

spark.stop()
