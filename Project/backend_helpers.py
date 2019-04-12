#@title Setup libs
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
from math import ceil, sqrt
import matplotlib.pyplot as plt
from PIL import Image
import csv
import io
import urllib.request
import pqdict
import networkx as nx
import tweepy
import pickle
import numpy as np
import re
from googletrans import Translator
# % matplotlib inline

def get_tweets(screen_name, num_tweets=200, save=False):

    def remove_pattern(input_txt, pattern):
        r = re.findall(pattern, input_txt)
        for i in r:
            input_txt = re.sub(i, '', input_txt)        
        return input_txt

    def clean_tweets(lst):
        # remove twitter Return handles (RT @xxx:)
        lst = np.vectorize(remove_pattern)(lst, "RT @[\w]*:")
        # remove twitter handles (@xxx)
        lst = np.vectorize(remove_pattern)(lst, "@[\w]*")
        # remove URL links (httpxxx)
        lst = np.vectorize(remove_pattern)(lst, "https?://[A-Za-z0-9./]*")
        # remove special characters, numbers, punctuations (except for #)
        lst = np.core.defchararray.replace(lst, "[^a-zA-Z#]", " ")
        return lst
        
    # initialize a list to hold all the tweepy Tweets
    alltweets = []

    # make initial request for most recent tweets (200 is the maximum allowed count)
    # We assume twitter API keys already set
    new_tweets = api.user_timeline(screen_name=screen_name, count=200)

    # save most recent tweets
    alltweets.extend(new_tweets)

    # save the id of the oldest tweet less one
    oldest = alltweets[-1].id - 1

    # keep grabbing tweets until there are no tweets left to grab
    while len(new_tweets) > 0 and num_tweets > 1:
        num_tweets -= 200
        print("getting tweets before %s" % (oldest))

        # all subsiquent requests use the max_id param to prevent duplicates
        new_tweets = api.user_timeline(
            screen_name=screen_name, count=200, max_id=oldest)

        # save most recent tweets
        alltweets.extend(new_tweets)

        # update the id of the oldest tweet less one
        oldest = alltweets[-1].id - 1

        print("...%s tweets downloaded so far" % (len(alltweets)))

    createTimes = []
    tweetsText = []
    # transform the tweepy tweets into a 2D array that will populate the csv
    for tweet in alltweets:
        createTimes.append(tweet.created_at)
        # tweetsText.append(tweet.text)
        tweetsText.append(tweet.text.encode('ascii', 'xmlcharrefreplace').decode('utf-8'))

    # Clean the tweet text
    tweetsText = clean_tweets(tweetsText)

    rows = zip(createTimes, tweetsText)

    # write the csv
    if save:
        with open('%s_tweets.csv' % screen_name, 'w') as f:
            writer = csv.writer(f)
            writer.writerow(["created_at", "text"])
            writer.writerows(rows)

    return createTimes, tweetsText


class Crawler(object):
    def __init__(self, seed):
        # the larger the more important (reverse=True)
        self.crawl_frontier = pqdict.pqdict(
            {user_id: 0 for user_id in seed}, reverse=True)
        self.visited = []
        self.graph = nx.DiGraph()

    def print(self):
        print('#{nodes}: %d' % self.graph.number_of_nodes())
        print('#{edges}: %d' % self.graph.number_of_edges())
        print('visited: %d' % len(self.visited))

    def crawl(self, api, update_interval=5, max_itr=15, max_width=5):
        cnt = 0
        api.wait_on_rate_limit = True
        while cnt < max_itr:
            if not self.crawl_frontier:
                break

            user_id = self.crawl_frontier.pop()
            if user_id in self.visited:
                continue
            self.visited.append(user_id)

            status = api.get_user(user_id)
            if status.protected:
                continue
            print('user_id: %d, screen_name: %s' %
                  (user_id, status.screen_name))

            # Friends ids gives the opposite direction of graph: those the seed user follows
