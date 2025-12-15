#!/bin/bash
#
# JEquity
# Copyright(c) 2008-2025, Beowurks
# Original Author: Eddie Fann
# License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
#
#

if [[ -z "$1" || -z "$2" ]]; then
  echo -e "\n"
  echo -e "You must run this script in a Linux environment.\n"
  echo -e "1st parameter: password for the private key."
  echo -e "2nd parameter: password for the PKCS12 keystore."
  echo -e "\nExiting. . . .\n\n"
  exit 1
fi

LINE_MARKER="\n========================================================================================"
BASE_FILE='rootOSS'

echo -e $LINE_MARKER
echo -e "\nRemoving previous $BASE_FILE files."
rm -v $BASE_FILE.*
echo -e $LINE_MARKER

echo -e "This script generates a self-signed x509 certificate on Linux that's valid for 10 years.\nRunning the following:\n"
echo -e "\nopenssl genrsa -aes256 -passout pass:$1 -out $BASE_FILE.key 16384"
openssl genrsa -aes256 -passout pass:"$1" -out $BASE_FILE.key 16384

if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl genrsa -aes256" >&2
  exit 1
fi
echo -e $LINE_MARKER

echo -e "\nopenssl req -x509 -new -nodes -key $BASE_FILE.key -sha512 -days 3650 -passin pass:$1 -out $BASE_FILE.pem"
openssl req -x509 -new -nodes -key $BASE_FILE.key -sha512 -days 3650 -passin pass:"$1" -out $BASE_FILE.pem<<EOF
US
Texas
Austin
Beowurks.com
Open-Source Software
Beowurks OSS Root Certificate
efann@beowurks.com
EOF

if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl req -x509'" >&2
  exit 1
fi
echo -e $LINE_MARKER

openssl x509 -text -noout -in $BASE_FILE.pem
# Combine them into a PKCS12 keystore (my-keystore.pfx)
echo -e "\nopenssl pkcs12 -export -out $BASE_FILE.pfx -inkey $BASE_FILE.key -in $BASE_FILE.pem -name SSO Certificate -passin pass:$1 -passout pass:$2"
openssl pkcs12 -export -out $BASE_FILE.pfx -inkey $BASE_FILE.key -in $BASE_FILE.pem -name "SSO Certificate" -passin pass:"$1" -passout pass:"$2"
if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl pkcs12 -export'" >&2
  exit 1
fi
echo -e $LINE_MARKER

echo -e "\n\nNow copy $BASE_FILE.* over to the folder of your choosing."
