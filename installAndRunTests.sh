#!/bin/bash
sudo apt-get -y install openjdk*
sudo apt-get -y install maven
sudo dpkg -i --force-all /var/cache/apt/archives/libwagon2-java_2.2-3+nmu1_all.deb
sudo apt-get -y install git
git clone https://github.com/jsebrien/elastic-search-tests.git
cd elastic-search-tests
mvn test