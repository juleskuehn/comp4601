
from movie_page_parser import parseMoviePageHTML

class CorpusParser:

  FILE_NAMES = []
  STOP_FILE = "stop.txt"
  STOP_WORDS = []
  DOC_MAPS = []
  MASTER_MAP = {}
  TERMS = []

  def __init__(self, fileNames):
    self.FILE_NAMES = fileNames
    self.readStopWords()
    self.readWords()
    self.sortTerms()

  def readStopWords(self):
    with open(self.STOP_FILE) as file:
      for line in file:
        for word in line.split():
          self.STOP_WORDS.append(word)
    
  def readWords(self):
    for fileName in self.FILE_NAMES:
      docMap = {}
      with open(fileName) as file:
        text = parseMoviePageHTML(file.read())
        for word in text.split():
          word = word.lower()
          if word in docMap:
            docMap[word] += 1
          else:
            docMap[word] = 1
          if word in self.MASTER_MAP:
            self.MASTER_MAP[word] += 1
          else:
            self.MASTER_MAP[word] = 1
      
      for stopWord in self.STOP_WORDS:
        docMap.pop(stopWord, None)
        self.MASTER_MAP.pop(stopWord, None)
      
      self.DOC_MAPS.append(docMap)

  def sortTerms(self):
    for [itemKey, itemValue] in self.MASTER_MAP.items():
      docFrequency = 0
      for i in range(len(self.FILE_NAMES)):
        if itemKey in self.DOC_MAPS[i]:
          docFrequency += 1
      self.TERMS.append(Word(itemKey, itemValue, docFrequency))

    self.TERMS.sort(reverse=True, key=lambda word: word.frequency)

  def getDocWordIdFreq(self, docId, wordId):
    word = self.TERMS[wordId]
    docMap = self.DOC_MAPS[docId]
    if word in docMap:
      return docMap[word]
    else:
      return 0

  def getDocWordFreq(self, docId, word):
    docMap = self.DOC_MAPS[docId]
    if word in docMap:
      return docMap[word]
    else:
      return 0

  def getWordFromIndex(self, index):
    return self.TERMS[index]

  def generateLDADataset(self, filename="lda_dataset.txt", numWords=5):
    topWords = self.TERMS[:numWords]
    output = ""
    for i in range(len(self.FILE_NAMES)):
      output += f"{i}"
      for j, word in enumerate(topWords):
        docFreq = self.getDocWordFreq(i, word.value)
        output += f" {j + 1}:{docFreq}"
      output += "\n"
    with open(filename, "w") as file:
      file.write(output)


class Word:
  value = ""
  frequency = 0
  documentFrequency = 0

  def __init__(self, value, frequency, documentFrequency):
    self.value = value
    self.frequency = frequency
    self.documentFrequency = documentFrequency