#             friends = api.friends_ids(user_id)
            followers = api.followers_ids(user_id)
            self.graph.add_edges_from([(follower, user_id)
                                       for follower in followers])
            self.crawl_frontier.update(
                {follower: -1 for follower in followers if follower not in self.crawl_frontier})

            cnt += 1

            if cnt % update_interval == 0:
                ranks = nx.pagerank(self.graph)
                for key in self.crawl_frontier.keys():
                    if key in ranks:
                        self.crawl_frontier[key] = ranks[key]
#                 ranked = list(sorted(ranks, key=ranks.get, reverse=True))
#                 self.graph.remove_nodes_from(ranked[max_width:])


api = None
translator = None
analyser = None

# Setup tweepy, translator, and crawler
def setup_services(
    consumer_key='qVID8YdMZes0zrMXLgPiPZL2K',
    consumer_secret='zOqOFGjEd4jp0d1mmonQMJTBrSMmIDQ3kjiPJhTV1m6E1b6pmS',
    access_token_key='1115993772356243462-G1q8Pb1YVTpHfWJCmRuFSN5PaLtbKM',
    access_token_secret='ntKOCJ5g88MjytKDEClW9lmc4mSjh1rOsJw93cXxtjpqU'
):
    global api
    global translator
    global analyser
    auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
    auth.set_access_token(access_token_key, access_token_secret)
    api = tweepy.API(auth)
    translator = Translator()
    analyser = SentimentIntensityAnalyzer()


def sentiment(text, translate=True):
    if translate:
        text = translator.translate(text).text
    return analyser.polarity_scores(text)['compound']


def load_crawler(fn='crawler.bin'):
    with open(fn, 'rb') as fp:
        return api, pickle.loads(fp.read())


def save_crawler(crawler, fn='crawler.bin'):
    with open(fn, 'wb') as fp:
        fp.write(pickle.dumps(crawler))


def new_crawler(screen_names):
    seed = [user.id for user in api.lookup_users(screen_names=screen_names)]
    return api, Crawler(seed=seed)



def plots(crawler, show_images=True, show_graph=True, layout='spring', num_users=50):
    # Limit visualization to only popular users
    graph = crawler.graph
    ranks = nx.pagerank(graph)
    ranked = list(sorted(ranks, key=ranks.get, reverse=True))
    graph.remove_nodes_from(ranked[num_users:])

    # Show screen names and images
    for user_id in graph.nodes():
        status = api.get_user(user_id)
        graph.node[user_id]['screen_name'] = status.screen_name
        file = io.BytesIO(urllib.request.urlopen(status.profile_image_url).read())
        graph.node[user_id]['image']= Image.open(file)

    if show_images:
        gridSize = ceil(sqrt(num_users))
        plt.subplots(gridSize, gridSize, figsize=(gridSize, gridSize))
        cnt = 1
        for user_id in graph.nodes():
            plt.subplot(gridSize, gridSize, cnt)
            plt.title(graph.node[user_id]['screen_name'], fontsize=12, fontweight='bold')
            plt.imshow(graph.node[user_id]['image'])
            cnt += 1
        plt.show()

    if show_graph:
        if layout == 'spring':
            pos = nx.spring_layout(graph) # GOOD
        if layout == 'circular':
            pos = nx.circular_layout(graph)
        if layout == 'shell':
            pos = nx.shell_layout(graph)
        if layout == 'spectral':
            pos = nx.spectral_layout(graph)
        else:
            pos = nx.random_layout(graph)

        fig = plt.figure(figsize=(10,10))
        ax = plt.subplot(1, 1, 1)
        nx.draw_networkx_edges(graph, pos, ax=ax, arrows=True, zorder=10)

        trans_data = ax.transData.transform
        trans_figure_inv = fig.transFigure.inverted().transform

        size = 0.05
        for user_id in graph.nodes():
            x, y = trans_figure_inv(trans_data(pos[user_id]))
            rect = [x - size/2, y - size/2, size, size]
            ax_image = plt.axes(rect)
            ax_image.imshow(graph.node[user_id]['image'], zorder=1)
            ax_image.axis('off')
            
        plt.show()