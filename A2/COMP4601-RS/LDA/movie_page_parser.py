
from bs4 import BeautifulSoup

def parseMoviePageHTML(html):
  soup = BeautifulSoup(html, 'html.parser')
  soup.find('title').decompose()
  for a in soup.findAll('a'):
    a.decompose()
  return soup.getText()