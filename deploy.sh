#!/bin/bash

# Deploy script to push project to openshift

# You need to setup following variables in your CI server :
#
#    OPENSHIFT_SERVER     - openshift server, ex: openshift.redhat.com
#    OPENSHIFT_ACCOUNT    - User account
#    OPENSHIFT_PASSWORD   - User password
#    OPENSHIFT_APPNAME    - Project Name in openshift

# exit with nonzero exit code if anything fails
set -e

# Check variables
if [ -z $OPENSHIFT_APPNAME ]; then
  echo "OPENSHIFT_APPNAME variable cannot be empty"
  exit 1
fi
if [ -z $OPENSHIFT_SERVER ]; then
  echo "OPENSHIFT_SERVER variable cannot be empty"
  exit 1
fi
if [ -z $OPENSHIFT_ACCOUNT ]; then
  echo "OPENSHIFT_ACCOUNT variable cannot be empty"
  exit 1
fi
if [ -z $OPENSHIFT_PASSWORD ]; then
  echo "OPENSHIFT_PASSWORD variable cannot be empty"
  exit 1
fi

# Login to openshift
# Q1: Generate a token now? (yes|no) no
# Q2: Upload ssh key now? (yes|no) yes
# Q3: Provide a name for this key: cikey

echo -e "no\nyes\ncikey" | rhc setup --server ${OPENSHIFT_SERVER}  -l ${OPENSHIFT_ACCOUNT} -p ${OPENSHIFT_PASSWORD} > /dev/null 2>&1

# deploy
# Q1: ask for password: $OPENSHIFT_PASSWORD
echo -e "$OPENSHIFT_PASSWORD" | rhc app-deploy master -a ${OPENSHIFT_APPNAME}

# After all done, leave openshift
rhc logout

# kill the ssh key when in CI
if [ !  -z $CI ]; then
    rm -rf ~/.ssh
fi