package com.infoclinika.mssharing.platform.repository;

/**
 * @author Herman Zamula
 */
public abstract class QueryTemplates {

    public static final String WRITE_ACCESS =
        "com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate$Access.WRITE";

    public static final String PUBLIC_PROJECT = "com.infoclinika.mssharing.platform.entity.Sharing$Type.PUBLIC";
    public static final String PRIVATE_PROJECT = "com.infoclinika.mssharing.platform.entity.Sharing$Type.PRIVATE";
    public static final String SHARED_PROJECT = "com.infoclinika.mssharing.platform.entity.Sharing$Type.SHARED";

    public static final String JOIN_COLLABORATORS = " left join p.sharing.collaborators uc " +
        " left join p.sharing.groupsOfCollaborators gc" +
        " left join gc.group.collaborators gcc ";

    public static final String IS_USER_COLLABORATOR = "(uc.user.id = :user or gcc.id = :user)";

    public static final String IS_USER_LAB_MEMBER =
        "f.instrument.lab.id in (select ulm.lab.id from UserLabMembership ulm where ulm.user.id = :user) ";


    public static final String HAVE_ACCESS_TO_PROJECT =
        "(select count(*) from UserProjectAccess upa where upa.user.id=:user and upa.project.id=p.id) > 0";
    public static final String IS_LAB_MEMBER =
        "(SELECT count(*) FROM lab.labMemberships lm where lm.user.id = :user) > 0";

    public static final String HAVE_WRITE_ACCESS_TO_PROJECT = "(select count(*) from UserProjectAccess upa where" +
        " upa.user.id=:user " +
        " and upa.project.id=p.id " +
        " and upa.accessLevel=" + WRITE_ACCESS +
        ") > 0";

    public static final String IS_FILE_AVAILABLE = "(f.owner.id = :user \n" +
        " or " + IS_USER_LAB_MEMBER +
        " or (" +
        " select count(*) from ExperimentTemplate e join e.rawFiles.data d join e.project p " +
        " where e.isDeleted = false and  (d.fileMetaData.id=f.id \n" +
        " and (" + HAVE_ACCESS_TO_PROJECT + " or p.sharing.type = " + PUBLIC_PROJECT + "))) > 0)";

    public static final String SHARED_PROJECTS_QUERY = "select p from ProjectTemplate p where " +
        " p.isDeleted = false and" +
        " p.sharing.type <> " + PUBLIC_PROJECT +
        " and " + HAVE_ACCESS_TO_PROJECT;

    public static final String JOIN_LAB_MEMBERS = " left join p.lab lab left join lab.labMemberships lm ";

    public static final String IS_USER_LAB_HEAD = " (lm.user.id = :user and lm.head = true)";

    public static class Search {

        public static class Projects {

            public static final String FILTER_PROJECTS_BY_QUERY =
                " (cast(p.id as string) like :query or lower(p.name) like :query or lower(p.description) like :query " +
                    "or lower(p.areaOfResearch) like :query)";

            public static final String FILTER_PROJECTS_BY_QUERY_WITH_ID =
                " (lower(p.name) like :query " +
                    "or lower(p.description) like :query " +
                    "or lower(p.areaOfResearch) like :query " +
                    "or lower(p.id) like :query)";

            public static final String ALL_AVAILABLE_PROJECTS_WITH_QUERY =
                " select distinct p from ProjectTemplate p " +
                    JOIN_LAB_MEMBERS +
                    " where " + FILTER_PROJECTS_BY_QUERY +
                    " and p.isDeleted=false " +
                    " and (p.creator.id = :user" +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ") " +
                    " or " + IS_USER_LAB_HEAD + ")";

