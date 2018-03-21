package com.infoclinika.mssharing.integration.test.data.projectdata;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.fail;

/**
 * @author Alexander Orlov
 */
public class ProjectData {

    private final String name;
    private final String laboratory;
    private final String area;
    private final String description;
    private List<PersonToInvite> personToInvite = new ArrayList<>();
    private final boolean hasBlog;

    private ProjectData(Builder builder) {
        this.name = builder.name;
        this.laboratory = builder.laboratory;
        this.area = builder.area;
        this.description = builder.description;
        this.personToInvite = builder.personToInvite;
        this.hasBlog = builder.hasBlog;
    }

    public String getName() {
        return name;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public String getArea() {
        return area;
    }

    public String getDescription() {
        return description;
    }

    public List<PersonToInvite> getPersonToInvite() {
        return personToInvite;
    }

    public boolean getHasBlog() {
        return hasBlog;
    }

    public static class Builder {

        private String name;
        private String laboratory;
        private String area;
        private String description;
        private List<PersonToInvite> personToInvite = new ArrayList<>();
        private boolean hasBlog;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder laboratory(String laboratory) {
            this.laboratory = laboratory;
            return this;
        }

        public Builder area(String area) {
            this.area = area;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder personToInvite(List<PersonToInvite> personToInvite) {
            this.personToInvite = personToInvite;
            return this;
        }

        public Builder hasBlog(boolean hasBlog) {
            this.hasBlog = hasBlog;
            return this;
        }

        public ProjectData build() {
            return new ProjectData(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectData that = (ProjectData) o;

        if (hasBlog != that.hasBlog) return false;
        if (area != null ? !area.equals(that.area) : that.area != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (laboratory != null ? !laboratory.equals(that.laboratory) : that.laboratory != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (personToInvite != null ? !personToInvite.equals(that.personToInvite) : that.personToInvite != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (laboratory != null ? laboratory.hashCode() : 0);
        result = 31 * result + (area != null ? area.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (personToInvite != null ? personToInvite.hashCode() : 0);
        result = 31 * result + (hasBlog ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectData{" +
                "name='" + name + '\'' +
                ", laboratory='" + laboratory + '\'' +
                ", area='" + area + '\'' +
                ", description='" + description + '\'' +
                ", personToInvite=" + personToInvite +
                ", hasBlog=" + hasBlog +
                '}';
    }
}
