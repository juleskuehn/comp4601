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

def svd_predict(Uk, SkSqrt, Vkt, userAvgRatings, userId, movieId):
    pseudoUsers = Uk.dot(SkSqrt)
    pseudoFilms = SkSqrt.dot(Vkt)
    return (userAvgRatings[userId]
            + pseudoUsers[userId].dot(pseudoFilms[:, movieId]))

def calc_userAvgs(ratingsFrame, helpfulsFrame, userId_to_profileName=None, show=True): # Get (and print) user stats
    # Get (and print) user stats
    userAvgRatings = {}
    userAvgHelpfuls = {}
    for userId in list(ratingsFrame.index):
        userAvgRatings[userId] = calc_userAvgRating(ratingsFrame, userId)
        userAvgHelpfuls[userId] = calc_userAvgHelpful(helpfulsFrame, userId)
        if show:
            print(f'{userId:15} : {userId_to_profileName[userId]:30}'
                + f'    {to_stars(userAvgRatings[userId]):3.1f} star avg'
                + f'    {userAvgHelpfuls[userId]*100:3.0f}% helpful')
    return userAvgRatings, userAvgHelpfuls

def fillRatingsFrame(R, userAvgRatings):
    R = R.copy()
    # Fill missing values with item averages
    allMoviesAvg = np.average(
                    [rating for _, rating in userAvgRatings.items()])
    print("allMoviesAvg", allMoviesAvg)
    for i, movieId in enumerate(list(R)):
        filled = 0
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
                if R.loc[userId, movieId] < 0:
                    filled += 1
                    R.loc[userId, movieId] = movieAvg
        print("Filled", filled, "ratings for movieId", movieId, f'with {movieAvg:3.2f} ({i*100/len(list(R)):3.0f}% done)')
    return R

def normalizeRatingsFrame(R, userAvgRatings, userAvgHelpfuls=None):
    # Normalize: Subtract user average from each of their ratings
    for userId in list(R.index):
        # TODO Multiply by helpfulness to increase weight?
        R.loc[userId, :] -= userAvgRatings[userId]
        if userAvgHelpfuls != None:
            R.loc[userId, :] *= userAvgHelpfuls[userId]
    return R