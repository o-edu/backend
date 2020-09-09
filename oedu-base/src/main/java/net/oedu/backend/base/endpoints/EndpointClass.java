package net.oedu.backend.base.endpoints;

import lombok.Data;

@Data
public abstract class EndpointClass {

    private final String name;
    private final String description;
}
