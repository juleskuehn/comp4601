Graph is currently using data from:
nodes_user_avg_nonzero.json

The full range of data was used to generate these user averages, but only on non-zero sentiments.


rolling_averages.json should be used for animation.

Keys are the timestamp of the start time of the window.
Values are {user_id: avg_sentiment_for_this_window}

Where the node ID exists in keyset for a particular window, update the value (color) of that node. Otherwise, keep it the same.