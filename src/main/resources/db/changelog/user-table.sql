--liquibase formatted sql

--changeset mrshoffen:1
CREATE SCHEMA IF NOT EXISTS cloudStorage;

--changeset mrshoffen:2
CREATE TABLE IF NOT EXISTS cloudStorage.users
(
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    storage_plan VARCHAR(10)  NOT NULL DEFAULT 'BASIC',
    avatar_url   VARCHAR(256)
);
