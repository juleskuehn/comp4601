import numpy as np
import pandas as pd

def to_stars(rating):
    return (rating * 4) + 1

def to_float(rating):
    return (rating - 1) / 4.

# Returns a user's average rating (float)
def calc_userAvgRating(ratingsFrame, userId):
    return np.average(
        [rating for rating in ratingsFrame.loc[userId]
                 if rating >= 0])

# Returns a user's average helpfulness (float)
def calc_userAvgHelpful(helpfulsFrame, userId):
    return np.average(
        [rating for rating in helpfulsFrame.loc[userId]
                 if rating >= 0])

# Returns a movies's average rating (float)
def calc_movieAvgRating(ratingsFrame, movieId):
    return np.average(
        [rating for rating in ratingsFrame.loc[:, movieId]
                 if rating >= 0])

# Takes np.array of ratings, number of output dimensions 
def svd_reduceDimensions(R, k):
    # Perform SVD
    U, S, V = np.linalg.svd(R, full_matrices=True)
    S = np.diag(S)

    # Crop to k dimensions
    UK = U[:, :k]
    SK = S[:k, :k]
    VK = V[:k, :]

    return UK, np.sqrt(SK), VK

def fillAndNormalize(R, userAvgRatings, userAvgHelpfuls=None):
    R = R.copy()
    # Fill missing values with item averages
    allMoviesAvg = np.average(
                    [rating for _, rating in userAvgRatings.iteritems()])
    for movieId in list(R):
        movieRatings = R.loc[:, movieId]
        movieAvg = np.average(
        [rating for rating in movieRatings
                 if rating >= 0])
        # If the film has never been rated, give it the overall average
        if movieAvg < 0:
            R.loc[:, movieId] = allMoviesAvg
        # Otherwise fill in empty ratings with the movie average
        else:
            for userId in list(R.index):
                R.loc[userId, movieId] -= movieId in list(R)

    # Normalize: Subtract user average from each of their ratings
    for userId in list(R.index):
        # TODO Multiply by helpfulness to increase weight?
        R[userId] -= userAvgRatings[userId]
        if userAvgHelpfuls != None:
            R[userId] *= userAvgHelpfuls[userId]

    return R