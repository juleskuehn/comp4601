import numpy as np
import pandas as pd
import pickle
import scipy as sp
from numpy import array
import matplotlib.pyplot as plt
from movie_helpers import *
from collections import defaultdict
from sklearn.cluster import KMeans
import io
from PIL import Image

class User:
    def __init__(self, userId):
        self.userId = userId
        # TODO load data statically (and pass into this class)
        self.ratingsFrame = pd.read_pickle('user_profiles/data_ratingsFrame.pkl')
        self.helpfulsFrame = pd.read_pickle('user_profiles/data_helpfulsFrame.pkl')

        with open('user_profiles/data_userId_to_profileName.pkl', 'rb') as f:
            self.userId_to_profileName = pickle.load(f)
        with open('user_profiles/ratings_2d.pkl', 'rb') as f:
            self.ratings2d = pd.DataFrame(pickle.load(f), index=self.ratingsFrame.index)
        with open('user_profiles/ratings_200d.pkl', 'rb') as f:
            self.ratings200d = pd.DataFrame(pickle.load(f), index=self.ratingsFrame.index)
        with open('user_profiles/userAvgRatings.pkl', 'rb') as f:
            self.userAvgRatings = pickle.load(f)
        with open('user_profiles/userAvgHelpfuls.pkl', 'rb') as f:
            self.userAvgHelpfuls = pickle.load(f)
        with open('user_profiles/userAssignments2d.pkl', 'rb') as f:
            self.userAssignments2d = pickle.load(f)
        with open('user_profiles/userAssignments200d.pkl', 'rb') as f:
            self.userAssignments200d = pickle.load(f)
        # TODO get timestamps of reviews (from HTML) in DataFrame corresponding to ratingsFrame

    # Returns a PIL Image object which can be returned to the browser
    # https://stackoverflow.com/questions/8598673/how-to-save-a-pylab-figure-into-in-memory-file-which-can-be-read-into-pil-image
    # https://stackoverflow.com/questions/7877282/how-to-send-image-generated-by-pil-to-browser
    def plot_user_vs_clusters_2d(self):
        # Plot user assignments, highlighting this user
        self.userAssignments = self.userAssignments2d # Use either 2d or 200d assignments
        x, y = np.array(self.ratings2d.loc[:, 0]), np.array(self.ratings2d.loc[:, 1])
        labels = np.array(self.userAssignments.loc[:,0])
        df = pd.DataFrame(dict(x=x, y=y, label=labels))
        groups = df.groupby('label')
        plt.figure()
        fig, ax = plt.subplots()
        ax.margins(0.05)
        for name, group in groups:
            ax.plot(group.x, group.y, marker='o', linestyle='', ms=5, label=name, alpha=0.2)
        x, y = self.get_2d_loc()
        ax.plot([x], [y], marker='+', ms=30, color='white',
             markeredgewidth=10, markeredgecolor='black')
        plt.title(f"{self.get_profileName()} vs all users")
        ax.legend(numpoints=1, loc='lower left')
        buf = io.BytesIO()
        plt.savefig(buf, format='png')
        buf.seek(0)
        im = Image.open(buf)
        # buf.close()
        return im

    def get_profileName(self):
        return self.userId_to_profileName[self.userId]

    def get_avgRating(self):
        return self.userAvgRatings[self.userId]

    def get_avgHelpful(self):
        return self.userAvgHelpfuls[self.userId]

    def get_cluster(self):
        return self.userAssignments200d.loc[self.userId, 0]

    def get_2d_loc(self):
        return self.ratings2d.loc[self.userId, 0], self.ratings2d.loc[self.userId, 1]

    def __str__(self):
        return f'{self.userId:10} {self.get_profileName():10} {self.get_avgHelpful()*100:3.1f}% helpful {to_stars(self.get_avgRating()):3.1f} star average rating. Cluster {self.get_cluster()}'


if __name__ == '__main__':
    user = User('A27W5AJNP6YX7Z')
    user_PIL_Image = user.plot_user_vs_clusters_2d()
    user_PIL_Image.show()
    print(user)

    # Print all users
    with open('user_profiles/data_userId_to_profileName.pkl', 'rb') as f:
        userId_to_profileName = pickle.load(f)
        for userId in userId_to_profileName:
            print(User(userId))