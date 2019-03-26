import numpy as np
import pandas as pd
import pickle
from movie_helpers import *

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

# Get (and print) user stats
userAvgRatings = {}
userAvgHelpfuls = {}
for userId in userId_to_profileName:
    userAvgRatings[userId] = calc_userAvgRating(ratingsFrame, userId)
    userAvgHelpfuls[userId] = calc_userAvgHelpful(helpfulsFrame, userId)
    print(f'{userId:15} : {userId_to_profileName[userId]:30}'
        + f'    {to_stars(userAvgRatings[userId]):3.1f} star avg'
        + f'    {userAvgHelpfuls[userId]*100:3.0f}% helpful')

# Verify against checking page manually - verified
print([to_stars(rating) for rating in ratingsFrame.loc['A27W5AJNP6YX7Z'] if rating >= 0])
print(userAvgRatings['A27W5AJNP6YX7Z'])