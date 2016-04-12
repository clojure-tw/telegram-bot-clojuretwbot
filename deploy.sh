#!/bin/bash

# Deploy script to push project to openshift

# You need to setup following variables in your CI server :
#
#    OPENSHIFT_SERVER     - openshift server, ex: openshift.redhat.com
#    OPENSHIFT_ACCOUNT    - User account
#    OPENSHIFT_PASSWORD   - User password
#    OPENSHIFT_APPNAME    - Project Name in openshift
#    OPENSHIFT_REPO       - openshift repo path

# exit with nonzero exit code if anything fails
set -e

function die {
      : << FUNCDOC
This function is used to simple leave the script with error message.

parameter 1: error message

FUNCDOC

    echo -e "\n\033[31m\033[1mERROR: $1\033[0m\n"

    # Exit with error
    exit 1
}

# Check variables
[ $OPENSHIFT_APPNAME  ] || die "OPENSHIFT_APPNAME variable cannot be empty"
[ $OPENSHIFT_SERVER   ] || die "OPENSHIFT_SERVER variable cannot be empty"
[ $OPENSHIFT_ACCOUNT  ] || die "OPENSHIFT_ACCOUNT variable cannot be empty"
[ $OPENSHIFT_PASSWORD ] || die "OPENSHIFT_PASSWORD variable cannot be empty"
[ $OPENSHIFT_REPO     ] || die "OPENSHIFT_REPO variable cannot be empty"

# Make sure rhc command exist
[ $(which rhc) ] || die "rhc command not found (Use 'gem install rhc' install it)"

# Login to openshift
# Q1: Generate a token now? (yes|no) no
# Q2: Upload ssh key now? (yes|no) yes
# Q3: Provide a name for this key: cikey

echo -e "no\nyes\ncikey" | rhc setup --server ${OPENSHIFT_SERVER}  -l ${OPENSHIFT_ACCOUNT} -p ${OPENSHIFT_PASSWORD} > /dev/null 2>&1

# Use git to deploy
git remote add openshift ${OPENSHIFT_REPO}

# Make deploy with circle-ci
# https://twitter.com/circleci/status/440917469432406016
[[ ! -e "$(git rev-parse --git-dir)/shallow" ]] || git fetch --unshallow

# deploy master branch only
git push openshift master -f

# After all done, leave openshift
rhc logout

# kill the ssh key when in CI
if [ !  -z $CI ]; then
    rm -rf ~/.ssh
fi