from movie_helpers import recommendMovie, to_stars

def basePage(title, content):
  return f"""
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>{title}</title>

        <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin>

        <style media="screen">
          body {{
            margin: 0;
            font-family: Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol";
            line-height: 1.5;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
          }}
        </style>
      </head>
      <body>
        {content}
      </body>
    </html>"""
    
def buildPageUrl(pageName):
  return f"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/{pageName}.html"

def pageWithAds(pageName, advertisingContent):
  return f"""
    <style media="screen">
      #content {{
        display: flex;
        flex-direction: row;
      }}

      #page, #advertising {{
        width: 50vw;
      }}

      #advertising {{
        margin: 15px 25px;
      }}

      iframe {{
        width: 100%;
        height: 100%;
      }}
    </style>
    <div id="content">
      <div id="page">
        <iframe frameBorder="0" src="{buildPageUrl(pageName)}"></iframe>
      </div>
      <div id="advertising">{advertisingContent}</div>
    </div>"""

def genAdvertising(movieId, userId, userAssignments, movieAssignments, communityRatings, communityRecs):
  userCommunity = userAssignments.loc[userId, 0]
  movieTopic = movieAssignments[movieId]
  recommendedMovieId = recommendMovie(movieTopic, userCommunity, communityRecs, movieAssignments)
  return f"""
    <h1>Custom advertisement for movie {movieId} and user {userId}</h1>
    <p>A random movie is pulled from a subset of movies determined by the following criteria:
    <ul>
      <li>Movie is rated better than community average by this user's community (community {userCommunity}).</li>
      <li>Movie has the same top topic as this movie (topic {movieTopic})</li>
    <ul>
    <p>Selected movie is <strong>{recommendedMovieId}</strong>, which is rated {to_stars(communityRatings.loc[userCommunity, recommendedMovieId]):.2f} stars by community {userCommunity}.</p>
    <p><a href="{buildPageUrl(recommendedMovieId)}">Read reviews for {recommendedMovieId}</a></p>
    """
