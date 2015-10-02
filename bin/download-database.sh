#!/usr/bin/env bash

heroku pg:backups capture
curl -o production.dump `heroku pg:backups public-url`
pg_restore --verbose --clean --no-acl --no-owner -h localhost -d ninjatools_dev production.dump
