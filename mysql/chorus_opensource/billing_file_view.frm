TYPE=VIEW
query=select `fmd`.`id` AS `id`,`fmd`.`archiveId` AS `archiveId`,`fmd`.`contentId` AS `contentId`,`fmd`.`instrument_id` AS `instrument_id`,`fmd`.`bill_lab` AS `bill_lab`,`fmd`.`name` AS `file_name`,concat(`u`.`firstName`,\' \',`u`.`lastName`) AS `owner_name`,`u`.`id` AS `owner_id`,`i`.`name` AS `instrument_name`,`fmd`.`billing_last_charging_date` AS `last_charging_date`,`i`.`lab_id` AS `instrument_lab` from ((`chorus_opensource`.`FileMetaDataTemplate` `fmd` join `chorus_opensource`.`Instrument` `i` on((`fmd`.`instrument_id` = `i`.`id`))) join `chorus_opensource`.`USER` `u` on((`u`.`id` = `fmd`.`owner_id`)))
md5=e43c01369098cb0fabcbe54473eef832
updatable=1
algorithm=0
definer_user=chorus
definer_host=%
suid=2
with_check_option=0
timestamp=2018-03-19 15:03:47
create-version=1
source=select fmd.id AS id, fmd.archiveId AS archiveId, fmd.contentId AS contentId, fmd.instrument_id AS instrument_id, fmd.bill_lab AS bill_lab, fmd.name as file_name, CONCAT(u.firstName ,\' \' ,u.lastName) as owner_name, u.id as owner_id, i.name as instrument_name, fmd.billing_last_charging_date as last_charging_date, i.lab_id AS instrument_lab from (FileMetaDataTemplate fmd join Instrument i on((fmd.instrument_id = i.id)) join USER u on u.id=fmd.owner_id)
client_cs_name=latin1
connection_cl_name=latin1_swedish_ci
view_body_utf8=select `fmd`.`id` AS `id`,`fmd`.`archiveId` AS `archiveId`,`fmd`.`contentId` AS `contentId`,`fmd`.`instrument_id` AS `instrument_id`,`fmd`.`bill_lab` AS `bill_lab`,`fmd`.`name` AS `file_name`,concat(`u`.`firstName`,\' \',`u`.`lastName`) AS `owner_name`,`u`.`id` AS `owner_id`,`i`.`name` AS `instrument_name`,`fmd`.`billing_last_charging_date` AS `last_charging_date`,`i`.`lab_id` AS `instrument_lab` from ((`chorus_opensource`.`FileMetaDataTemplate` `fmd` join `chorus_opensource`.`Instrument` `i` on((`fmd`.`instrument_id` = `i`.`id`))) join `chorus_opensource`.`USER` `u` on((`u`.`id` = `fmd`.`owner_id`)))
