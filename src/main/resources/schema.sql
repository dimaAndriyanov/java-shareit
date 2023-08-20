CREATE TABLE IF NOT EXISTS USERS (
    USER_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USER_NAME VARCHAR(100) NOT NULL,
    EMAIL VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ITEMS (
    ITEM_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ITEM_NAME VARCHAR(100) NOT NULL,
    DESCRIPTION VARCHAR(1000) NOT NULL,
    AVAILABLE BOOLEAN NOT NULL,
    OWNER_USER_ID BIGINT NOT NULL,
    CONSTRAINT ITEMS_USERS FOREIGN KEY (OWNER_USER_ID) REFERENCES USERS(USER_ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS BOOKINGS (
    BOOKING_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    START_DATE TIMESTAMP NOT NULL,
    END_DATE TIMESTAMP NOT NULL,
    STATUS VARCHAR(8) NOT NULL,
    BOOKER_USER_ID BIGINT NOT NULL,
    ITEM_ID BIGINT NOT NULL,
    CONSTRAINT BOOKINGS_USERS FOREIGN KEY (BOOKER_USER_ID) REFERENCES USERS(USER_ID) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT BOOKINGS_ITEMS FOREIGN KEY (ITEM_ID) REFERENCES ITEMS(ITEM_ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS COMMENTS (
    COMMENT_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ITEM_ID BIGINT NOT NULL,
    TEXT VARCHAR(1000) NOT NULL,
    AUTHOR_NAME VARCHAR(100) NOT NULL,
    CREATED TIMESTAMP NOT NULL,
    CONSTRAINT COMMENTS_ITEMS FOREIGN KEY (ITEM_ID) REFERENCES ITEMS(ITEM_ID) ON DELETE CASCADE ON UPDATE CASCADE
);