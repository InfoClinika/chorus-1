TYPE=VIEW
query=select distinct `u`.`id` AS `USER_id`,`p`.`id` AS `projectsWithReadAccess_id` from ((((`chorus_opensource`.`USER` `u` left join `chorus_opensource`.`project_user_collaborator` `puc` on((`puc`.`user_id` = `u`.`id`))) left join `chorus_opensource`.`GroupTemplate_UserTemplate` `sgu` on((`sgu`.`collaborators_id` = `u`.`id`))) left join `chorus_opensource`.`project_group_collaborator` `pgc` on((`pgc`.`group_id` = `sgu`.`GroupTemplate_id`))) join `chorus_opensource`.`ProjectTemplate` `p` on(((`p`.`creator_id` = `u`.`id`) or (`p`.`type` = 0) or (`puc`.`project_id` = `p`.`id`) or (`pgc`.`project_id` = `p`.`id`))))
md5=83ae432ca0a31dad5d95f0899a273a86
updatable=0
algorithm=0
definer_user=chorus
definer_host=%
suid=2
with_check_option=0
timestamp=2018-03-23 08:09:41
create-version=1
source=select distinct u.id USER_id, p.id projectsWithReadAccess_id from USER u left join project_user_collaborator puc on puc.user_id = u.id left join GroupTemplate_UserTemplate sgu on collaborators_id = u.id left join project_group_collaborator pgc on pgc.group_id = sgu.GroupTemplate_id join ProjectTemplate p on (p.creator_id = u.id or p.type = 0 or puc.project_id = p.id or pgc.project_id = p.id)
client_cs_name=latin1
connection_cl_name=latin1_swedish_ci
view_body_utf8=select distinct `u`.`id` AS `USER_id`,`p`.`id` AS `projectsWithReadAccess_id` from ((((`chorus_opensource`.`USER` `u` left join `chorus_opensource`.`project_user_collaborator` `puc` on((`puc`.`user_id` = `u`.`id`))) left join `chorus_opensource`.`GroupTemplate_UserTemplate` `sgu` on((`sgu`.`collaborators_id` = `u`.`id`))) left join `chorus_opensource`.`project_group_collaborator` `pgc` on((`pgc`.`group_id` = `sgu`.`GroupTemplate_id`))) join `chorus_opensource`.`ProjectTemplate` `p` on(((`p`.`creator_id` = `u`.`id`) or (`p`.`type` = 0) or (`puc`.`project_id` = `p`.`id`) or (`pgc`.`project_id` = `p`.`id`))))
