package eu.europeana.entitymanagement.definitions.web;

/**
 * Container class for EntityIds - Disabled status
 */
public class EntityIdDisabledStatus {

    private String entityId;
    private boolean isDisabled;

    public EntityIdDisabledStatus(String entityId, boolean isDisabled) {
        this.entityId = entityId;
        this.isDisabled = isDisabled;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityIdDisabledStatus that = (EntityIdDisabledStatus) o;

        if (isDisabled() != that.isDisabled()) return false;
        return getEntityId().equals(that.getEntityId());
    }

    @Override
    public int hashCode() {
        int result = getEntityId().hashCode();
        result = 31 * result + (isDisabled() ? 1 : 0);
        return result;
    }
}
