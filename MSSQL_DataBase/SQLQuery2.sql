use WaterStation;


/*
delete from WaterStations;
delete from Locations;
delete from Providers;
delete from WaterType;
delete from DeprecatedRecord;
*/
/*
insert into Locations values(2,'ll1');
insert into Providers values(2,'pp1');
insert into WaterType values(2,'ww');
insert into WaterStations values(2,2,'aabb',2,2,'1999-9-29');
*/

select * from Locations;
select * from Providers;
select * from WaterType;
select * from DeprecatedRecord;


select * from WaterStations left outer join Locations on location = locationID
left outer join WaterType on typeID = waterType
left outer join Providers on provider = providerID

--select * from WaterStations;

--insert into DeprecatedRecord values('ll',5);
--insert into DeprecatedRecord values('ll',6);

--select top(1) record from DeprecatedRecord where tableName = 'll';

--update WaterStations set location = 1, name = 'aabb2', waterType = 1, provider = 1, updateDate = '1999-10-09' where stationID = 1

--exec getWaterStationById @id = 1;

--delete from WaterStations where stationID = 2
--delete from WaterType where typeID = 2;