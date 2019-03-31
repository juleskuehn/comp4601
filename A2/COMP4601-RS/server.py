from flask import Flask
from dao import *
from html_builders import *

app = Flask(__name__)

@app.route("/rs")
def name():
  return "COMP4601-RS"

@app.route("/rs/context")
def context():
  string = "Generated user profiles:"
  for userId in userId_to_profileName:
    string += get_userString(userId) 
  return string

@app.route("/rs/community")
def community():
  m = 4 # 4 communities
  userNamesByCluster = [[] for _ in range(m)]
  for userId, userName in userId_to_profileName.items():
    userCluster = get_userCluster(userId)
    userNamesByCluster[userCluster].append(userName)
  return f'{userNamesByCluster}'

@app.route("/rs/fetch/<user>/<page>")
def userPage(user, page):
  # ad = genAdvertising(page, user, userAssignments2d, movieAssignments, communityRatings, communityRecs);
  ad = ""
  return basePage(f"{user} - {page}", pageWithAds(page, ad))

@app.route("/rs/advertising/<category>")
def advertisingCategory(category):
  return "Advertising Category: " + category

if __name__ == '__main__':
  app.run(debug=True)
