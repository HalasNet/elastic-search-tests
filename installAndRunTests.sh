#!/bin/bash
sudo apt-get -y install openjdk*
sudo apt-get -y install maven
sudo dpkg -i --force-all /var/cache/apt/archives/libwagon2-java_2.2-3+nmu1_all.deb
sudo apt-get -y install git
git clone https://github.com/jsebrien/elastic-search-tests.git
cd elastic-search-tests
mvn package
echo "Press Enter to start GeoSearchTest (this will take a few seconds)"
read
mvn test > geo-search-test.log
echo "Test Done! Press Enter to see tests execution log"
read
more geo-search-test.log
