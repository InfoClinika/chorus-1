package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Alexander Orlov
 */
public class SharingGroupData {

    private final String name;
    private final String personToInviteName;
    private final String personToInviteEmail;

    private SharingGroupData(Builder builder) {
        this.name = builder.name;
        this.personToInviteName = builder.personToInviteName;
        this.personToInviteEmail = builder.personToInviteEmail;
    }

    public String getName() {
        return name;
    }

    public String getPersonToInviteName() {
        return personToInviteName;
    }

    public String getPersonToInviteEmail() {
        return personToInviteEmail;
    }

    public static class Builder {
        private String name;
        private String personToInviteName;
        private String personToInviteEmail;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder personToInviteName(String personToInviteName) {
            this.personToInviteName = personToInviteName;
            return this;
        }

        public Builder personToInviteEmail(String personToInviteEmail) {
            this.personToInviteEmail = personToInviteEmail;
            return this;
        }

        public SharingGroupData build() {
            return new SharingGroupData(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharingGroupData that = (SharingGroupData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (personToInviteEmail != null ? !personToInviteEmail.equals(that.personToInviteEmail) : that.personToInviteEmail != null)
            return false;
        if (personToInviteName != null ? !personToInviteName.equals(that.personToInviteName) : that.personToInviteName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (personToInviteName != null ? personToInviteName.hashCode() : 0);
        result = 31 * result + (personToInviteEmail != null ? personToInviteEmail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SharingGroupData{" +
                "name='" + name + '\'' +
                ", personToInviteName='" + personToInviteName + '\'' +
                ", personToInviteEmail='" + personToInviteEmail + '\'' +
                '}';
    }
}
