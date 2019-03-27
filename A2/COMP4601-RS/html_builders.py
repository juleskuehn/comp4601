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
    

loremIpsum = """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sodales fermentum enim quis tincidunt. Maecenas tempor tortor in risus mattis tincidunt. Ut luctus ipsum sit amet turpis lacinia, vel dictum risus congue. Fusce et vestibulum est. Maecenas scelerisque gravida nibh. Phasellus eu purus ex. Ut non lorem in ligula euismod efficitur. Maecenas vestibulum orci nec finibus tempor. Curabitur vulputate, quam ut commodo rutrum, lectus libero scelerisque mauris, eu porttitor nisl nunc sed elit. Nulla facilisi. Suspendisse ac sapien cursus, hendrerit neque vel, dictum mi. In eu eleifend tellus. Vivamus nec diam nunc. Vivamus fringilla, ex sed tempor pulvinar, purus ipsum faucibus ligula, eget dictum tortor mauris nec risus.

Donec est elit, pharetra id aliquet vitae, convallis vitae massa. Donec finibus mi quis auctor faucibus. Donec ac mattis magna. Nulla faucibus ultricies ullamcorper. Aliquam euismod aliquet tortor. Ut eget fringilla ligula. In id imperdiet ipsum. Sed condimentum erat sed nunc pulvinar, et malesuada magna ornare.

Integer quis turpis ligula. Aliquam egestas, dolor eu mattis ultricies, augue magna varius lacus, nec tristique sem lectus eget libero. Aliquam auctor vitae purus vel cursus. Duis id odio velit. Fusce et risus commodo, porta velit porttitor, feugiat erat. Quisque maximus, elit sit amet egestas feugiat, ex urna mollis turpis, sagittis ultricies metus quam a ligula. Pellentesque a ultricies magna, porta dapibus velit. Phasellus interdum viverra lacus, rutrum vehicula libero varius vel. Phasellus posuere lectus non blandit porta. Praesent elementum sodales faucibus. Nulla tellus dui, blandit at pellentesque id, sodales quis nisl. Nulla convallis, turpis egestas scelerisque tincidunt, ex risus vehicula mi, a placerat ligula sapien ac orci. Sed in mi non magna accumsan pretium. Aenean ac est tellus.

Quisque egestas et eros at facilisis. Cras posuere fermentum dui, sit amet rhoncus libero tincidunt id. Etiam non neque sapien. Morbi elementum sed erat vel malesuada. Suspendisse quis enim pellentesque, sagittis leo non, consequat orci. Aenean laoreet sodales justo. Nam vulputate, odio sit amet varius tincidunt, tellus ligula molestie quam, et porttitor ipsum elit pulvinar metus. Maecenas vel risus vel lorem ultrices faucibus. Mauris dapibus varius ultrices. Fusce fermentum scelerisque nulla a tristique. Aliquam enim mi, tempor et dolor quis, vestibulum venenatis ex.

Nam at tincidunt nibh, sed dapibus odio. Sed sed metus id purus varius auctor sed in lectus. Cras consequat quam eget ante dictum, a fermentum massa dictum. Sed gravida ac dolor at blandit. Vestibulum fermentum odio id elit feugiat, a viverra lectus mollis. In auctor ornare neque at mollis. Cras mollis felis quis est interdum, in eleifend ligula ultricies. Donec tempor sit amet ipsum sed fringilla. Vivamus nec enim in massa fermentum pharetra id id elit. Integer placerat eros ipsum, at rhoncus dolor hendrerit ut. In ut turpis libero. Phasellus tortor diam, congue sit amet libero at, sodales rhoncus nulla. Mauris condimentum bibendum nisl, non viverra dui. Sed quis sollicitudin ex. Curabitur ultrices luctus felis, sed euismod sapien egestas eget. Sed hendrerit maximus tellus at auctor."""
