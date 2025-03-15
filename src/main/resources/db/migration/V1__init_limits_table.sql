-- Создание таблицы limits
CREATE TABLE IF NOT EXISTS limits
(
    client_id      bigint    PRIMARY KEY,
    daily_limit    int
);

-- Начальное заполнение таблицы limits
INSERT INTO limits(client_id, daily_limit)
VALUES (1, 10000),
       (2, 10000),
       (3, 10000)
ON CONFLICT (client_id) DO NOTHING;