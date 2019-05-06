package com.infoclinika.mssharing.model.internal;


import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.platform.entity.Sharing;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Component;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

@Component
public class CriteriaRules {

    public Criterion isFileAvailableFromExperiment(long actor, String fileIdAlias) {
        final DetachedCriteria isFileSharedQuery = DetachedCriteria.forClass(ActiveExperiment.class, "e")
                .createAlias("e.rawFiles.data", "d")
                .createAlias("e.project", "p");

        joinCollaborators(isFileSharedQuery, "p").add(Restrictions.and(
                Restrictions.eqProperty("d.fileMetaData.id", fileIdAlias),
                isNotOwnedProjectAvailable(actor)
        ))
                .setProjection(Projections.rowCount());
        return Subqueries.lt((long) 0, isFileSharedQuery);
    }

    public LogicalExpression isNotOwnedProjectAvailable(long actor) {
        return Restrictions.or(
                Restrictions.eq("p.sharing.type", Sharing.Type.PUBLIC),
                Restrictions.or(
                        Restrictions.eq("uc.user.id", actor),
                        Restrictions.eq("gcc.id", actor)
                )
        );
    }

    public Criteria joinCollaborators(Criteria criteria, String projectAlias) {
        return criteria.createAlias(projectAlias + ".sharing.collaborators", "uc", LEFT_OUTER_JOIN)
                .createAlias(projectAlias + ".sharing.groupsOfCollaborators", "gc", LEFT_OUTER_JOIN)
                .createAlias("gc.group", "gcg", LEFT_OUTER_JOIN)
                .createAlias("gcg.collaborators", "gcc", LEFT_OUTER_JOIN);
    }

    public DetachedCriteria joinCollaborators(DetachedCriteria criteria, String projectAlias) {
        return criteria.createAlias(projectAlias + ".sharing.collaborators", "uc", LEFT_OUTER_JOIN)
                .createAlias(projectAlias + ".sharing.groupsOfCollaborators", "gc")
                .createAlias("gc.group", "gcg")
                .createAlias("gcg.collaborators", "gcc");
    }

    public Criterion isUserOperatorOfInstrumentForFile(long actor, String instrumentIdAlias) {
        //Is user instrument operator
        final DetachedCriteria subquery = DetachedCriteria.forClass(Instrument.class, "i");
        subquery.createAlias("i.operators", "o", INNER_JOIN)
                .add(Restrictions.and(Restrictions.eqProperty("id", instrumentIdAlias), Restrictions.eq("o.id", actor)))
                .setProjection(Projections.rowCount());
        //
        return Subqueries.lt((long) 0, subquery);
    }
}
