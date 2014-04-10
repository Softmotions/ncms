package com.softmotions.ncms.users;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class NcmsUserPrincipalImpl implements NcmsUserPrincipal {

    private final String name;

    private final String email;

    private final String fullName;

    public NcmsUserPrincipalImpl(String email, String fullName) {
        this.name = email;
        this.email = email;
        this.fullName = fullName;
    }

    public NcmsUserPrincipalImpl(String name, String email, String fullName) {
        this.name = name;
        this.email = email;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcmsUserPrincipalImpl that = (NcmsUserPrincipalImpl) o;
        return name.equals(that.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("NcmsUserPrincipalImpl{");
        sb.append("name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
