from backend_helpers import *

# Instatiate Tweepy, Google Translate, and Vader as api, translator, analyser
setup_services(
    consumer_key = 'qVID8YdMZes0zrMXLgPiPZL2K',
    consumer_secret = 'zOqOFGjEd4jp0d1mmonQMJTBrSMmIDQ3kjiPJhTV1m6E1b6pmS',
    access_token_key = '1115993772356243462-G1q8Pb1YVTpHfWJCmRuFSN5PaLtbKM',
    access_token_secret = 'ntKOCJ5g88MjytKDEClW9lmc4mSjh1rOsJw93cXxtjpqU'
)

# Analyze sentiment
print(sentiment("je suis tres bien!!"))
print(sentiment("i am very good!!"))

# Build graph by crawling from seed users

# api, crawler = new_crawler(['cerealnac'])
crawler.crawl(api=api, update_interval=5, max_itr=50, max_width=5)
# print('visited: %d' % len(crawler.visited))
save_crawler(crawler, fn='crawler.bin')

# api, crawler = load_crawler(fn='crawler.bin')
crawler.print()

# Show graphs
plots(crawler, show_images=True, show_graph=True, layout='spring', num_users=100)

# Get tweets
bieberTimes, bieberTweets = get_tweets('justinbieber', 200, save=True)
print(bieberTweets)