USE thehoneycomb;
create table IF NOT EXISTS APP_LOGS(
    LOG_ID varchar(100) primary key,
    ENTRY_DATE timestamp,
    LOGGER varchar(100),
    LOG_LEVEL varchar(100),
    MESSAGE TEXT,
    EXCEPTION TEXT
)