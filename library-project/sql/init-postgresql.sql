-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DROP TABLE IF EXISTS "order";
DROP TYPE IF EXISTS order_status;
DROP TABLE IF EXISTS "user";
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS image;

-- Table: image
CREATE TABLE image
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    name       TEXT        NOT NULL,
    path       TEXT        NOT NULL
);

-- Table: author
CREATE TABLE author
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    first_name TEXT        NOT NULL,
    last_name  TEXT        NOT NULL,
    age        INTEGER     NOT NULL
--     image_id   UUID UNIQUE,
--     CONSTRAINT fk_author_image FOREIGN KEY (image_id) REFERENCES image (id) ON DELETE SET NULL
);

-- Table: book
CREATE TABLE book
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    title       TEXT        NOT NULL,
    description TEXT,
    genre       TEXT        NOT NULL,
    year        INTEGER     NOT NULL,
    image_id    UUID UNIQUE,
    author_id   UUID,
    CONSTRAINT fk_book_image FOREIGN KEY (image_id) REFERENCES image (id) ON DELETE SET NULL,
    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id) ON DELETE SET NULL
);

-- Table: "user"
CREATE TABLE "user"
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    email      TEXT UNIQUE NOT NULL,
    password   TEXT        NOT NULL,
    role       TEXT        NOT NULL DEFAULT 'USER'
);

-- Enum: order_status
CREATE TYPE order_status AS ENUM ('ACTIVE', 'RETURNED', 'CANCELLED');

-- Table: "order"
CREATE TABLE "order"
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    name        TEXT         NOT NULL,
    borrow_date TIMESTAMPTZ  NOT NULL DEFAULT now(),
    return_date TIMESTAMPTZ,
    status      order_status NOT NULL DEFAULT 'ACTIVE',
    user_id     UUID,
    book_id     UUID,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE SET NULL,
    CONSTRAINT fk_order_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE SET NULL
);
