CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE cities (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES countries(id),
    name VARCHAR(120) NOT NULL,
    plate_code VARCHAR(2) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_cities_country_name UNIQUE (country_id, name),
    CONSTRAINT uk_cities_country_plate UNIQUE (country_id, plate_code)
);

CREATE INDEX idx_countries_active_code ON countries(code) WHERE active = TRUE;
CREATE INDEX idx_cities_country_sort_active ON cities(country_id, sort_order) WHERE active = TRUE;

INSERT INTO countries (code, name, display_name, active)
VALUES ('TR', 'Turkey', 'Türkiye', TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    display_name = EXCLUDED.display_name,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO cities (country_id, name, plate_code, sort_order, active)
SELECT c.id, v.name, v.plate_code, v.sort_order, TRUE
FROM countries c
CROSS JOIN (VALUES
    ('Adana', '01', 1),
    ('Adıyaman', '02', 2),
    ('Afyonkarahisar', '03', 3),
    ('Ağrı', '04', 4),
    ('Amasya', '05', 5),
    ('Ankara', '06', 6),
    ('Antalya', '07', 7),
    ('Artvin', '08', 8),
    ('Aydın', '09', 9),
    ('Balıkesir', '10', 10),
    ('Bilecik', '11', 11),
    ('Bingöl', '12', 12),
    ('Bitlis', '13', 13),
    ('Bolu', '14', 14),
    ('Burdur', '15', 15),
    ('Bursa', '16', 16),
    ('Çanakkale', '17', 17),
    ('Çankırı', '18', 18),
    ('Çorum', '19', 19),
    ('Denizli', '20', 20),
    ('Diyarbakır', '21', 21),
    ('Edirne', '22', 22),
    ('Elazığ', '23', 23),
    ('Erzincan', '24', 24),
    ('Erzurum', '25', 25),
    ('Eskişehir', '26', 26),
    ('Gaziantep', '27', 27),
    ('Giresun', '28', 28),
    ('Gümüşhane', '29', 29),
    ('Hakkari', '30', 30),
    ('Hatay', '31', 31),
    ('Isparta', '32', 32),
    ('Mersin', '33', 33),
    ('İstanbul', '34', 34),
    ('İzmir', '35', 35),
    ('Kars', '36', 36),
    ('Kastamonu', '37', 37),
    ('Kayseri', '38', 38),
    ('Kırklareli', '39', 39),
    ('Kırşehir', '40', 40),
    ('Kocaeli', '41', 41),
    ('Konya', '42', 42),
    ('Kütahya', '43', 43),
    ('Malatya', '44', 44),
    ('Manisa', '45', 45),
    ('Kahramanmaraş', '46', 46),
    ('Mardin', '47', 47),
    ('Muğla', '48', 48),
    ('Muş', '49', 49),
    ('Nevşehir', '50', 50),
    ('Niğde', '51', 51),
    ('Ordu', '52', 52),
    ('Rize', '53', 53),
    ('Sakarya', '54', 54),
    ('Samsun', '55', 55),
    ('Siirt', '56', 56),
    ('Sinop', '57', 57),
    ('Sivas', '58', 58),
    ('Tekirdağ', '59', 59),
    ('Tokat', '60', 60),
    ('Trabzon', '61', 61),
    ('Tunceli', '62', 62),
    ('Şanlıurfa', '63', 63),
    ('Uşak', '64', 64),
    ('Van', '65', 65),
    ('Yozgat', '66', 66),
    ('Zonguldak', '67', 67),
    ('Aksaray', '68', 68),
    ('Bayburt', '69', 69),
    ('Karaman', '70', 70),
    ('Kırıkkale', '71', 71),
    ('Batman', '72', 72),
    ('Şırnak', '73', 73),
    ('Bartın', '74', 74),
    ('Ardahan', '75', 75),
    ('Iğdır', '76', 76),
    ('Yalova', '77', 77),
    ('Karabük', '78', 78),
    ('Kilis', '79', 79),
    ('Osmaniye', '80', 80),
    ('Düzce', '81', 81)
) AS v(name, plate_code, sort_order)
WHERE c.code = 'TR'
ON CONFLICT (country_id, name) DO UPDATE
SET plate_code = EXCLUDED.plate_code,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active,
    updated_at = now();
