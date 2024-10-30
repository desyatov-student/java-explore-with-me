CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(40) NOT NULL,
    UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS events (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
   initiator_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
   category_id BIGINT REFERENCES categories (id),
   request_id BIGINT,
   annotation VARCHAR NOT NULL,
   title VARCHAR NOT NULL,
   description VARCHAR NOT NULL,
   created_on TIMESTAMP,
   event_date TIMESTAMP,
   published_on TIMESTAMP,
   latitude FLOAT NOT NULL,
   longitude FLOAT NOT NULL,
   paid BOOLEAN NOT NULL,
   participant_limit INT,
   request_moderation BOOLEAN,
   state VARCHAR
);

CREATE TABLE IF NOT EXISTS requests (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
   requester_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
   event_id BIGINT REFERENCES events (id) ON DELETE CASCADE,
   created TIMESTAMP NOT NULL,
   status VARCHAR NOT NULL,
   UNIQUE(requester_id, event_id)
);

ALTER TABLE events DROP CONSTRAINT IF EXISTS fk_events_to_requests;
ALTER TABLE events ADD CONSTRAINT fk_events_to_requests FOREIGN KEY(request_id) REFERENCES requests(id);