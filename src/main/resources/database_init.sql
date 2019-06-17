CREATE DATABASE IF NOT EXISTS `recorder` DEFAULT CHARACTER SET utf8;

CREATE TABLE IF NOT EXISTS recorder.records
(
    logid    int AUTO_INCREMENT
        PRIMARY KEY,
    logtime  datetime                                                         NOT NULL,
    username varchar(16)                                                      NOT NULL,
    category enum ('AUTH', 'GAMING', 'WORLD', 'CHAT', 'INTERACTION', 'PARTY') NOT NULL,
    event    varchar(32)                                                      NOT NULL,
    metadata varchar(512)                                                     NULL,
    rawdata  text                                                             NULL
)
    COMMENT 'Player Action Record';

CREATE INDEX records_logtime_index
    ON recorder.records (logtime);

CREATE INDEX records_username_index
    ON recorder.records (username);