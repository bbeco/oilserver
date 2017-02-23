drop table if EXISTS CHAT;
DROP TABLE IF EXISTS email;

create table CHAT (
	_id INTEGER AUTO_INCREMENT PRIMARY KEY, 
	sender TEXT NOT NULL,
	recipient TEXT NOT NULL, 
	payload TEXT NOT NULL, 
	ts INTEGER NOT NULL
);

CREATE TABLE email
(
        emailAddress    VARCHAR(255)    PRIMARY_KEY,
        name            VARCHAR(255)    NOT NULL
);
