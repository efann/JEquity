CREATE TABLE <<schema>>"Financial"
(
    FINANCIALID <<primary_auto_increment>>,
    GROUPID INT NOT NULL,
    DESCRIPTION VARCHAR(255) DEFAULT '' NOT NULL,
    SYMBOL CHAR(10) DEFAULT '' NOT NULL,
    ACCOUNT VARCHAR(255) DEFAULT '' NOT NULL,
    TYPE VARCHAR(255) DEFAULT '' NOT NULL,
    CATEGORY VARCHAR(255) DEFAULT '' NOT NULL,
    SHARES FLOAT(52) DEFAULT 0 NOT NULL,
    PRICE FLOAT(52) DEFAULT 0 NOT NULL,
    VALUATIONDATE DATE DEFAULT '1980-01-01' NOT NULL,
    RETIREMENT BOOLEAN DEFAULT false NOT NULL,
    COMMENTS <<long_varchar>>,
    PRIMARY KEY (FINANCIALID)
);