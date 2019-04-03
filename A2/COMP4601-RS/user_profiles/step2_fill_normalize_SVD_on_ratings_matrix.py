#!/usr/bin/env python

import numpy as np
import pandas as pd
import pickle
# from sklearn.decomposition import TruncatedSVD
import scipy as sp
from numpy import array
from sklearn.decomposition import TruncatedSVD
from scipy.linalg import norm
from movie_helpers import *
from collections import defaultdict

print(f"\nLoading from data_*.pkl")
ratingsFrame = pd.read_pickle('data_ratingsFrame.pkl')
helpfulsFrame = pd.read_pickle('data_helpfulsFrame.pkl')
with open('data_userId_to_profileName.pkl', 'rb') as f:
    userId_to_profileName = pickle.load(f)

print("loaded ratingsFrame (a pandas DataFrame [row:user, col:movie])")
print("loaded helpfulsFrame (a pandas DataFrame corresponding to ratingsFrame)")
print("loaded userId_to_profileName (dict {userId: profileName})")

print("\nratingsFrame")
print(ratingsFrame)
print("\nhelpfulsFrame")
print(helpfulsFrame)
print("\n userId_to_profileName")
print(len(userId_to_profileName))

userAvgRatings, userAvgHelpfuls = calc_userAvgs(ratingsFrame, helpfulsFrame, userId_to_profileName, show=False)
with open('userAvgRatings.pkl', 'wb') as f:
    pickle.dump(userAvgRatings, f)
with open('userAvgHelpfuls.pkl', 'wb') as f:
    pickle.dump(userAvgHelpfuls, f)

# Verify against checking page manually - verified
print([to_stars(rating) for rating in ratingsFrame.loc['A27W5AJNP6YX7Z'] if rating >= 0])
print(userAvgRatings['A27W5AJNP6YX7Z'])

ratings64file = open('ratings_filledNormalized64.pkl', 'rb')
if ratings64file == None:
    ratings64file.close()
    # Fill and normalize matrix before performing SVD
    # This reduces reconstruction error from 29.18% to 23.68% in 2d
    filledRatingsFrame = fillRatingsFrame(ratingsFrame, userAvgRatings)
    filledNormalizedRatingsFrame = normalizeRatingsFrame(ratingsFrame, userAvgRatings)
    ratings64 = np.array(filledNormalizedRatingsFrame, dtype='float64')
    with open('ratings_filledNormalized64.pkl', 'wb') as f:
        pickle.dump(ratings64, f)
else:
    ratings64 = pickle.load(ratings64file)
    ratings64file.close()

# Perform SVD and reduce dimensions with varying n_components
rfNorm = np.linalg.norm(ratings64)
svd_results = defaultdict(float)
for k in range(2, 101, 1):
    svd = TruncatedSVD(n_components=k)
    ratings2d = svd.fit_transform(ratings64)
    diff = np.linalg.norm(ratings64 - svd.inverse_transform(ratings2d))
    print("\n with n_components =", k)
    print("diff =", diff)
    err = diff*100 / rfNorm
    print(f"error = {err}%")
    svd_results[k] = err

# Save comparisons of error in reconstruction vs number of dimensions
with open('truncatedSvd_reconError_byK.pkl', 'wb') as f:
    pickle.dump(svd_results, f)

# Save 2 dimensional values (for clustering and visualizing)
svd = TruncatedSVD(n_components=2)
ratings2d = svd.fit_transform(ratings64)
with open('ratings_2d.pkl', 'wb') as f:
    pickle.dump(ratings2d, f)

# Save 200 dimensional values (for clustering)
svd = TruncatedSVD(n_components=200)
ratings200d = svd.fit_transform(ratings64)
print(ratings200d)
with open('ratings_200d.pkl', 'wb') as f:
    pickle.dump(ratings200d, f)
diff = np.linalg.norm(ratings64 - svd.inverse_transform(ratings200d))
print("\n with n_components =", 200)
print("diff =", diff)
err = diff*100 / rfNorm
print(f"error = {err}%")
with open('ratings_200d.pkl', 'wb') as f:
    pickle.dump(ratings200d, f)

