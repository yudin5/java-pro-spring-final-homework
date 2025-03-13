-- Создание таблицы limits
CREATE TABLE IF NOT EXISTS limits
(
    client_id    bigint    PRIMARY KEY,
    day_limit    int
);

-- Начальное заполнение таблицы limits
INSERT INTO limits(client_id, day_limit)
VALUES (1, 15000),
       (2, 25000),
       (3, 35000)
ON CONFLICT (client_id) DO NOTHING;