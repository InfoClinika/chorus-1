drop table if exists experiment_dashboard_record;

CREATE OR REPLACE VIEW experiment_dashboard_record AS
SELECT
e.id as id,
e.name,
e.description,
e.lab_id as lab_id,
e.bill_laboratory as bill_lab_id,
e.project_id,
e.creator_id,
count(rf.id) as numberOfFiles,
0 as analyzesCount,
e.lastModification,
e.downloadToken,
e.experimentCategory,
(select l.name from Lab l where l.id = e.lab_id) as labName,
(e.deletionDate is not null) as deleted,
e.failed,
e.experimentType_id
from ExperimentTemplate e
LEFT join RawFile rf on rf.experiment_id=e.id
left outer join ProjectTemplate p on e.project_id=p.id
group by e.id;

drop table if exists project_dashboard_record;

create or replace view project_dashboard_record as
select p.id, p.lab_id, p.name, p.creator_id, u.email, p.areaOfResearch,
(select count(*) from ExperimentTemplate e where e.project_id = p.id) as experiments,
p.lastModification, p.type,
(select l.name from Lab l where p.lab_id = l.id) as labName,
  (p.deletionDate is not null) as deleted
from ProjectTemplate p
join USER u on u.id = p.creator_id;

drop table if exists project_access_view;

create or replace view project_access_view as select distinct u.id USER_id, p.id projectsWithReadAccess_id from USER u
  left join project_user_collaborator puc on puc.user_id = u.id
  left join GroupTemplate_UserTemplate sgu on collaborators_id = u.id
  left join project_group_collaborator pgc on pgc.group_id = sgu.GroupTemplate_id join ProjectTemplate p on (p.creator_id = u.id or p.type = 0 or puc.project_id = p.id or pgc.project_id = p.id);


DROP TABLE IF EXISTS billing_user_function_item_view;

DROP TABLE IF EXISTS billing_file_view;

CREATE OR REPLACE VIEW billing_file_view
  AS select

     fmd.id AS id,
     fmd.archiveId AS archiveId,
     fmd.contentId AS contentId,
     fmd.instrument_id AS instrument_id,
     fmd.bill_lab AS bill_lab,
     fmd.name as file_name,
     CONCAT(u.firstName ,' ' ,u.lastName) as owner_name,
     u.id as owner_id,
     i.name as instrument_name,
     fmd.billing_last_charging_date as last_charging_date,
     i.lab_id AS instrument_lab

   from (FileMetaDataTemplate fmd join Instrument i on((fmd.instrument_id = i.id)) join USER u on u.id=fmd.owner_id);

create or replace view project_access_view as select distinct u.id USER_id, p.id projectsWithReadAccess_id from USER u left join project_user_collaborator puc on puc.user_id = u.id left join GroupTemplate_UserTemplate sgu on collaborators_id = u.id left join project_group_collaborator pgc on pgc.group_id = sgu.GroupTemplate_id join ProjectTemplate p on (p.creator_id = u.id or p.type = 0 or puc.project_id = p.id or pgc.project_id = p.id);
