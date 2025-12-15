#!/bin/bash
#
# JEquity
# Copyright(c) 2008-2025, Beowurks
# Original Author: Eddie Fann
# License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
#
#

if [ -z "$1" ]; then
  echo -e "\nOne must pass the password for the private key. Exiting. . . .\n\n"
  exit 1
fi

if [ -z "$2" ]; then
  echo -e "\nOne must pass the password for the PKCS12 keystore. Exiting. . . .\n\n"
  exit 1
fi

echo -e "This script generates a self-signed x509 certificate on Linux that's valid for 10 years.\nRunning the following:\n"
echo -e "openssl genrsa -aes256 -out rootSSO.key 16384"

openssl genrsa -aes256 -out rootSSO.key 16384 -passout pass:"$1"

openssl req -x509 -new -nodes -key rootSSO.key -sha512 -days 3650 -passin pass:"$1" -out rootSSO.pem<<EOF
US
Texas
Austin
Beowurks.com
Open-Source Software
Open-Source Software Root Certificate
efann@beowurks.com
EOF

openssl x509 -text -noout -in rootSSO.pem
# Combine them into a PKCS12 keystore (my-keystore.pfx)
openssl pkcs12 -export -out rootSSO.pfx -inkey rootSSO.key -in rootSSO.pem -name "SSO Certificate" -passin pass:"$1" -passout pass:"$2"

