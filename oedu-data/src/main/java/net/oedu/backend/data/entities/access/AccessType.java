package net.oedu.backend.data.entities.access;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AccessType {

    READ(0),
    COMMENTS(1),
    WRITE(2),
    EDIT(3),
    ADMIN(4);

    private final int lvl;

    public boolean hasAccess(final AccessType minAccessType) {
        return this.lvl >= minAccessType.lvl;
    }
}
