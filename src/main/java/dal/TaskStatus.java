package dal;

public enum TaskStatus {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    FINISHED("Finished");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static TaskStatus fromString(String status) {
        if (status == null) return NOT_STARTED;

        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.displayName.equals(status)) {
                return taskStatus;
            }
        }
        return NOT_STARTED; // Default fallback
    }
}
