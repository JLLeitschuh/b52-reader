# b52-reader
Fast reader for website articles with good support for searching.

## Goals
The main goals of the b52-reader are to support anyone who wants to read articles from one or more websites and:
- have a fast experience because multiple articles are already fetched by the b52-reader, trying to predict which articles you want to read next;
- provide good support for searching on keywords, author, publication date;
- give multiple ways of sorting your list of articles;
- keep track of the articles that you have read.

Additional goals that could be achieved at a later stage are:
- give some sort of notification when a new article is published that seems to be important or interesting (for example based on keywords and author);
- make the experience at least as smooth as using the website directly, so no additional login requests etc.

## Technology
Java 8 desktop application using the Swing library for the GUI and either [JxBrowser](https://www.teamdev.com/jxbrowser) or [Java-CEF](https://bitbucket.org/chromiumembedded/java-cef) for adding Chromium-based browsers. In principle support Windows, Linux, and macOS (although my personal focus is on Windows and I don't have a macOS test device).
