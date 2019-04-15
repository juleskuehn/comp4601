var w = window.innerWidth;
var h = window.innerHeight;

var keyc = true, keys = true, keyt = true, keyr = true, keyx = true, keyd = true, keyl = true, keym = true, keyh = true, key1 = true, key2 = true, key3 = true, key0 = true

var focus_node = null, highlight_node = null;

//var text_center = false;
var outline = false;

var min_sentiment = -1;
var max_sentiment = 1;

var color = d3.scale.linear()
  .domain([min_sentiment, (min_sentiment + max_sentiment) / 2, max_sentiment])
  .range(["red", "yellow", "lime"]);

var highlight_color = "blue";
var highlight_trans = 0.1;

var size = d3.scale.pow().exponent(1)
  .domain([1, 100])
  .range([8, 24]);

var radius = d3.scale.sqrt()
  .range([0, 6]);

Number.prototype.map = function (in_min, in_max, out_min, out_max) {
  if (this > in_max) return out_max;
  if (this < in_min) return out_min;
  return (this - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}


let defaultSpeed = 50;
let transitionDuration = defaultSpeed.map(1, 100, 1000, 10);

var force = d3.layout.force()
  .size([w, h])
  .charge(-400)
  .linkDistance(function (d) { return radius(d.source.size) + radius(d.target.size) + 20; });

var default_node_color = "#ccc";
//var default_node_color = "rgb(3,190,100)";
var default_link_color = "#888";
var nominal_base_node_size = 8;
var nominal_text_size = 10;
var max_text_size = 24;
var nominal_stroke = 1.5;
var max_stroke = 4.5;
var max_base_node_size = 36;
var min_zoom = 0.1;
var max_zoom = 7;
var svg = d3.select("body").append("svg");
var zoom = d3.behavior.zoom().scaleExtent([min_zoom, max_zoom])
var g = svg.append("g");
svg.style("cursor", "move");

svg.append("svg:defs").selectAll("marker")
  .data(["end"])      // Different link/path types can be defined here
  .enter().append("svg:marker")    // This section adds in the arrows
  .attr("id", String)
  .attr("viewBox", "0 -5 10 10")
  .attr("refX", 22)
  .attr("refY", 0)
  .attr("markerWidth", 5)
  .attr("markerHeight", 5)
  .attr("orient", "auto")
  .append("svg:path")
  .attr("d", "M0,-5L10,0L0,5");

d3.json("graph.json", function (error, graph) {

  d3.json("rolling_averages_2week.json", (error, averages) => {

    // Compute array of time windows
    const timeWindows = [];
    for (const [timestamp, value] of Object.entries(averages)) {
      timeWindows.push({ timestamp, ...value });
    }

    // Sort by increasing timestamp
    timeWindows.sort((value) => value.timestamp);

    // Compute links
    const links = [];
    for (const node of graph.nodes) {
      for (const follower of node.followers) {
        links.push({ source: follower, target: node.id });
      }
    }

    var nodeById = d3.map();

    graph.nodes.forEach(function (node) {
      nodeById.set(node.id, node);
      node.size = node.followers.length.map(0, 100, 8, 20);
    });

    links.forEach(function (link) {
      link.source = nodeById.get(link.source);
      link.target = nodeById.get(link.target);
    });

    var linkedByIndex = {};
    links.forEach(function (d) {
      linkedByIndex[d.source + ":" + d.target] = true;
    });

    function isConnected(a, b) {
      return linkedByIndex[a.id + ":" + b.id] || linkedByIndex[b.id + ":" + a.id] || a.id === b.id;
    }

    function hasConnections(a) {
      for (var property in linkedByIndex) {
        var s = property.split(":");
        if ((s[0] === a.id || s[1] === a.id) && linkedByIndex[property]) return true;
      }
      return false;
    }

    force
      .nodes(graph.nodes)
      .links(links)
      .start();

    var link = g.selectAll(".link")
      .data(links)
      .enter().append("line")
      .attr("class", "link")
      //.attr("marker-end", "url(#end)")
      .style("stroke-width", nominal_stroke)
      .style("stroke", function (d) {
        if (d.sentiment === 0.0) return default_node_color;
        else if (isNumber(d.sentiment) && d.sentiment >= -1 && d.sentiment <= 1) return color(d.sentiment);
        else return default_link_color;
      })

    var node = g.selectAll(".node")
      .data(graph.nodes)
      .enter().append("g")
      .attr("class", "node")
      .call(force.drag)

    node.on("dblclick.zoom", function (d) {
      d3.event.stopPropagation();
      var dcx = (window.innerWidth / 2 - d.x * zoom.scale());
      var dcy = (window.innerHeight / 2 - d.y * zoom.scale());
      zoom.translate([dcx, dcy]);
      g.attr("transform", "translate(" + dcx + "," + dcy + ")scale(" + zoom.scale() + ")");
    });

    var tocolor = "fill";
    var towhite = "stroke";
    if (outline) {
      tocolor = "stroke"
      towhite = "fill"
    }

    var circle = node.append("path")
      .attr("d", d3.svg.symbol()
        .size(function (d) { return Math.PI * Math.pow(nominal_base_node_size, 2); })
        .type("circle"))

      .style(tocolor, function (d) {
        if (d.sentiment === 0.0) return default_node_color;
        else if (isNumber(d.sentiment) && d.sentiment >= -1 && d.sentiment <= 1) return color(d.sentiment);
        else return default_node_color;
      })
      //.attr("r", function(d) { return size(d.size)||nominal_base_node_size; })
      .style("stroke-width", nominal_stroke)
      .style(towhite, "white");
    
    
    
    let currentTimeWindowIndex;
    
    // Setup date display and range slider
    const dateSlider = document.getElementById('date-slider');
    const dateDisplay = document.getElementById('date-display');
    const speedInput = document.getElementById('speed');

    dateSlider.max = timeWindows.length - 1;
    speedInput.value = defaultSpeed;

    const setDateInput = (dateIndex) => {
      const date = new Date(parseInt(timeWindows[dateIndex].timestamp * 1000));
      dateDisplay.textContent = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
    };

    const setSliderValue = (value) => {
      dateSlider.value = value;
    };

    dateSlider.addEventListener('input', (e) => {
      setTimeWindow(e.target.value);
    });

    speedInput.addEventListener('input', (e) => {
      const speed = parseInt(e.target.value);
      if (speed !== NaN && speed >= 1 && speed <= 100) {
        transitionDuration = parseInt(e.target.value).map(1, 100, 1000, 10);
      }
    });

    
    // Animation playback
    const setCircleColors = (timeWindowIndex) => {
      circle
        .transition()
        .duration(transitionDuration)
        .style("fill", (o) => {
          const sentiment = timeWindows[timeWindowIndex][o.id];
          if (!isValidSentiment(sentiment)) {
            return colorFromSentiment(o.sentiment);
          }
          o.sentiment = sentiment; // Maybe change
          return colorFromSentiment(sentiment);
        });
    };

    const setTimeWindow = (index) => {
      const _index = parseInt(index);
      currentTimeWindowIndex = _index;

      // Side effects
      setCircleColors(_index);
      setSliderValue(_index);
      setDateInput(_index);
    }

    let playing = false;
    let timeoutPromise = null;
    setTimeWindow(0);
    document.getElementById('playpause').addEventListener('click', async () => {
      if (playing) {
        playing = false;
        timeoutPromise && timeoutPromise.cancel();
        return;
      }
      playing = true;
      if (currentTimeWindowIndex === timeWindows.length - 1) {
        setTimeWindow(0);
      }
      while (playing) {
        if (currentTimeWindowIndex === timeWindows.length - 1) {
          playing = false;
          break;
        }
        setTimeWindow(currentTimeWindowIndex + 1);
        timeoutPromise = timeout(transitionDuration);
        await timeoutPromise;
      }
    });



    // var text = g.selectAll(".text")
    //   .data(graph.nodes)
    //   .enter().append("text")
    //   .attr("dy", ".35em")
    //   .style("font-size", nominal_text_size + "px")

    // if (text_center)
    //   text.text(function (d) { return d.id; })
    //     .style("text-anchor", "middle");
    // else
    //   text.attr("dx", function (d) { return (size(d.size) || nominal_base_node_size); })
    //     .text(function (d) { return '\u2002' + d.id; });

    node.on("mouseover", function (d) {
      set_highlight(d);
    })
      .on("mousedown", function (d) {
        d3.event.stopPropagation();
        focus_node = d;
        set_focus(d)
        if (highlight_node === null) set_highlight(d)

      }).on("mouseout", function (d) {
        exit_highlight();
      });

    d3.select(window).on("mouseup",
      function () {
        if (focus_node !== null) {
          focus_node = null;
          if (highlight_trans < 1) {

            circle.style("opacity", 1);
            //text.style("opacity", 1);
            link.style("opacity", 1);
          }
        }

        if (highlight_node === null) exit_highlight();
      });

    function exit_highlight() {
      highlight_node = null;
      if (focus_node === null) {
        svg.style("cursor", "move");
        if (highlight_color != "white") {
          circle.style(towhite, "white");
          //text.style("font-weight", "normal");
          link.style("stroke", function (o) {
            return colorFromSentiment(o.sentiment);
          });
        }
      }
    }

    function isValidSentiment(sentiment) {
      return sentiment && isNumber(sentiment) && sentiment >= -1 && sentiment <= 1;
    }

    function colorFromSentiment(sentiment) {
      if (sentiment === 0.0) return default_node_color;
      return isValidSentiment(sentiment) ? color(sentiment) : default_link_color
    }

    function set_focus(d) {
      if (highlight_trans < 1) {
        circle.style("opacity", function (o) {
          return isConnected(d, o) ? 1 : highlight_trans;
        });

        // text.style("opacity", function (o) {
        //   return isConnected(d, o) ? 1 : highlight_trans;
        // });

        link.style("opacity", function (o) {
          return o.source.id === d.id || o.target.id === d.id ? 1 : highlight_trans;
        });
      }
    }


    function set_highlight(d) {
      svg.style("cursor", "pointer");
      if (focus_node !== null) d = focus_node;
      highlight_node = d;

      if (highlight_color != "white") {
        circle.style(towhite, function (o) {
          return isConnected(d, o) ? highlight_color : "white";
        });
        // text.style("font-weight", function (o) {
        //   return isConnected(d, o) ? "bold" : "normal";
        // });
        link.style("stroke", function (o) {
          return o.source.id === d.id || o.target.id === d.id ? highlight_color : o.sentiment === 0.0 ? default_node_color : ((isNumber(o.sentiment) && o.sentiment >= -1 && o.sentiment <= 1) ? color(o.sentiment) : default_link_color);
        });
      }
    }


    zoom.on("zoom", function () {
      var stroke = nominal_stroke;
      if (nominal_stroke * zoom.scale() > max_stroke) stroke = max_stroke / zoom.scale();
      link.style("stroke-width", stroke);
      circle.style("stroke-width", stroke);

      var base_radius = nominal_base_node_size;
      if (nominal_base_node_size * zoom.scale() > max_base_node_size) base_radius = max_base_node_size / zoom.scale();
      circle.attr("d", d3.svg.symbol()
        .size(function (d) { return Math.PI * Math.pow(size(d.size) * base_radius / nominal_base_node_size || base_radius, 2); })
        .type("circle"))

      //if (!text_center) text.attr("dx", function (d) { return (size(d.size) * base_radius / nominal_base_node_size || base_radius); });

      // var text_size = nominal_text_size;
      // if (nominal_text_size * zoom.scale() > max_text_size) text_size = max_text_size / zoom.scale();
      // text.style("font-size", text_size + "px");

      g.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    });

    svg.call(zoom);

    resize();
    //window.focus();
    d3.select(window).on("resize", resize).on("keydown", keydown);

    force.on("tick", function () {

      node.attr("transform", function (d) { return "translate(" + d.x + "," + d.y + ")"; });
      //text.attr("transform", function (d) { return "translate(" + d.x + "," + d.y + ")"; });

      link.attr("x1", function (d) { return d.source.x; })
        .attr("y1", function (d) { return d.source.y; })
        .attr("x2", function (d) { return d.target.x; })
        .attr("y2", function (d) { return d.target.y; });

      node.attr("cx", function (d) { return d.x; })
        .attr("cy", function (d) { return d.y; });
    });

    function resize() {
      var width = window.innerWidth, height = window.innerHeight;
      svg.attr("width", width).attr("height", height);

      force.size([force.size()[0] + (width - w) / zoom.scale(), force.size()[1] + (height - h) / zoom.scale()]).resume();
      w = width;
      h = height;
    }

    function keydown() {
      if (d3.event.keyCode == 32) { force.stop(); }
      else if (d3.event.keyCode >= 48 && d3.event.keyCode <= 90 && !d3.event.ctrlKey && !d3.event.altKey && !d3.event.metaKey) {
        switch (String.fromCharCode(d3.event.keyCode)) {
          case "C": keyc = !keyc; break;
          case "S": keys = !keys; break;
          case "T": keyt = !keyt; break;
          case "R": keyr = !keyr; break;
          case "X": keyx = !keyx; break;
          case "D": keyd = !keyd; break;
          case "L": keyl = !keyl; break;
          case "M": keym = !keym; break;
          case "H": keyh = !keyh; break;
          case "1": key1 = !key1; break;
          case "2": key2 = !key2; break;
          case "3": key3 = !key3; break;
          case "0": key0 = !key0; break;
        }

        // link.style("display", function (d) {
        //   var flag = vis_by_node_sentiment(d.source.sentiment) && vis_by_node_sentiment(d.target.sentiment);
        //   linkedByIndex[d.source.id + ":" + d.target.id] = flag;
        //   return flag ? "inline" : "none";
        // });
        // node.style("display", function (d) {
        //   return (key0 || hasConnections(d)) && vis_by_node_sentiment(d.sentiment) ? "inline" : "none";
        // });
        // // text.style("display", function (d) {
        // //   return (key0 || hasConnections(d)) && vis_by_node_sentiment(d.sentiment) ? "inline" : "none";
        // // });

        // if (highlight_node !== null) {
        //   if ((key0 || hasConnections(highlight_node)) && vis_by_node_sentiment(highlight_node.sentiment)) {
        //     if (focus_node !== null) set_focus(focus_node);
        //     set_highlight(highlight_node);
        //   }
        //   else { exit_highlight(); }
        // }

      }
    }

  });
});

function vis_by_node_sentiment(sentiment) {
  if (isNumber(sentiment)) {
    if (keyl) return sentiment > 0.0;
    if (keym) return sentiment < 0.0;
    if (keyh) return sentiment === 0.0;
  }
  return true;
}

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

/**
 * Promise wrapper around setTimeout
 * @param {Number} millis - Milliseconds to wait
 */
export const timeout = (millis) => {
  let id = null;
  const timeoutPromise = new Promise((resolve) => {
    id = setTimeout(resolve, millis);
  });
  timeoutPromise.id = id;
  timeoutPromise.cancel = () => {
    clearTimeout(timeoutPromise.id);
  };
  return timeoutPromise;
}

/**
 * Promise wrapper around requestAnimationFrame
 */
export const animationFrame = () => {
  return new Promise((resolve) => {
    requestAnimationFrame(resolve);
  });
}