create database if not exists milkyway_pm;

use milkyway_pm;

create table if not exists watchlist(
    id int auto_increment primary key,
    stock varchar(200) not null
);

create table if not exists transactions(
    id int auto_increment primary key,
    date date not null,
    type varchar(50) not null,
    transaction_value decimal(15,2) not null,
    purse_value decimal(15,2) not null,
    status boolean not null
);

create table if not exists holdings(
    id int auto_increment primary key,
    stock varchar(200) not null,
    quantity int not null,
    total_invested decimal(15,2) not null
);

insert into watchlist (stock) values ('AAPL'), ('GOOGL'), ('MSFT');

insert into transactions (date, type, transaction_value, purse_value, status) values
('2026-01-30', 'PURSE ADD', 10000, 10000, true),
('2026-02-02', 'BUY AAPL', 2700, 7300, true),
('2026-02-03', 'BUY GOOGL', 3400, 3900, true),
('2026-02-03', 'BUY MSFT', 4100, 3900, false);

insert into holdings (stock, quantity, total_invested) values
('AAPL', 10, 2700),
('GOOGL', 10, 3400);