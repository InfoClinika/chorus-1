TYPE=VIEW
query=select `p`.`id` AS `id`,`p`.`lab_id` AS `lab_id`,`p`.`name` AS `name`,`p`.`creator_id` AS `creator_id`,`u`.`email` AS `email`,`p`.`areaOfResearch` AS `areaOfResearch`,(select count(0) from `chorus_opensource`.`ExperimentTemplate` `e` where (`e`.`project_id` = `p`.`id`)) AS `experiments`,`p`.`lastModification` AS `lastModification`,`p`.`type` AS `type`,(select `l`.`name` from `chorus_opensource`.`Lab` `l` where (`p`.`lab_id` = `l`.`id`)) AS `labName`,(`p`.`deletionDate` is not null) AS `deleted` from (`chorus_opensource`.`ProjectTemplate` `p` join `chorus_opensource`.`USER` `u` on((`u`.`id` = `p`.`creator_id`)))
md5=919264b5d56df62c26307af756db7950
updatable=0
algorithm=0
definer_user=chorus
definer_host=%
suid=2
with_check_option=0
timestamp=2018-03-23 08:09:41
create-version=1
source=select p.id, p.lab_id, p.name, p.creator_id, u.email, p.areaOfResearch, (select count(*) from ExperimentTemplate e where e.project_id = p.id) as experiments, p.lastModification, p.type, (select l.name from Lab l where p.lab_id = l.id) as labName, (p.deletionDate is not null) as deleted from ProjectTemplate p join USER u on u.id = p.creator_id
client_cs_name=latin1
connection_cl_name=latin1_swedish_ci
view_body_utf8=select `p`.`id` AS `id`,`p`.`lab_id` AS `lab_id`,`p`.`name` AS `name`,`p`.`creator_id` AS `creator_id`,`u`.`email` AS `email`,`p`.`areaOfResearch` AS `areaOfResearch`,(select count(0) from `chorus_opensource`.`ExperimentTemplate` `e` where (`e`.`project_id` = `p`.`id`)) AS `experiments`,`p`.`lastModification` AS `lastModification`,`p`.`type` AS `type`,(select `l`.`name` from `chorus_opensource`.`Lab` `l` where (`p`.`lab_id` = `l`.`id`)) AS `labName`,(`p`.`deletionDate` is not null) AS `deleted` from (`chorus_opensource`.`ProjectTemplate` `p` join `chorus_opensource`.`USER` `u` on((`u`.`id` = `p`.`creator_id`)))
