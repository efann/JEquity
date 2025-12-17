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
BASE_FILE='beowurksOSS'
KEY_PRIVATE=$BASE_FILE".key"
KEY_PUBLIC=$BASE_FILE".cer"
KEY_SIGNING=$BASE_FILE".pfx"

echo -e $LINE_MARKER
echo -e "\nRemoving previous $BASE_FILE files."
rm -v $BASE_FILE.*
echo -e $LINE_MARKER

echo -e "This script generates a self-signed x509 certificate on Linux that's valid for 10 years.\n"

#################################
echo -e "Running openssl genrsa, which can take a couple of minutes at 16384 bits"
# Not including the -verbose option as it's really slow with 16384.
openssl genrsa -aes256 -passout pass:"$1" -out $KEY_PRIVATE 16384

if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl genrsa -aes256" >&2
  exit 1
fi
echo -e "Created $KEY_PRIVATE"
echo -e $LINE_MARKER

#################################
echo -e "Running openssl req"
openssl req -x509 -new -nodes -key $KEY_PRIVATE -sha512 -days 3650 -passin pass:"$1" -out $KEY_PUBLIC -subj "/C=US/ST=Texas/L=Austin/O=Beowurks.com/OU=Beowurks Open-Source Software/CN=Beowurks OSS Root Certificate/emailAddress=efann@beowurks.com"

if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl req -x509'"  >&2
  exit 1
fi
echo -e "Created $KEY_PUBLIC"
echo -e $LINE_MARKER

#################################
openssl x509 -text -noout -in $KEY_PUBLIC
echo -e $LINE_MARKER

#################################
echo -e "Running openssl pkcs12"
openssl pkcs12 -export -out $KEY_SIGNING -inkey $KEY_PRIVATE -in $KEY_PUBLIC -name "Beowurks OSS Certificate" -passin pass:"$1" -passout pass:"$2"
if [ $? -ne 0 ]; then
  echo "An error occurred with 'openssl pkcs12 -export'" >&2
  exit 1
fi
echo -e "Created $KEY_SIGNING"
echo -e $LINE_MARKER

#################################
echo -e "\n\nNow copy $BASE_FILE.* over to the folder of your choosing."

echo -e "\nBy the way, you can view your certificates\nClick Start, type certmgr.msc"
