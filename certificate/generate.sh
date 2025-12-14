#!/bin/bash
#
# JEquity
# Copyright(c) 2008-2025, Beowurks
# Original Author: Eddie Fann
# License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
#
#

echo "This script generates a self-signed x509 certificate on Linux that's valid for 10 years."

openssl genrsa -aes256 -out rootSSO.key 16384

openssl req -x509 -new -nodes -key rootSSO.key -sha512 -days 3650 -out rootSSO.pem<<EOF
US
Texas
Austin
Beowurks.com
Open-Source Software
Open-Source Software Root Certificate
efann@beowurks.com
EOF

openssl x509 -text -noout -in rootSSO.pem
