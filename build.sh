#!/bin/bash

mvn install
sudo docker build . -t kprod/firewatch
