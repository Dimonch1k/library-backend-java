-- Drop tables in proper order to avoid FK issues
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE "order" CASCADE CONSTRAINTS';
  EXECUTE IMMEDIATE 'DROP TABLE "user" CASCADE CONSTRAINTS';
  EXECUTE IMMEDIATE 'DROP TABLE book CASCADE CONSTRAINTS';
  EXECUTE IMMEDIATE 'DROP TABLE author CASCADE CONSTRAINTS';
  EXECUTE IMMEDIATE 'DROP TABLE image CASCADE CONSTRAINTS';
  EXECUTE IMMEDIATE 'DROP TYPE order_status';
EXCEPTION
  WHEN OTHERS THEN NULL; -- Ignore if not existing
END;
/

-- Create custom ENUM type as an Oracle OBJECT type + CHECK
CREATE TYPE order_status AS OBJECT
(
  status VARCHAR2(20)
);
/

-- Table: image
CREATE TABLE image
(
  id         VARCHAR2(36) PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

  name       VARCHAR2(255)                                      NOT NULL,
  path       VARCHAR2(255)                                      NOT NULL
);

-- Table: author
CREATE TABLE author
(
  id         VARCHAR2(36) PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

  first_name VARCHAR2(255)                                      NOT NULL,
  last_name  VARCHAR2(255)                                      NOT NULL,
  age        NUMBER                                             NOT NULL
);

-- Table: book
CREATE TABLE book
(
  id          VARCHAR2(36) PRIMARY KEY,
  created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

  title       VARCHAR2(255)                                      NOT NULL,
  description CLOB,
  genre       VARCHAR2(100)                                      NOT NULL,
  year        NUMBER                                             NOT NULL,

  image_id    VARCHAR2(36) UNIQUE,
  CONSTRAINT fk_book_image FOREIGN KEY (image_id) REFERENCES image (id) ON DELETE SET NULL,

  author_id   VARCHAR2(36),
  CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id) ON DELETE SET NULL
);

-- Table: user
CREATE TABLE "user"
(
  id         VARCHAR2(36) PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

  email      VARCHAR2(255) UNIQUE                               NOT NULL,
  password   VARCHAR2(255)                                      NOT NULL,
  role       VARCHAR2(20)             DEFAULT 'USER'            NOT NULL
);

-- Table: order
CREATE TABLE "order"
(
  id          VARCHAR2(36) PRIMARY KEY,
  created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

  name        VARCHAR2(255)                                      NOT NULL,
  borrow_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  return_date TIMESTAMP WITH TIME ZONE,

  status      VARCHAR2(20)             DEFAULT 'ACTIVE'          NOT NULL
    CHECK (status IN ('NEW', 'ACTIVE', 'RETURNED', 'CANCELLED')),

  user_id     VARCHAR2(36),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE SET NULL,

  book_id     VARCHAR2(36),
  CONSTRAINT fk_order_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE SET NULL
);
