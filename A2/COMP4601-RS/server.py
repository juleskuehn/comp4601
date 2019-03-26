from flask import Flask
from html_builders import *

app = Flask(__name__)

@app.route("/rs")
def name():
  return "COMP4601-RS"

@app.route("/rs/context")
def context():
  return "Context page"

@app.route("/rs/community")
def community():
  return "Community page"

@app.route("/rs/fetch/<user>/<page>")
def userPage(user, page):
  return basePage(f"{user} - {page}", pageWithAds(page, loremIpsum))

@app.route("/rs/advertising/<category>")
def advertisingCategory(category):
  return "Advertising Category: " + category

if __name__ == '__main__':
  app.run(debug=True)
