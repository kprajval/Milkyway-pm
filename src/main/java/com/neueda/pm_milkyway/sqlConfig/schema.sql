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
