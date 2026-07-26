-- Kelime bankasi baslangic verisi.
--
-- Calistirmak icin (container ayaktayken):
--   docker exec -i wordbattle-postgres psql -U postgres -d wordbattle < backend/seed-words.sql
--
-- value alani unique oldugu icin tekrar calistirmak guvenli, mevcut kayitlar atlanir.

INSERT INTO words (id, value, category, difficulty) VALUES
(gen_random_uuid(), 'ELEPHANT',  'animals', 'EASY'),
(gen_random_uuid(), 'LION',      'animals', 'EASY'),
(gen_random_uuid(), 'TIGER',     'animals', 'EASY'),
(gen_random_uuid(), 'GIRAFFE',   'animals', 'MEDIUM'),
(gen_random_uuid(), 'DOLPHIN',   'animals', 'MEDIUM'),
(gen_random_uuid(), 'PENGUIN',   'animals', 'MEDIUM'),
(gen_random_uuid(), 'CROCODILE', 'animals', 'HARD'),
(gen_random_uuid(), 'APPLE',     'food',    'EASY'),
(gen_random_uuid(), 'BREAD',     'food',    'EASY'),
(gen_random_uuid(), 'CHEESE',    'food',    'EASY'),
(gen_random_uuid(), 'CHOCOLATE', 'food',    'MEDIUM'),
(gen_random_uuid(), 'SPAGHETTI', 'food',    'HARD'),
(gen_random_uuid(), 'RED',       'colors',  'EASY'),
(gen_random_uuid(), 'BLUE',      'colors',  'EASY'),
(gen_random_uuid(), 'PURPLE',    'colors',  'MEDIUM'),
(gen_random_uuid(), 'TURQUOISE', 'colors',  'HARD'),
(gen_random_uuid(), 'GUITAR',    'objects', 'EASY'),
(gen_random_uuid(), 'WINDOW',    'objects', 'EASY'),
(gen_random_uuid(), 'KEYBOARD',  'objects', 'MEDIUM'),
(gen_random_uuid(), 'UMBRELLA',  'objects', 'MEDIUM'),
(gen_random_uuid(), 'TELESCOPE', 'objects', 'HARD'),
(gen_random_uuid(), 'RIVER',     'nature',  'EASY'),
(gen_random_uuid(), 'MOUNTAIN',  'nature',  'MEDIUM'),
(gen_random_uuid(), 'THUNDER',   'nature',  'MEDIUM'),
(gen_random_uuid(), 'VOLCANO',   'nature',  'HARD')
ON CONFLICT (value) DO NOTHING;
