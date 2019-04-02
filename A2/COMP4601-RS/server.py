from flask import Flask
from dao import *
from html_builders import *

app = Flask(__name__)

@app.route("/rs")
def name():
  return "COMP4601-RS"

@app.route("/rs/context")
def context():
  tableHeaders = ['User ID', 'User Name', 'Helpfullness', 'Average Star Rating', 'Cluster']
  tableRows = get_userRows()
  return basePage('Context', tablePage('Generated user profiles', tableHeaders, tableRows))

@app.route("/rs/community")
def community():
  m = 4 # 4 communities
  userNamesByCluster = [[] for _ in range(m)]
  for userId, userName in userId_to_profileName.items():
    userCluster = get_userCluster(userId)
    userNamesByCluster[userCluster].append(userName)

  tableHeaders = ['Community', 'Users']
  tableRows = []
  for index, cluster in enumerate(userNamesByCluster):
    tableRows.append([f'C-{index + 1}', listToCommaString(cluster)])
  
  return basePage('Community', tablePage('User communities', tableHeaders, tableRows))

@app.route("/rs/fetch/<user>/<page>")
def userPage(user, page):
  ad = genAdvertising(page, user, userAssignments2d, movieAssignments, communityRatings, communityRecs)
  return basePage(f"{user} - {page}", pageWithAds(page, ad))

@app.route("/rs/advertising/<category>")
def advertisingCategory(category):
  style = '<style>body { margin: 0 30px; }</style>'
  header = '<h1>Advertised movies for category ' + category + '</h1>'
  return basePage("Advertising Category " + category,
    f'{style} {header} {listToCommaString(communityRecs[int(category) - 1])}')

if __name__ == '__main__':
  app.run(debug=True)