            public static final String ALL_AVAILABLE_PROJECTS_WITH_QUERY_AND_ID =
                " select distinct p from ProjectTemplate p " +
                    JOIN_LAB_MEMBERS +
                    " where " + FILTER_PROJECTS_BY_QUERY_WITH_ID +
                    " and p.isDeleted=false " +
                    " and (p.creator.id = :user" +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ") " +
                    " or " + IS_USER_LAB_HEAD + ")";
            public static final String COUNT_ALL_AVAILABLE_PROJECTS_WITH_QUERY =
                " select count(distinct p.id) from ProjectTemplate p " +
                    JOIN_LAB_MEMBERS +
                    " where " + FILTER_PROJECTS_BY_QUERY +
                    " and p.isDeleted=false " +
                    " and (p.creator.id = :user" +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ") " +
                    " or " + IS_USER_LAB_HEAD + ")";

        }

        public static class Files {

            public static final String FILTER_FILES_BY_QUERY =
                " (cast(f.id as string) like :query or lower(f.name) like :query or lower(f.labels) like :query)";

            private static final String WHERE_ALL_AVAILABLE_FILES_WITH_QUERY = " where " + FILTER_FILES_BY_QUERY +
                " and (f.owner.id = :user" +
                " or (select count(*) from InstrumentTemplate i join i.lab lab join lab.labMemberships membersh " +
                " where membersh.user.id = :user and f.instrument = i) > 0 " +
                " or (select count(*) from ExperimentTemplate e join e.rawFiles.data d join e.project p " +
                " where d.fileMetaData.id = f.id and e.isDeleted=false " +
                " and ((p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ") " +
                " or p.sharing.type = " + PUBLIC_PROJECT + ")) > 0)";

            public static final String ALL_AVAILABLE_FILES_WITH_QUERY =
                " select distinct f from FileMetaDataTemplate f " + WHERE_ALL_AVAILABLE_FILES_WITH_QUERY;
            public static final String COUNT_ALL_AVAILABLE_FILES_WITH_QUERY =
                " select count(distinct f.id) from FileMetaDataTemplate f " + WHERE_ALL_AVAILABLE_FILES_WITH_QUERY;

        }

        public static class Instrument {

            public static final String FILTER_INSTRUMENTS_BY_QUERY =
                " (cast(i.id as string) like :query or lower(i.name) like :query or lower(i.serialNumber) like :query" +
                    " or lower(i.peripherals) like :query ) ";
            public static final String COUNT_ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY =
                " select count(distinct i.id) from InstrumentTemplate i " +
                    " where " + FILTER_INSTRUMENTS_BY_QUERY +
                    " and ( i.lab.id in (select l.id from LabTemplate l left join l.labMemberships lm" +
                    " where lm.user.id = :user))";
            public static final String ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY =
                " select i from InstrumentTemplate i " +
                    " where " + FILTER_INSTRUMENTS_BY_QUERY +
                    " and ( i.lab.id in (select l.id from LabTemplate l left join l.labMemberships lm" +
                    " where lm.user.id = :user))";

        }

        public static class Experiment {

            public static final String FILTER_EXPERIMENTS_BY_QUERY =
                " (cast(e.id as string) like :query or lower(e.name) like :query or lower(e.experiment.description) " +
                    "like :query)";

            public static final String COUNT_ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY =
                " select count(distinct e.id) from ExperimentTemplate e left join e.project p" +
                    " where e.isDeleted = false and " + FILTER_EXPERIMENTS_BY_QUERY +
                    " and (p.creator.id = :user " +
                    " or e.creator.id = :user " +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ")) ";

            public static final String ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY =
                " select distinct e from ExperimentTemplate e left join e.project p" +
                    " where e.isDeleted = fa and " + FILTER_EXPERIMENTS_BY_QUERY +
                    " and (p.creator.id = :user " +
                    " or e.creator.id = :user " +
                    " or p.sharing.type = " + PUBLIC_PROJECT +
                    " or (p.sharing.type = " + SHARED_PROJECT + " and " + HAVE_ACCESS_TO_PROJECT + ")) ";
        }
    }


}
