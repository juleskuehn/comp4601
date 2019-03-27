import pickle
import pandas as pd
from movie_helpers import to_stars
    
ratingsFrame = pd.read_pickle('user_profiles/data_ratingsFrame.pkl')
helpfulsFrame = pd.read_pickle('user_profiles/data_helpfulsFrame.pkl')

with open('user_profiles/data_userId_to_profileName.pkl', 'rb') as f:
    userId_to_profileName = pickle.load(f)
with open('user_profiles/ratings_2d.pkl', 'rb') as f:
    ratings2d = pd.DataFrame(pickle.load(f), index=ratingsFrame.index)
with open('user_profiles/ratings_200d.pkl', 'rb') as f:
    ratings200d = pd.DataFrame(pickle.load(f), index=ratingsFrame.index)
with open('user_profiles/userAvgRatings.pkl', 'rb') as f:
    userAvgRatings = pickle.load(f)
with open('user_profiles/userAvgHelpfuls.pkl', 'rb') as f:
    userAvgHelpfuls = pickle.load(f)
with open('user_profiles/userAssignments2d.pkl', 'rb') as f:
    userAssignments2d = pickle.load(f)
with open('user_profiles/userAssignments200d.pkl', 'rb') as f:
    userAssignments200d = pickle.load(f)
# TODO get timestamps of reviews (from HTML) in DataFrame corresponding to ratingsFrame

def get_userName(userId):
    return userId_to_profileName[userId]

# Returns a star rating
def get_userAvgRating(userId):
    return to_stars(userAvgRatings[userId])

# Returns a percentage
def get_userAvgHelpful(userId):
    return userAvgHelpfuls[userId] * 100

# Returns a number from [0...numClusters-1]
def get_userCluster(userId):
    return userAssignments200d.loc[userId, 0]

def get_userPoint(userId):
    return ratings2d.loc[userId, 0], ratings2d.loc[userId, 1]

def get_userString(userId):
    return f'<br>{userId} {get_userName(userId)} {get_userAvgHelpful(userId):3.1f}% helpful {get_userAvgRating(userId):3.1f} star average rating. Cluster {get_userCluster(userId)}'