/**
 * Bare-bones single page node express server
 * 
 * Run command: node server.js
 * Default port is 5000
 */

'use strict';

const http = require("http"),
  pathUtils = require("path"),
  express = require("express"),
  app = express(),
  PORT = process.env.PORT || 5000,
  appDir = pathUtils.resolve(__dirname, "public");

app.use(express.static(appDir));

app.get("*", function (req, res) {
  res.sendFile(pathUtils.resolve(appDir, "index.html"));
});

http.createServer(app).listen(PORT, function () {
  console.log("Express server listening on port " + PORT);
  console.log("http://localhost:" + PORT);
});