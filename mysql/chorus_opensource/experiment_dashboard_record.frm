TYPE=VIEW
query=select `e`.`id` AS `id`,`e`.`name` AS `name`,`e`.`description` AS `description`,`e`.`lab_id` AS `lab_id`,`e`.`bill_laboratory` AS `bill_lab_id`,`e`.`project_id` AS `project_id`,`e`.`creator_id` AS `creator_id`,count(`rf`.`id`) AS `numberOfFiles`,0 AS `analyzesCount`,`e`.`lastModification` AS `lastModification`,`e`.`downloadToken` AS `downloadToken`,`e`.`experimentCategory` AS `experimentCategory`,(select `l`.`name` from `chorus_opensource`.`Lab` `l` where (`l`.`id` = `e`.`lab_id`)) AS `labName`,(`e`.`deletionDate` is not null) AS `deleted` from ((`chorus_opensource`.`ExperimentTemplate` `e` left join `chorus_opensource`.`RawFile` `rf` on((`rf`.`experiment_id` = `e`.`id`))) left join `chorus_opensource`.`ProjectTemplate` `p` on((`e`.`project_id` = `p`.`id`))) group by `e`.`id`
md5=b63240784d09547318d775c62301211e
updatable=0
algorithm=0
definer_user=chorus
definer_host=%
suid=2
with_check_option=0
timestamp=2018-03-19 15:03:47
create-version=1
source=SELECT e.id as id, e.name, e.description, e.lab_id as lab_id, e.bill_laboratory as bill_lab_id, e.project_id, e.creator_id, count(rf.id) as numberOfFiles, 0 as analyzesCount, e.lastModification, e.downloadToken, e.experimentCategory, (select l.name from Lab l where l.id = e.lab_id) as labName, (e.deletionDate is not null) as deleted from ExperimentTemplate e LEFT join RawFile rf on rf.experiment_id=e.id left outer join ProjectTemplate p on e.project_id=p.id group by e.id
client_cs_name=latin1
connection_cl_name=latin1_swedish_ci
view_body_utf8=select `e`.`id` AS `id`,`e`.`name` AS `name`,`e`.`description` AS `description`,`e`.`lab_id` AS `lab_id`,`e`.`bill_laboratory` AS `bill_lab_id`,`e`.`project_id` AS `project_id`,`e`.`creator_id` AS `creator_id`,count(`rf`.`id`) AS `numberOfFiles`,0 AS `analyzesCount`,`e`.`lastModification` AS `lastModification`,`e`.`downloadToken` AS `downloadToken`,`e`.`experimentCategory` AS `experimentCategory`,(select `l`.`name` from `chorus_opensource`.`Lab` `l` where (`l`.`id` = `e`.`lab_id`)) AS `labName`,(`e`.`deletionDate` is not null) AS `deleted` from ((`chorus_opensource`.`ExperimentTemplate` `e` left join `chorus_opensource`.`RawFile` `rf` on((`rf`.`experiment_id` = `e`.`id`))) left join `chorus_opensource`.`ProjectTemplate` `p` on((`e`.`project_id` = `p`.`id`))) group by `e`.`id`
