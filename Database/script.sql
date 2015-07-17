drop table if EXISTS CHAT;

create table CHAT (
	_id INTEGER AUTO_INCREMENT PRIMARY KEY, 
	sender TEXT NOT NULL,
	recipient TEXT NOT NULL, 
	payload TEXT NOT NULL, 
	ts INTEGER NOT NULL
);

