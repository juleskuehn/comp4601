from pycorenlp import StanfordCoreNLP
from bs4 import BeautifulSoup
import numpy as np
import os
import matplotlib
import matplotlib.pyplot as plt
from collections import defaultdict
import pandas as pd
import pickle
matplotlib.style.use('ggplot')


# We want a matrix that has [user][movie]
# All the necessary information (and more!) is in the reviews directory
# While we're at it, let's retrieve:
# - profileName for each user {'userID': 'profileName'}
# - usefulness for each rating (same format as rating matrix [user][movie])
# To simplify creation of pandas dataframe, use numpy (unlabelled) matrix
# - dictionary mapping userID to index1 in ratings matrix {'userID': int index1}
# - dictionary mapping movieID to index2 in matrix {'movieID': int index2}
# - list mapping index1 to userID
# - list mapping index2 to movieID

# How big should our ratings matrix be?
# numUsers (rows) * numMovies (cols)
# We can get this information from size of users and pages directories
isfile = os.path.isfile
join = os.path.join
directory = './users/'
numUsers = sum(1 for item in os.listdir(directory) if isfile(join(directory, item)))
directory = './pages/'
numMovies = sum(1 for item in os.listdir(directory) if isfile(join(directory, item)))
directory = './reviews/'
numReviews = sum(1 for item in os.listdir(directory) if isfile(join(directory, item)))
# Uncomment next block out for production
# numUsers = 100
# numMovies = 100
# numReviews = 100

userId_to_profileName = {}
userId_to_rowIndex = {}
movieId_to_colIndex = {}
rowIndex_to_userId = []
colIndex_to_movieId = []

# Now we can create our numpy arrays for rating and helpfulness
# Setting empty values to -1 so we can check with < 0
ratings = np.full((numUsers, numMovies), -1, dtype="float16")
helpfuls = np.full((numUsers, numMovies), -1, dtype="float16")

i = 0
for fn in os.listdir(directory):
    if fn.endswith(".html"):
        with open(directory + fn, 'r') as f:
            i += 1
            soup = BeautifulSoup(f.read(), features='html5lib')
            movieId = soup.title.string
            for tag in soup.find_all("meta"):
                # Just use the score from metadata
                if tag.get("name", None) == "score":
                    rating = tag.get("content", None)
                elif tag.get("name", None) == "userId":
                    userId = tag.get("content", None)
                # TODO: Get screen name from broken HTML ("""" problems)
                elif tag.get("name", None) == "profileName":
                    profileName = tag.get("content", None)
                elif tag.get("name", None) == "helpfulness":
                    helpfulness = tag.get("content", None)

            # We have all the information. Put it in the right places.
            if userId in userId_to_rowIndex:
                userIdx = userId_to_rowIndex[userId]
            else:
                userIdx = len(rowIndex_to_userId)
                userId_to_rowIndex[userId] = userIdx
                rowIndex_to_userId.append(userId)
                userId_to_profileName[userId] = profileName

            if movieId in movieId_to_colIndex:
                movieIdx = movieId_to_colIndex[movieId]
            else:
                movieIdx = len(colIndex_to_movieId)
                movieId_to_colIndex[movieId] = movieIdx
                colIndex_to_movieId.append(movieId)

            # Both ratings and helpfulness will be between 0 and 1
            ratings[userIdx, movieIdx] = (float(rating)-1)/4
            helpfulness = eval(helpfulness) if helpfulness[-1] != '0' else -1
            helpfuls[userIdx, movieIdx] = float(helpfulness)
            print("Parsed review", i, "of", numReviews, f'({float(i)*100/numReviews:.2f}% done)')

# Truncate empty space (in production, this shouldn't occur)
ratings = ratings[:len(rowIndex_to_userId), :len(colIndex_to_movieId)]
helpfuls = helpfuls[:len(rowIndex_to_userId), :len(colIndex_to_movieId)]

# Convert to Pandas so we can access by userId, movieId (vs indices)
ratingsFrame = pd.DataFrame(ratings, rowIndex_to_userId, colIndex_to_movieId)
helpfulsFrame = pd.DataFrame(helpfuls, rowIndex_to_userId, colIndex_to_movieId)

print("numUsers, numMovies:", ratings.shape)
print("numReviews:", numReviews)
print("density:", numReviews / (ratings.shape[0] * ratings.shape[1]))

print("\nratingsFrame")
print(ratingsFrame)
print("\nhelpfulsFrame")
print(helpfulsFrame)

# Save the ratings matrix, helpfuls matrix, and {userId: profileName}
print(f"\nSaving to files data_*.pkl")
ratingsFrame.to_pickle('data_ratingsFrame.pkl')
helpfulsFrame.to_pickle('data_helpfulsFrame.pkl')
with open('data_userId_to_profileName.pkl', 'wb') as f:
    pickle.dump(userId_to_profileName, f)
# No need to save anything related to rowIdx, colIdx. Labeled in DataFrame
# Rows are all ratings from a particular user
# Cols are all ratings for a particluar movie