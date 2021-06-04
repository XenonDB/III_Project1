
create table WaterStations (
	stationID int not null primary key,
	[location] int,
	[name] varchar(4096),
	waterType int,
	[provider] int
);

create table Locations(
	locationID int not null primary key,
	city int,
	[name] varchar(max) not null
);
/*
create table Cities(
	cityID int not null primary key,
	[name] varchar(256) not null
);
*/
create table WaterType(
	typeID int not null primary key,
	[name] varchar(256) not null
);

create table Providers(
	providerID int not null primary key,
	[name] varchar(1024) not null
);

alter table WaterStations add foreign key ([location]) references Locations(locationID);
alter table WaterStations add foreign key (waterType) references WaterType(typeID);
alter table WaterStations add foreign key ([provider]) references Providers(providerID);

--alter table Locations add foreign key (city) references Cities(cityID);


create table DeprecatedRecord(
	tableName varchar(128) not null,
	record int not null
);