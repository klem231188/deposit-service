DROP TABLE IF EXISTS DepositType;
CREATE TABLE DepositType
(
    id   SMALLINT    NOT NULL,
    name VARCHAR(50) NOT NULL,

    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS Deposit;
CREATE TABLE Deposit
(
    id             INTEGER     NOT NULL AUTO_INCREMENT,
    uuid           UUID        NOT NULL,
    userId         VARCHAR(50) NOT NULL,
    depositType    SMALLINT    NOT NULL,
    amount         DOUBLE      NOT NULL,
    expirationDate DATE        NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (depositType) references DepositType (id),
    UNIQUE (uuid)
);
