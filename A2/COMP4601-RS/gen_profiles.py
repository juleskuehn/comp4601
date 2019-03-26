import numpy as np
import pandas as pd
import pickle 

print(f"\nLoading from data_*.pkl")
ratingsFrame = pd.read_pickle('data_ratingsFrame.pkl')
helpfulsFrame = pd.read_pickle('data_helpfulsFrame.pkl')
with open('data_userId_to_profileName.pkl', 'rb') as f:
    userId_to_profileName = pickle.load(f)

print("loaded ratingsFrame (a pandas DataFrame [row:user, col:movie])")
print("loaded helpfulsFrame (a pandas DataFrame corresponding to ratingsFrame)")
print("loaded userId_to_profileName (dict {userId: profileName})")