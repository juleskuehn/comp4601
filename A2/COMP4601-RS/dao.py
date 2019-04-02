import pickle
import pandas as pd
from movie_helpers import to_stars
from html_builders import userIdToLink
import numpy as np
# from scipy.spatial import distance

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
with open('LDA/movieTopicDict.pkl', 'rb') as f:
    movieAssignments = pickle.load(f)
with open('LDA/topicWordsDict.pkl', 'rb') as f:
    topicWords = pickle.load(f)
    
communityRecs = []
with open('user_profiles/community_recommendations.txt', 'r') as f:
    for line in f:
        communityRecs.append([movieId for movieId in line.split()])

communityRatings = []
with open('user_profiles/community_ratings.txt', 'r') as f:
    for i, line in enumerate(f):
        if i == 0:
            columns = [movieId for movieId in line.split()]
        else:
            communityRatings.append([float(rating) for rating in line.split()])
    communityRatings = pd.DataFrame(np.array(communityRatings), columns=columns)

# TODO get timestamps of reviews (from HTML) in DataFrame corresponding to ratingsFrame

def print_topics():
    for topic in topicWords:
        print(topicWords[topic])
    import collections
    print(collections.Counter(topicWords[0]+topicWords[1]+topicWords[2]+topicWords[3]))

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

def get_userRows():
    rows = []
    for userId in userId_to_profileName:
        rows.append([userIdToLink(userId), get_userName(userId), f'{get_userAvgHelpful(userId):3.1f}', f'{get_userAvgRating(userId):3.1f}', get_userCluster(userId)])
    return rows

def get_rating(userId, movieId):
    return ratingsFrame.loc[userId, movieId]

# Returns a list of userIds for the k nearest neighbours in 2d
# def get_neighbours(userId, k):
#     # Get k nearest neighbours to user in 2d
#     user2d = ratings2d.loc[userId]
#     # Find the distance between this user and everyone else.
#     euclidean_distances = ratings2d.apply(lambda user: distance.euclidean(user, user2d), axis=1)
#     distance_frame = pd.DataFrame(data={"dist": euclidean_distances, "userId": euclidean_distances.index})
#     distance_frame.sort_values("dist", inplace=True)
#     # Since the smallest distance would be this user, don't include first userId
#     return [distance_frame.iloc[i]["userId"] for i in range(1, k + 1)]

def print_neighbours(userId, k):
    for neighbourId in get_neighbours(userId, k):
        print(get_userString(neighbourId))
