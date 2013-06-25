Elastic search use cases
====================
This repo contains unit tests, describing ES real life use cases, using only the native JAVA API.

Installation prerequisites
-------

To run these tests, you need:
- JDK >=6
- Git
- Maven

Download the project
-------

git clone https://github.com/jsebrien/elastic-search-tests.git

Compile the project
-------

mvn package

Run tests
-------

mvn test

Use cases
-------

###  Geodistance search

751 cities are indexed, then perform a search to find cities far by 1000 km from Paris. 
Search results are showed in reversed order (the farthest displayed first).

The list of cities is stored in `src/test/resources/cities.txt`

Search parameters can be changed, using the following constants:

`NB_MAX_RESULTS` (default : 200)

`ORIGIN_CITY_LON` (default : 2.34 - Paris longitude)

`ORIGIN_CITY_LAT` (default : 48.86 - Paris lattitude)

`DISTANCE_FROM_ORIGIN` (default : 1000)

`DISTANCE_UNIT` (default : DistanceUnit.KILOMETERS)

###  Scan Search

728 ebooks are indexed, then perform a scan search for ebooks where the word "Biological" is part of their name.
The list of ebooks is stored in `src/test/resources/ebook2013.csv`

Search parameters can be changed, using the following constants:

`SCROLL_SIZE` (default : 5) - retrieve 5 items per request

`TEXT_TO_SEARCH` (default : "Biological")

Some code snippets are taken from tests provided by David Pilato (aka dadoonet):

https://github.com/elasticsearchfr/elasticsearch-java-tests
