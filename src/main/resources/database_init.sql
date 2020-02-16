CREATE DATABASE IF NOT EXISTS `recorder` DEFAULT CHARACTER SET utf8;

-- auto-generated definition
CREATE TABLE IF NOT EXISTS recorder.records (
    logtime  datetime     NOT NULL,
    username varchar(32)  NOT NULL,
    category int          NOT NULL,
    event    int          NOT NULL,
    metadata varchar(512) NULL,
    rawdata  text         NULL
)
    COMMENT 'Player Action Record';

CREATE INDEX records_category_event_index
    ON recorder.records (category, event);

CREATE INDEX records_logtime_index
    ON recorder.records (logtime);

CREATE INDEX records_username_index
    ON recorder.records (username);

