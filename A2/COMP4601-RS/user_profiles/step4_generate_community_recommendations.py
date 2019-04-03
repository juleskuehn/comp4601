#!/usr/bin/env python

import numpy as np
import pandas as pd
import pickle
import scipy as sp
from numpy import array
import matplotlib.pyplot as plt
from movie_helpers import *
from collections import defaultdict
from sklearn.cluster import KMeans


print(f"\nLoading from data_*.pkl")
ratingsFrame = pd.read_pickle('data_ratingsFrame.pkl')
with open('userAssignments2d.pkl', 'rb') as f:
    userAssignments = pickle.load(f)
# with open('movieAssignments', 'rb') as f:
#     movieAssignments = pickle.load(f)

numCommunities = 4
# numTopics = len(movieAssignments[0])
# print(userAssignments)

userIdByCluster = [[] for _ in range(numCommunities)]
for userId, row in userAssignments.iterrows():
    userIdByCluster[row.loc[0]].append(userId)

communityRatings = pd.DataFrame(np.full((numCommunities, ratingsFrame.shape[1]), -1, dtype='float32'),
                    columns=ratingsFrame.columns)

for community in range(numCommunities):
    # Get the community rating for each film
    for movieId in ratingsFrame.columns:
        numRatings = 0
        for userId in userIdByCluster[community]:
            rating = ratingsFrame.loc[userId, movieId]
            if rating >= 0:
                numRatings += 1
                communityRatings.loc[community, movieId] += rating
        if numRatings > 0:
            communityRatings.loc[community, movieId] /= numRatings

print(communityRatings)

communityAvgs = [np.average(
    [rating for rating in communityRatings.loc[i] if rating >= 0]
    ) for i in range(numCommunities)]
print(communityAvgs)

communityRecs = [[] for _ in range(numCommunities)]
for community in range(numCommunities):
    # Get a list of films that have a higher rating than the average
    for movieId in ratingsFrame.columns:
        rating = communityRatings.loc[community, movieId]
        if rating >= communityAvgs[community]:
            communityRecs[community].append(movieId)


# Make some text representations since the pickle won't work (???)
comRecStr = ""
for communityRec in communityRecs:
    comRecStr += ' '.join(communityRec)
    comRecStr += "\n"

with open('community_recommendations.txt', 'w') as f:
    f.write(comRecStr)

comRatingStr = ' '.join(ratingsFrame.columns) + '\n'
for row in communityRatings.iterrows():
    comRatingStr += ' '.join([str(rating) for rating in row[1].tolist()]) + '\n'

with open('community_ratings.txt', 'w') as f:
    f.write(comRatingStr)


# with open('community_recommendations.pkl', 'wb') as f:
#     pickle.dump(communityRecs, f)

# communityRatings.to_pickle('community_ratings.pkl')
