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
helpfulsFrame = pd.read_pickle('data_helpfulsFrame.pkl')
with open('data_userId_to_profileName.pkl', 'rb') as f:
    userId_to_profileName = pickle.load(f)
with open('ratings_2d.pkl', 'rb') as f:
    ratings2d = pd.DataFrame(pickle.load(f), index=ratingsFrame.index)
with open('ratings_200d.pkl', 'rb') as f:
    ratings200d = pd.DataFrame(pickle.load(f), index=ratingsFrame.index)
with open('truncatedSvd_reconError_byK.pkl', 'rb') as f:
    svdResults = pickle.load(f)

def run_trials(points, startK=2, endK=10, label="K-means elbow finding"):
    results = []
    for k in range(startK, endK):
        km = KMeans(n_clusters=k, random_state=0).fit(points)
        results.append(km.inertia_)
    plt.plot(results)
    plt.title(label)
    plt.ylabel('Objective function')
    plt.xlabel('k-1 (add 1 to get k)')
    plt.show()
    return results

user_results_2d = run_trials(ratings2d, startK=1, endK=30, label="kmeans elbow finding: ratings2d")
user_results_200d = run_trials(ratings200d, startK=1, endK=30, label="kmeans elbow finding: ratings200d")

# Results generated with code above suggest that these n_clusters are appropriate
userAssignments2d = pd.DataFrame(KMeans(n_clusters=4).fit(ratings2d).labels_, index=ratingsFrame.index)
userAssignments2d.to_pickle('userAssignments2d.pkl')
userAssignments200d = pd.DataFrame(KMeans(n_clusters=4).fit(ratings200d).labels_, index=ratingsFrame.index)
userAssignments200d.to_pickle('userAssignments200d.pkl')

# Plot user assignments
userAssignments = userAssignments2d # Use either 2d or 200d assignments
x, y = np.array(ratings2d.loc[:, 0]), np.array(ratings2d.loc[:, 1])
labels = np.array(userAssignments.loc[:,0])
df = pd.DataFrame(dict(x=x, y=y, label=labels))
groups = df.groupby('label')
fig, ax = plt.subplots()
ax.margins(0.05)
for name, group in groups:
    ax.plot(group.x, group.y, marker='o', linestyle='', ms=5, label=name, alpha=0.2)

plt.title("Users clusters: 2d SVD")
ax.legend(numpoints=1, loc='lower left')
plt.show()

